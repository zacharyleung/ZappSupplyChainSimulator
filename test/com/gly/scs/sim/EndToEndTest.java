package com.gly.scs.sim;

import java.util.*;

import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;

public class EndToEndTest {

	/**
	 * Test whether the XdockSimulator works.
	 * 
	 * <ul>
	 * <li>The topology is nat -- reg0 -- ret0</li>
	 * <li>The primary lead time is always 1 period.</li>
	 * <li>The secondary lead time is always 2 periods.</li>
	 * <li>The regional and retail shipment schedules are to order during
	 * periods 0, 4, 8, ...</li>
	 * <li>The demand in period t is always 10 units.</li>
	 * <li>The initial inventory level is 5 * average demand per period</li>
	 * <li>The replenishment quantity is always 30 units.</li>
     * </ul>
	 */
	public static void main(String[] args) throws Exception {
		int forecastHorizon = 24;
		int shipmentCycle = 4;
		int oneTierReportDelay = 1;
		int replenishmentQuantity = 30;
		int simulationStartPeriod = 0;
		int simulationEndPeriod = 10;
		long randomSeed = 0;
		int beforePeriods = 12;

		int historyPeriods = 10;
		int retailInitialInventoryPeriods = 5;
		
		List<Topology.Entry> topologyEntries = new LinkedList<>();
		topologyEntries.add(new Topology.Entry.Builder()
		.withRegionalFacilityId("reg0")
		.withRetailFacilityId("ret0")
		.build());
		Topology topology = new Topology(topologyEntries);

		List<RegionalOffsetEntry> regionalOffsetEntries = new LinkedList<>();
		regionalOffsetEntries.add(new RegionalOffsetEntry.Builder()
		.withOffset(0)
		.withRegionalFacilityId("reg0")
		.build());
		RegionalShipmentSchedule regionalShipmentSchedule =
				new RegionalShipmentSchedule.Builder()
		.withCycle(shipmentCycle)
		.withRegionalOffsetEntries(regionalOffsetEntries)
		.build();
		// retail shipment schedule
		AbstractShipmentSchedule retailShipmentSchedule =
				new ConstantShipmentSchedule(shipmentCycle);

		// demand factory
		SingleFacilityDemandFactory demandFactory =
				new ConstantSingleFacilityDemandFactory(topology.getRetailIds(), 10);

		// lead time factory for primary lead time
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> primaryLeadTimeEntries = new LinkedList<>();
		primaryLeadTimeEntries.add(new ConstantSingleFacilityLeadTimeFactory.Entry("reg0", 1));
		SingleFacilityLeadTimeFactory primarySfltFactory =
				new ConstantSingleFacilityLeadTimeFactory(primaryLeadTimeEntries);

		// lead time factory for secondary lead time
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> secondaryLeadTimeEntries =	new LinkedList<>();
		secondaryLeadTimeEntries.add(new ConstantSingleFacilityLeadTimeFactory.Entry("ret0", 2));
		SingleFacilityLeadTimeFactory secondarySfltFactory =
				new ConstantSingleFacilityLeadTimeFactory(secondaryLeadTimeEntries);		

		// national replenishment policy
		NationalReplenishmentSchedule nationalReplenishmentPolicy =
				new NationalReplenishmentSchedule.Builder()
		.withCycle(12)
		.withHorizon(forecastHorizon)
		.withOffset(0)
		.withQuantity(1000)
		.build();

		// retail replenishment policy
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime> 
		replenishmentFunction =
		new ConstantReplenishmentFunction<>(replenishmentQuantity);
		XdockReplenishmentPolicy xdockReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						replenishmentFunction);

		SupplyChain supplyChain = new SupplyChain.Builder()
		.withDemandFactory(demandFactory)
		.withTopology(topology)
		.withNationalReplenishmentSchedule(nationalReplenishmentPolicy)
		.withRegionalLeadTimeFactory(primarySfltFactory)
		.withRetailLeadTimeFactory(secondarySfltFactory)
		.withRegionalShipmentSchedule(regionalShipmentSchedule)
		.withRetailShipmentSchedule(retailShipmentSchedule)
		.build();

		SimulationParameters simulationParameters = 
				new SimulationParameters.Builder()
		.withBeforePeriods(beforePeriods)
		.withAfterPeriods(forecastHorizon)
		.withStartPeriod(simulationStartPeriod)
		.withSimulationPeriods(simulationEndPeriod)
		.withRandomSeed(randomSeed)
		.build();

		// initial inventory level
		OrderUpToLevel retailInitialInventoryLevel =
				new AmiOrderUpToLevel.Builder()
		.withHistoryPeriods(historyPeriods)
		.withOrderUpToPeriods(retailInitialInventoryPeriods)
		.build();
		
		XdockSimulator simulator = new XdockSimulator.Builder()
		.withSupplyChain(supplyChain)
		.withSimulationParameters(simulationParameters)
		.withReplenishmentPolicy(xdockReplenishmentPolicy)
		.withRetailInitialInventoryLevel(retailInitialInventoryLevel)
		.build();

		SimulationResults results = new SimulationResults(simulator);
		results.printTrace(simulationStartPeriod, simulationEndPeriod);
		
		System.out.printf("service level = %.1f%%%n", 
				100 * results.getServiceLevel());
		System.out.printf("mean retail inventory level = %.1f%n", 
				results.getMeanRetailInventoryLevel());
		System.out.printf("mean max retail inventory level = %.1f%n", 
				results.getMeanMaxRetailInventoryLevel());
		
		
	}

}
