package com.gly.scs.sim;

import java.util.*;

import com.gly.scs.data.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;

public class XdockSimulator extends AbstractSimulator {

	private XdockReplenishmentPolicy replenishmentPolicy;
	private XdockLeadTime leadTime;
	private XdockShipmentSchedule xdockShipmentSchedule;
	
	private XdockSimulator(Builder builder) throws Exception {
		super(builder.supplyChain, 
				builder.simulationParameters,
				builder.regionalInitialInventoryLevel,
				builder.retailInitialInventoryLevel);
		
		// this class members
		this.replenishmentPolicy = builder.replenishmentPolicy;
		
		runSimulation();
	}

	public static class Builder {
		private SupplyChain supplyChain;
		private SimulationParameters simulationParameters;
		private XdockReplenishmentPolicy replenishmentPolicy;
		
		/**
		 * By default, the retail initial inventory level is zero.
		 */
		private OrderUpToLevel retailInitialInventoryLevel =
				new ZeroOrderUpToLevel();

		/**
		 * The regional initial inventory level is always zero.
		 */
		private OrderUpToLevel regionalInitialInventoryLevel =
				new ZeroOrderUpToLevel();

		public Builder withSupplyChain(SupplyChain supplyChain) {
			this.supplyChain = supplyChain;
			return this;
		}
		
		public Builder withSimulationParameters(
				SimulationParameters simulationParameters) {
			this.simulationParameters = simulationParameters;
			return this;
		}
		
		public Builder withReplenishmentPolicy(
				XdockReplenishmentPolicy replenishmentPolicy) {
			this.replenishmentPolicy = replenishmentPolicy;
			return this;
		}
	
		public Builder withRetailInitialInventoryLevel(
				OrderUpToLevel retailInitialInventoryLevel) {
			this.retailInitialInventoryLevel = retailInitialInventoryLevel;
			return this;
		}

		public XdockSimulator build() throws Exception {
			return new XdockSimulator(this);
		}
	}

	/**
	 * The objects that this class needs to create are:
	 *  - the cross-docking lead time model
	 */
	@Override
	protected void subCreateObjects(RandomParameters randomParameters) {
		// build cross-docking shipment schedule
		xdockShipmentSchedule =
				new XdockShipmentSchedule.Builder()
		.withRegionalShipmentSchedule(regionalShipmentSchedule)
		.withTopology(topology)
		.build();
		
		// build cross-docking lead time model
		XdockLeadTimeFactory xdockLeadTimeFactory =
				new XdockLeadTimeFactory.Builder()
		.withPrimarySfltFactory(supplyChain.regionalLeadTimeFactory)
		.withSecondarySfltFactory(supplyChain.retailLeadTimeFactory)
		.withTopology(topology)
		.build();
		leadTime = xdockLeadTimeFactory.build(randomParameters);
		
		replenishmentPolicy.resetState();
	}

	@Override
	protected void submitReports(int period) {
		for (RetailFacility retail : facilityRepo.getRetailFacilities()) {
			submitReport(period, nationalFacility, retail,
					xdockShipmentSchedule, replenishmentPolicy);
		}
	}

	@Override
	protected void receiveAndSendShipments(int period) throws Exception {
		String nationalFacilityId = nationalFacility.getId();
		
		// replenish the national facility
		nationalReplenishmentSchedule.replenish(
				period, nationalFacility, shipmentRepo);

		// national facility receives shipments
		receiveShipments(period,
				shipmentRepo.getShipmentsToBeReceived(
						nationalFacility, period));

		// retail replenishment policy makes shipment decisions
		// for retail facilities
		Collection<Report> reports = reportRepo.getReports(
				nationalFacilityId, period);
		ImmutableShipmentRepository immutableShipmentRepo =
				new ImmutableShipmentRepository(shipmentRepo);
		Collection<ShipmentDecision> shipmentDecisions =
				replenishmentPolicy.getShipmentDecisions(
						period, nationalFacility, reports,
						immutableShipmentRepo, demand, 
						xdockShipmentSchedule, leadTime);

		// simulator executes shipment decisions
		for (ShipmentDecision shipmentDecision : shipmentDecisions) {
			makeShipment(period, nationalFacility, shipmentDecision,
					xdockShipmentSchedule, leadTime);
		}

		// retail facilities receive shipments
		for (RetailFacility retailFacility : facilityRepo.getRetailFacilities()) {
			Collection<Shipment> shipments = shipmentRepo.getShipmentsToBeReceived(
					retailFacility, period);
			receiveShipments(period, shipments);

			//System.out.println("XdockSimulator.receiveAndSendShipments()");
			//for (Shipment shipment : shipments) {
			//	System.out.println(shipment);
			//}
		}

	}

	@Override
	protected LeadTime getLeadTime(Facility customer) {
		return leadTime;
	}

	@Override
	AbstractShipmentSchedule getRetailShipmentSchedule() {
		return xdockShipmentSchedule;
	}

	@Override
	LeadTime getRetailLeadTime() {
		return leadTime;
	}

}
