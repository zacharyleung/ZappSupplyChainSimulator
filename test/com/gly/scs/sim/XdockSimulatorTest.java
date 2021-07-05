package com.gly.scs.sim;

import java.util.*;

import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;

/**
 * Run a few basic scenarios and check that the
 * <code>XdockSimualtor</code> works correctly.
 * 
 * @author zacharyleung
 *
 */
public class XdockSimulatorTest {

	public static void main(String[] args) throws Exception {
		basicTest();
		leadTimesTest();
	}

	/**
	 * Test whether the XdockSimulator works.
	 * 
	 * <ul>
	 * <li>The topology is nat -- reg0 -- ret0</li>
	 * <li>The primary lead time is always 1 period.</li>
	 * <li>The secondary lead time is always 2 periods.</li>
	 * <li>The regional and retail shipment schedules are to order during
	 * periods 0, 4, 8, ...</li>
	 * <li>The demand in period t is t mod 20 units.</li>
	 * <li>The initial inventory level is 4 * average demand per period</li>
	 * <li>The replenishment quantity is always 30 units.</li>
     * </ul>
	 */
	public static void basicTest() throws Exception {
		int forecastHorizon = 24;
		int shipmentCycle = 4;
		int oneTierReportDelay = 1;
		int replenishmentQuantity = 30;
		int simulationStartPeriod = 0;
		int simulationEndPeriod = 24;
		long randomSeed = 0;
		int beforePeriods = 12;

		int historyPeriods = 10;
		int retailInitialInventoryPeriods = 10;
		
		List<Topology.Entry> topologyEntries = new LinkedList<>();
		topologyEntries.add(new Topology.Entry.Builder()
		.withRegionalFacilityId("reg0")
		.withRetailFacilityId("ret0")
		.build());
		Topology topology = 	new Topology(topologyEntries);

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
				new ModuloSingleFacilityDemandFactory(topology.getRetailIds(), 20);

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
		.build()	;
		
		XdockSimulator simulator = new XdockSimulator.Builder()
		.withSupplyChain(supplyChain)
		.withSimulationParameters(simulationParameters)
		.withReplenishmentPolicy(xdockReplenishmentPolicy)
		.withRetailInitialInventoryLevel(retailInitialInventoryLevel)
		.build();

		SimulationResults results = new SimulationResults(simulator);
		results.printTrace(simulationStartPeriod, simulationEndPeriod);
	}

	/**
	 * Test whether the XdockSimulator works with:
	 *  - a single retail facility
	 *  - uniform lead times
	 *  - constant demands
	 *  - constant replenishment
	 */
	public static void leadTimesTest() throws Exception {
		/*
		int forecastHorizon = 24;
		int oneTierReportDelay = 1;
		int simulationStartPeriod = 0;
		int simulationEndPeriod = 24;
		long randomSeed = 0;
		int beforePeriods = 0;

		List<Topology.Entry> retailToRegionalEntries = new LinkedList<>();
		retailToRegionalEntries.add(new Topology.Entry.Builder()
		.withRegionalFacilityId("reg0")
		.withRetailFacilityId("ret0")
		.build());
		Topology retailToRegional =
				new Topology(retailToRegionalEntries);

		List<RegionalOffsetEntry> regionalOffsetEntries = new LinkedList<>();
		regionalOffsetEntries.add(new RegionalOffsetEntry.Builder()
		.withOffset(0)
		.withRegionalFacilityId("reg0")
		.build());
		RegionalShipmentSchedule regionalShipmentSchedule =
				new RegionalShipmentSchedule.Builder()
		.withCycle(4)
		.withRegionalOffsetEntries(regionalOffsetEntries)
		.build();

		XdockShipmentSchedule xdockShipmentSchedule =
				new XdockShipmentSchedule.Builder()
		.withRegionalShipmentSchedule(regionalShipmentSchedule)
		.withTopology(retailToRegional)
		.build();

		// demand factory
		AbstractDemandFactory demandFactory =
				new ConstantDemandFactory(10);

		// lead time factory
		SingleFacilityLeadTimeFactory primarySfltFactory =
				new ConstantSingleFacilityLeadTimeFactory(1);
		LinkedList<UniformSingleFacilityLeadTimeFactory.Entry> entries =
				new LinkedList<UniformSingleFacilityLeadTimeFactory.Entry>();
		entries.add(new UniformSingleFacilityLeadTimeFactory.Entry.Builder()
		.withFacilityId("ret0")
		.withMin(2)
		.withMax(10)
		.build());
		SingleFacilityLeadTimeFactory secondarySfltFactory =
				new UniformSingleFacilityLeadTimeFactory(entries);
		XdockLeadTimeFactory xdockLeadTimeFactory =
				new XdockLeadTimeFactory.Builder()
		.withPrimarySfltFactory(primarySfltFactory)
		.withSecondarySfltFactory(secondarySfltFactory)
		.withTopology(retailToRegional)
		.build();

		// national replenishment policy
		NationalReplenishmentPolicy nationalReplenishmentPolicy =
				new NationalReplenishmentPolicy.Builder()
		.withCycle(12)
		.withHorizon(forecastHorizon)
		.withOffset(0)
		.withQuantity(1000)
		.build();


		// retail replenishment policy
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new ConstantXdockReplenishmentPolicy.Builder()
		.withQuantity(30)
		.withShipmentSchedule(xdockShipmentSchedule)
		.withOneTierReportDelay(oneTierReportDelay)
		.build();

		XdockSimulator simulator = new XdockSimulator.Builder()
		.withTopology(retailToRegional)
		.withDemandFactory(demandFactory)
		.withNationalReplenishmentPolicy(nationalReplenishmentPolicy)
		.withRetailReplenishmentPolicy(retailReplenishmentPolicy)
		.withShipmentSchedule(xdockShipmentSchedule)
		.withXdockLeadTimeFactory(xdockLeadTimeFactory)
		.build();

		SimulationParameters simulationParameters = 
				new SimulationParameters.Builder()
		.withBeforePeriods(beforePeriods)
		.withAfterPeriods(forecastHorizon)
		.withSimulationStartPeriod(simulationStartPeriod)
		.withSimulationEndPeriod(simulationEndPeriod)
		.withRandomSeed(randomSeed)
		.build();

		simulator.runSimulation(simulationParameters);

		SimulationResults results = new SimulationResults(simulator);
		results.printTrace(simulationStartPeriod, simulationEndPeriod);
		*/
	}
}
