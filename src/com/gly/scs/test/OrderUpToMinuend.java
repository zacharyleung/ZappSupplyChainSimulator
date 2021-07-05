
package com.gly.scs.test;

import java.util.LinkedList;

import com.gly.scs.demand.ConstantSingleFacilityDemandFactory;
import com.gly.scs.demand.SingleFacilityDemandFactory;
import com.gly.scs.domain.NationalFacility;
import com.gly.scs.domain.Topology;
import com.gly.scs.leadtime.ConstantSingleFacilityLeadTimeFactory;
import com.gly.scs.leadtime.SingleFacilityLeadTimeFactory;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.replen.*;
import com.gly.scs.sched.ConstantShipmentSchedule;
import com.gly.scs.sched.RegionalOffsetEntry;
import com.gly.scs.sched.RegionalShipmentSchedule;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.sim.XdockScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

/**
 * Test whether the the OrderUpToReplenishmentFunction correctly 
 * implements the choice of minuend as inventory level or inventory
 * position.
 * 
 * @author zacleung
 *
 */
public class OrderUpToMinuend {
	private static int shipmentCycle = 4;
	private static int reportDelay = 1;
	private static int primaryLeadTime = 1;
	private static int secondaryLeadTime = 5;
	private static Minuend inventoryLevel = 
			new Minuend.InventoryLevel();
	private static Minuend inventoryPosition = 
			new Minuend.InventoryPosition();
	private static Minuend minuend = inventoryPosition;

	public static void main(String[] args) throws Exception {	
		Topology topology = getTopology();

		SingleFacilityDemandFactory demandFactory = 
				new ConstantSingleFacilityDemandFactory(
						topology.getRetailIds(), 10);

		NationalReplenishmentSchedule nationalReplenishmentSchedule =
				new NationalReplenishmentSchedule.Builder()
				.withCycle(12)
				.withHorizon(48)
				.withOffset(0)
				.withQuantity(1000)
				.build();

		SupplyChain supplyChain = new SupplyChain.Builder()
				.withDemandFactory(demandFactory)
				.withTopology(topology)
				.withRegionalLeadTimeFactory(
						getRegionalLeadTimeFactory(topology))
				.withRetailLeadTimeFactory(
						getRetailLeadTimeFactory(topology))
				.withRegionalShipmentSchedule(
						getRegionalShipmentSchedule(topology))
				.withRetailShipmentSchedule(
						new ConstantShipmentSchedule(shipmentCycle))
				.withNationalReplenishmentSchedule(nationalReplenishmentSchedule)
				.build();

		AbstractScenario scenario = getXdockScenario();
		
		SimulationParameters simulationParameters =
				new SimulationParameters.Builder()
		.withAfterPeriods(48)
		.withBeforePeriods(48)
		.withRandomSeed(0)
		.withSimulationPeriods(1 * 48)
		.withStartPeriod(0)
		.withWarmUpPeriods(0)
		.build(); 
		
		AbstractSimulator simulator = 
				scenario.run(supplyChain, simulationParameters);
		SimulationResults results = new SimulationResults(simulator);
		results.printRetailTrace(System.out);
	}

	private static Topology getTopology() {
		LinkedList<Topology.Entry> entries = new LinkedList<>();
		entries.add(new Topology.Entry.Builder()
				.withRegionalFacilityId("region")
				.withRetailFacilityId("retail")
				.build());
		return new Topology(entries);
	}

	private static SingleFacilityLeadTimeFactory
	getRegionalLeadTimeFactory(Topology topology) {
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> entries =
				new LinkedList<>();
		for (String regional : topology.getRegionalIds()) {
			entries.add(new ConstantSingleFacilityLeadTimeFactory.Entry(
					regional, primaryLeadTime));
		}
		return new ConstantSingleFacilityLeadTimeFactory(entries);
	}

	private static SingleFacilityLeadTimeFactory
	getRetailLeadTimeFactory(Topology topology) {
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> entries =
				new LinkedList<>();
		for (String retail : topology.getRetailIds()) {
			entries.add(new ConstantSingleFacilityLeadTimeFactory.Entry(
					retail, secondaryLeadTime));
		}
		return new ConstantSingleFacilityLeadTimeFactory(entries);
	}

	private static RegionalShipmentSchedule
	getRegionalShipmentSchedule(Topology topology) {
		LinkedList<RegionalOffsetEntry> entries =
				new LinkedList<>();
		for (String regional : topology.getRegionalIds()) {
			entries.add(new RegionalOffsetEntry.Builder()
					.withRegionalFacilityId(regional)
					.withOffset(0)
					.build());
		}
		return new RegionalShipmentSchedule.Builder()
				.withCycle(shipmentCycle)
				.withRegionalOffsetEntries(entries)
				.build();
	}

	private static AbstractScenario getXdockScenario() {
		// order-up-to replenishment function
		AmiOrderUpToLevel orderUpToLevel = new AmiOrderUpToLevel.Builder()
		.withHistoryPeriods(12)
		.withOrderUpToPeriods(16)
		.build();

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>
		replenishmentFunction = 
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel, minuend, new Allocation.Fcfs());
		
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(reportDelay,
						replenishmentFunction);

		return new XdockScenario.Builder()
		.withName("blah")
		.withReplenishmentPolicy(retailReplenishmentPolicy)
		.withRetailInitialInventoryLevel(
				ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}

}
