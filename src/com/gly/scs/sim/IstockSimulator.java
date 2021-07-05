package com.gly.scs.sim;

import java.util.*;

import com.gly.scs.data.*;
import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;
import com.gly.scs.sim.XdockSimulator.Builder;

public class IstockSimulator extends AbstractSimulator {

	private OneTierReplenishmentPolicy retailReplenishmentPolicy;
	private OneTierReplenishmentPolicy regionalReplenishmentPolicy;

	private OneTierLeadTime regionalLeadTime;
	private OneTierLeadTime retailLeadTime;

	private OrderUpToLevel regionalInitialInventoryLevel;

	/**
	 * Create the objects in the supply chain and run the simulation
	 * according to the simulation parameters.	
	 * @param builder
	 */
	private IstockSimulator(Builder builder) throws Exception {
		super(builder.supplyChain, 
				builder.simulationParameters,
				builder.regionalInitialInventoryLevel,
				builder.retailInitialInventoryLevel);

		// this class members
		this.regionalReplenishmentPolicy = builder.regionalReplenishmentPolicy;
		this.retailReplenishmentPolicy = builder.retailReplenishmentPolicy;
		this.regionalInitialInventoryLevel = builder.regionalInitialInventoryLevel;

		runSimulation();
	}

	public static class Builder {
		private SupplyChain supplyChain;
		private OneTierReplenishmentPolicy retailReplenishmentPolicy;
		private OneTierReplenishmentPolicy regionalReplenishmentPolicy;
		private SimulationParameters simulationParameters;

		/**
		 * By default, the retail initial inventory level is zero.
		 */
		private OrderUpToLevel retailInitialInventoryLevel =
				new ZeroOrderUpToLevel();

		/**
		 * By default, the regional initial inventory level is zero.
		 */
		private OrderUpToLevel regionalInitialInventoryLevel =
				new ZeroOrderUpToLevel();

		public Builder withSupplyChain(SupplyChain supplyChain) {
			this.supplyChain = supplyChain;
			return this;
		}

		public Builder withRetailReplenishmentPolicy(
				OneTierReplenishmentPolicy retailReplenishmentPolicy) {
			this.retailReplenishmentPolicy = retailReplenishmentPolicy;
			return this;
		}

		public Builder withRegionalReplenishmentPolicy(
				OneTierReplenishmentPolicy regionalReplenishmentPolicy) {
			this.regionalReplenishmentPolicy = regionalReplenishmentPolicy;
			return this;
		}

		public Builder withRegionalInitialInventoryLevel(
				OrderUpToLevel regionalInitialInventoryLevel) {
			this.regionalInitialInventoryLevel = regionalInitialInventoryLevel;
			return this;
		}

		public Builder withRetailInitialInventoryLevel(
				OrderUpToLevel retailInitialInventoryLevel) {
			this.retailInitialInventoryLevel = retailInitialInventoryLevel;
			return this;
		}

		public Builder withSimulationParameters(
				SimulationParameters simulationParameters) {
			this.simulationParameters = simulationParameters;
			return this;
		}

		public IstockSimulator build() throws Exception { 
			return new IstockSimulator(this);
		}
	}

	@Override
	protected void subCreateObjects(RandomParameters randomParameters) {
		// build lead time models
		//  - build retail lead time model
		retailLeadTime = new OneTierLeadTime.Builder()
				.withRandomSeed(randomParameters.retailLeadTimeRandomSeed)
				.withStartPeriod(randomParameters.startPeriod)
				.withEndPeriod(randomParameters.endPeriod)
				.withSfltFactory(supplyChain.retailLeadTimeFactory)
				.withFacilityIds(topology.getRetailIds())
				.build();

		//  - build regional lead time model
		regionalLeadTime = new OneTierLeadTime.Builder()
				.withRandomSeed(randomParameters.regionalLeadTimeRandomSeed)
				.withStartPeriod(randomParameters.startPeriod)
				.withEndPeriod(randomParameters.endPeriod)
				.withSfltFactory(supplyChain.regionalLeadTimeFactory)
				.withFacilityIds(topology.getRegionalIds())
				.build();

		regionalReplenishmentPolicy.resetState();
		retailReplenishmentPolicy.resetState();
	}

	@Override
	protected void submitReports(int period) {
		for (RetailFacility retail : facilityRepo.getRetailFacilities()) {
			String regionalId = topology.getRegional(retail.getId());
			Facility regional = facilityRepo.getFacility(regionalId); 
			submitReport(period, regional, retail, 
					retailShipmentSchedule, retailReplenishmentPolicy);
		}

		for (RegionalFacility regional : facilityRepo.getRegionalFacilities()) {
			submitReport(period, nationalFacility, regional, 
					regionalShipmentSchedule, regionalReplenishmentPolicy);
		}
	}

	@Override
	protected void receiveAndSendShipments(int period) throws Exception {
		Collection<Report> reports;

		// replenish the national facility
		nationalReplenishmentSchedule.replenish(
				period, nationalFacility, shipmentRepo);

		// national facility receives shipments
		receiveShipments(period,
				shipmentRepo.getShipmentsToBeReceived(
						nationalFacility, period));

		// regional replenishment policy makes shipment decisions
		// for regional facilities
		reports = reportRepo.getReports(nationalFacility.getId(), period);
		ImmutableShipmentRepository immutableShipmentRepo =
				new ImmutableShipmentRepository(shipmentRepo);
		Collection<ShipmentDecision> shipmentDecisions =
				regionalReplenishmentPolicy.getShipmentDecisions(
						period, nationalFacility, reports,
						immutableShipmentRepo, demand, 
						regionalShipmentSchedule, regionalLeadTime);

		// simulator executes shipment decisions
		for (ShipmentDecision shipmentDecision : shipmentDecisions) {
			makeShipment(period, nationalFacility, shipmentDecision,
					regionalShipmentSchedule, regionalLeadTime);
		}

		// for each regional facility
		for (RegionalFacility regionalFacility : facilityRepo.getRegionalFacilities()) {
			// regional facility receives shipments
			receiveShipments(period,
					shipmentRepo.getShipmentsToBeReceived(
							regionalFacility, period));

			// retail replenishment policy makes shipment decisions
			// for retail facilities
			reports = reportRepo.getReports(regionalFacility.getId(), period);
			shipmentDecisions =
					retailReplenishmentPolicy.getShipmentDecisions(
							period, regionalFacility, reports,
							immutableShipmentRepo, demand, 
							retailShipmentSchedule, retailLeadTime);

			// simulator executes shipment decisions
			for (ShipmentDecision shipmentDecision : shipmentDecisions) {
				makeShipment(period, regionalFacility, shipmentDecision,
						retailShipmentSchedule, retailLeadTime);
			}

		}

		// retail facilities receive shipments
		for (RetailFacility retailFacility : facilityRepo.getRetailFacilities()) {
			receiveShipments(period,
					shipmentRepo.getShipmentsToBeReceived(
							retailFacility, period));
		}

	}

	@Override
	protected LeadTime getLeadTime(Facility customer) {
		if (customer instanceof RegionalFacility) {
			return regionalLeadTime;
		} else {
			return retailLeadTime;
		}
	}

	@Override
	AbstractShipmentSchedule getRetailShipmentSchedule() {
		return retailShipmentSchedule;
	}

	@Override
	LeadTime getRetailLeadTime() {
		return retailLeadTime;
	}

}
