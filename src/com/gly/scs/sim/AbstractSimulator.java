package com.gly.scs.sim;

import java.util.*;

import com.gly.random.AbstractRandomVariable;
import com.gly.random.IntegerRandomVariable;
import com.gly.scs.data.*;
import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;
import com.gly.util.SparseArray;

public abstract class AbstractSimulator {

	public final SupplyChain supplyChain;
	public final SimulationParameters simulationParameters;
	public final OrderUpToLevel regionalInitialInventoryLevel;
	public final OrderUpToLevel retailInitialInventoryLevel;

	protected boolean shouldPrint = false;

	protected FacilityRepository facilityRepo;
	protected AbstractShipmentRepository shipmentRepo =
			new LinkedListShipmentRepository();

	protected AbstractReportRepository reportRepo = new ReportRepository();

	// the following variables are just extracted from the supplyChain
	// object for the sake of convenience
	protected Topology topology;
	public final NationalReplenishmentSchedule nationalReplenishmentSchedule;
	protected DemandModel demand;
	protected XdockShipmentSchedule xdockShipmentSchedule;	
	protected RegionalShipmentSchedule regionalShipmentSchedule;
	protected AbstractShipmentSchedule retailShipmentSchedule;

	protected int simulationStartPeriod;
	protected int simulationEndPeriod;

	protected NationalFacility nationalFacility;

	protected AbstractSimulator(SupplyChain supplyChain,
			SimulationParameters simulationParameters,
			OrderUpToLevel regionalInitialInventoryLevel,
			OrderUpToLevel retailInitialInventoryLevel) {
		this.supplyChain = supplyChain;
		this.simulationParameters = simulationParameters;
		this.regionalInitialInventoryLevel = regionalInitialInventoryLevel;
		this.retailInitialInventoryLevel = retailInitialInventoryLevel;

		this.topology = supplyChain.topology;
		this.nationalReplenishmentSchedule = supplyChain.nationalReplenishmentSchedule;
		this.regionalShipmentSchedule = supplyChain.regionalShipmentSchedule;
		this.retailShipmentSchedule = supplyChain.retailShipmentSchedule;
	}

	/**
	 * Run the simulation. 
	 */
	public void runSimulation()	throws Exception {
		if (shouldPrint) {
			System.out.println("AbstractSimulator.runSimulation()");
			System.out.println(topology);
		}

		simulationStartPeriod = simulationParameters.getSimulationStartPeriod();
		simulationEndPeriod = simulationParameters.getSimulationEndPeriod();

		// create facility, demand and lead time objects
		createObjects();

		for (int t = simulationStartPeriod; t < simulationEndPeriod; ++t) {
			if (shouldPrint) {
				System.out.println("%%------------------------------------------%%");
				System.out.println("AbstractSimulator.runSimulation()");
				System.out.println("Period = " + t);
				for (Facility facility : facilityRepo.getAll()) {
					System.out.println(facility);
					for (Shipment shipment : shipmentRepo.getPipeline(facility)) {
						System.out.println(shipment);
					}
				}
				System.out.printf("%n%n");
			}

			// for each facility, log the beginning of period inventory level
			logStartOfPeriodInventory(t);

			// for each facility, submit a report to its supplier according
			// to the replenishment policy
			submitReports(t);

			// for each facility, from national to regional to retail,
			// receive shipments that are due to arrive,
			// make shipments if applicable
			receiveAndSendShipments(t);

			// demand arrives at the facility
			demandArrives(t);
		}

		if (shouldPrint) {
			SimulationResults results = new SimulationResults(this);
			results.printTrace();
		}
	}


	private void createObjects() {
		int randomStartPeriod = simulationParameters.getRandomStartPeriod();
		int randomEndPeriod = simulationParameters.getRandomEndPeriod();

		// random parameters used to create facilities, demand model,
		// and lead time models
		RandomParameters randomParameters = new RandomParameters.Builder()
				.withRandomSeed(simulationParameters.getRandomSeed())
				.withStartPeriod(randomStartPeriod)
				.withEndPeriod(randomEndPeriod)
				.build();

		buildDemand(randomParameters);		
		buildFacilitiesAndFacilityRepository(randomParameters);
		setDemandAndConsumption(randomParameters);
		setInitialInventoryLevel();
		subCreateObjects(randomParameters);
	}

	private void buildDemand(RandomParameters randomParameters) {
		demand = new DemandModel.Builder()
				.withRandomParameters(randomParameters)
				.withFacilityIds(topology.getRetailIds())
				.withSfdFactory(supplyChain.demandFactory)
				.build();
	}

	/**
	 * Build facilities and facility repository.
	 * @param randomParameters
	 */
	private void buildFacilitiesAndFacilityRepository(
			RandomParameters randomParameters) {
		// build facilities and the facility repository
		LinkedList<Facility> facilities = new LinkedList<>();
		//  - create national facility
		nationalFacility = new NationalFacility.Builder()
				.withId("nat")
				.withStartPeriod(randomParameters.startPeriod)
				.withEndPeriod(randomParameters.endPeriod)
				.build();
		facilities.add(nationalFacility);
		//  - create regional facilities
		for (String regionalFacilityId : topology.getRegionalIds()) {
			RegionalFacility facility = new RegionalFacility.Builder()
					.withId(regionalFacilityId)
					.withStartPeriod(randomParameters.startPeriod)
					.withEndPeriod(randomParameters.endPeriod)
					.build();
			facilities.add(facility);
		}
		//  - create retail facilities
		for (String retailFacilityId : topology.getRetailIds()) {
			RetailFacility facility = new RetailFacility.Builder()
					.withId(retailFacilityId)
					.withStartPeriod(randomParameters.startPeriod)
					.withEndPeriod(randomParameters.endPeriod)
					.build();
			facilities.add(facility);
		}
		//  - build facility repository
		facilityRepo = new FacilityRepository(facilities);
	}

	private void setDemandAndConsumption(RandomParameters randomParameters) {
		// for retail facilities, set the demand and consumption for
		// periods before the simulation starts to the start of the
		// simulation
		for (String retailFacilityId : topology.getRetailIds()) {
			Facility facility = facilityRepo.getFacility(retailFacilityId); 
			for (int t = randomParameters.startPeriod; t < simulationStartPeriod; ++t) {
				int d = demand.getDemand(retailFacilityId, t);
				facility.setDemand(t, d);
				facility.setConsumption(t, d);
			}
		}
		// for regional facilities, set the demand and consumption as well
		for (String retailFacilityId : topology.getRetailIds()) {
			String regionalFacilityId = topology.getRegional(retailFacilityId);
			Facility regionalFacility = facilityRepo.getFacility(regionalFacilityId);
			for (int t = randomParameters.startPeriod; t < simulationStartPeriod; ++t) {
				// the current demand at the regional facility 
				int d = regionalFacility.getDemand(t);
				d += demand.getDemand(retailFacilityId, t);
				regionalFacility.setDemand(t, d);
				regionalFacility.setConsumption(t, d);
			}
		}
	}

	/**
	 * Set the initial inventory level for regional and retail facilities.
	 */
	private void setInitialInventoryLevel() {
		int historyPeriods;
		ReportParameters params;

		// for retail facilities
		historyPeriods = 
				retailInitialInventoryLevel.getReportHistoryPeriods();
		params = new ReportParameters.Builder()
				.withCurrentPeriod(simulationStartPeriod)
				.withHistoryPeriods(historyPeriods)
				.withShipments(new LinkedList<Shipment>())
				.build();		
		for (RetailFacility retail : facilityRepo.getRetailFacilities()) {
			Report report = getReport(simulationStartPeriod, retail, 
					retailInitialInventoryLevel);
			int inv = retailInitialInventoryLevel.getOrderUpToLevel(
					0, report, null, null, null);
			retail.setInventory(inv);
		}

		// for regional facilities
		historyPeriods = 
				regionalInitialInventoryLevel.getReportHistoryPeriods();
		params = new ReportParameters.Builder()
				.withCurrentPeriod(simulationStartPeriod)
				.withHistoryPeriods(historyPeriods)
				.withShipments(new LinkedList<Shipment>())
				.build();		
		for (RegionalFacility regional : facilityRepo.getRegionalFacilities()) {
			Report report = getReport(simulationStartPeriod, regional, 
					regionalInitialInventoryLevel);
			int inv = regionalInitialInventoryLevel.getOrderUpToLevel(
					0, report, null, null, null);
			regional.setInventory(inv);
		}
	}

	/**
	 * This method is implemented a subclass to create objects
	 * that only need to be used by the subclass.
	 * @param randomParameters
	 */
	protected abstract void subCreateObjects(RandomParameters randomParameters);

	protected abstract void submitReports(int period);

	protected abstract void receiveAndSendShipments(int period) throws Exception;

	private void logStartOfPeriodInventory(int period) {
		for (Facility facility : getFacilities()) {
			facility.logStartOfPeriodInventory(period);
		}
	}

	private void demandArrives(int period) {
		for (RetailFacility retail : facilityRepo.getRetailFacilities()) {
			//System.out.printf("demand[%d] = %d\n", period,
			//		demand.getDemand(retail.getId(), period));
			retail.demandArrives(
					period, demand.getDemand(retail.getId(), period));
		}		
	}

	/**
	 * Receive shipments by incrementing the facility inventory levels. 
	 * @param shipment
	 */
	protected void receiveShipments(int period, Collection<Shipment> shipments) {
		for (Shipment shipment : shipments) {
			Facility to = shipment.to;
			int quantity = shipment.quantity;
			//System.out.println("AbstractSimulator.receiveShipments()");
			//System.out.println("Before receiving:");
			//System.out.println(to);
			to.incrementInventory(new Facility.Increment.Builder()
					.withPeriod(period)
					.withQuantity(quantity)
					.build());
			//System.out.println("After receiving:");
			//System.out.println(to);
			
			//System.out.printf("AbstractSimulator.receiveShipments(%d)\n", period);
			//System.out.println(shipment);
		}
	}

	/**
	 * Supplier makes a shipment to a facility.
	 * Check that the intended shipment is according to the shipment schedule.
	 * @param period
	 * @param shipmentDecision
	 */
	protected void makeShipment(int period, Facility supplier,
			ShipmentDecision shipmentDecision,
			AbstractShipmentSchedule thisShipmentSchedule,
			LeadTime thisLeadTime) {
		Facility to = shipmentDecision.facility;
		String customerFacilityId = to.getId();
		int quantity = shipmentDecision.quantity;

		// check that the attempted shipment does not violate the
		// shipment schedule
		if (!thisShipmentSchedule.isShipmentPeriod(to.getId(), period)) {
			System.out.println("facility to = " + to.getId());
			System.out.println("current period = " + period);
			throw new IllegalArgumentException(
					"Attempted shipment violates shipment schedule");
		}

		if (shipmentDecision.period != period) {
			throw new IllegalArgumentException(
					String.format("Shipment decision period = %d != current period = %d\n",
							shipmentDecision.period, period));
		}

		if (shouldPrint) {
			System.out.println("AbstractSimulator.makeShipment()");
			System.out.println(supplier);
		}
		// decrement the inventory of the supplier
		supplier.decrementInventory(new Facility.Increment.Builder()
				.withPeriod(period)
				.withQuantity(quantity)
				.build());

		// if there is a lead time, so that the shipment arrives
		// within the simulation horizon, then send the shipment
		if (thisLeadTime.hasLeadTime(customerFacilityId, period)) {
			// create a new shipment object
			Shipment shipment = new Shipment.Builder()
					.withPeriodReceived(period + thisLeadTime.getLeadTime(to.getId(), period))
					.withPeriodSent(period)
					.withQuantity(quantity)
					.withTo(to)
					.build();

			// add the shipment to the shipment repository
			shipmentRepo.addShipment(shipment);

			if (shouldPrint) {
			//if (true) {
				System.out.printf("AbstractSimulator.makeShipment(%d)\n", period);
				//System.out.println(shipmentDecision);
				System.out.println(to);
				System.out.println(shipment);
			}
		}
	}

	/**
	 * Customer facility submits a report to supplier facility.
	 * @param currentPeriod
	 * @param supplier
	 * @param customer
	 * @param shipmentSchedule
	 * @param replenishmentPolicy
	 */
	protected <T extends Facility, U extends LeadTime> void submitReport(
			int currentPeriod,
			Facility supplier, Facility customer,
			AbstractShipmentSchedule shipmentSchedule,
			AbstractReplenishmentPolicy<T, U> replenishmentPolicy) {
		String customerFacilityId = customer.getId();
		String supplierFacilityId = supplier.getId();

		if (isReportingPeriod(shipmentSchedule, replenishmentPolicy,
				customerFacilityId, currentPeriod)) {

			Report report = getReport(currentPeriod, customer, 
					replenishmentPolicy);

			// add the report into the report repository
			int delay = replenishmentPolicy.getReportDelay();
			int periodReceived = currentPeriod + delay;  
			reportRepo.addReport(report, supplierFacilityId, periodReceived);

			if (shouldPrint) {
				System.out.printf(
						"AbstractSimulator.submitReports(%d)\n", currentPeriod);
				System.out.println(report);
			}
		}
	}

	private <T extends Facility, U extends LeadTime> Report 
	getReport(int currentPeriod, Facility customer,
			ReportProcessor processor) {
		LeadTime leadTime = getLeadTime(customer);
		String customerId = customer.getId();
		int t = currentPeriod;
		int historyPeriods = processor.getReportHistoryPeriods();
		int forecastPeriods = processor.getReportForecastPeriods();

		// pipeline shipment information from the shipment repository
		Collection<Shipment> shipments = 
				shipmentRepo.getPipeline(customer);

		double[] futureAccessibility = new double[forecastPeriods];
		for (int i = 0; i < forecastPeriods; ++i) {
			int futurePeriod = t + i;
			futureAccessibility[i] = 
					leadTime.getAccessibility(customerId, futurePeriod);
		}

		return new Report.Builder()
				.withFrom(customer)
				.withInventory(customer.getCurrentInventoryLevel())
				.withPeriodCreated(currentPeriod)
				.withPastConsumption(customer.getPastConsumption(currentPeriod, historyPeriods))
				.withPastDemand(customer.getPastDemand(currentPeriod, historyPeriods))
				.withFutureAccessibility(futureAccessibility)
				.withShipments(shipments)
				.build();
	}

	/** 
	 * According to the shipment schedule, is the supplier facility due
	 * to make a shipment to the customer facility in the current period?
	 * If the delay is zero, then the customer facility submits a report
	 * to its supplier facility in every period.
	 * @param shipmentSchedule
	 * @param replenishmentPolicy
	 * @param facilityId
	 * @param t
	 * @return
	 */
	protected <T extends Facility, U extends LeadTime> boolean isReportingPeriod(
			AbstractShipmentSchedule shipmentSchedule,
			AbstractReplenishmentPolicy<T, U> replenishmentPolicy,
			String facilityId, int t) {
		if (replenishmentPolicy.shouldAlwaysSubmitReports()) {
			return true;
		} else {
			int delay = replenishmentPolicy.getReportDelay();
			return shipmentSchedule.isShipmentPeriod(facilityId, t + delay);
		}
	}

	Collection<Facility> getFacilities() {
		return facilityRepo.getAll();
	}

	public Facility getFacility(String facilityId) {
		return facilityRepo.getFacility(facilityId);
	}

	/**
	 * The lead time for shipments to this customer.
	 * @param customer
	 * @return
	 */
	protected abstract LeadTime getLeadTime(Facility customer);

	abstract AbstractShipmentSchedule getRetailShipmentSchedule();

	abstract LeadTime getRetailLeadTime();

}
