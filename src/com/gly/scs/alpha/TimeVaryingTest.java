package com.gly.scs.alpha;

import java.util.LinkedList;

import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;
import com.gly.scs.sim.*;
import com.gly.scs.zambia.ZambiaSimulationParameters;

/**
 * Does the TimeVaryingReplenishmentFunction work?
 * 
 * Setup:
 * <ul>
 * <li>Topology is nat &rarr; reg &rarr; ret</li>
 * <li>One tier report delay of one period</li>
 * <li>Primary lead time is deterministically one period</li>
 * <li>Primary lead time is deterministically two periods</li>
 * <li>Initial shipments to the regional facilities on weeks 0 mod 4</li>
 * <li>Constant demand of 10 units/period</li>
 * </ul>
 * 
 * Example to illustrate sequence of events:
 * <ul>
 * <li>Retail facility submits order in period 50</li>
 * <li>National facility receives order in period 52</li>
 * <li>National facility sends shipment in period 52</li>
 * <li>Retail facility receives shipment in period 55</li>
 * </ul>
 * 
 * @author zacharyleung
 *
 */

public class TimeVaryingTest {
	private static int shipmentCycle = 4;

	public static void main(String[] args) throws Exception {
		SupplyChain supplyChain = new SupplyChain.Builder()
		.withDemandFactory(getDemandFactory())
		.withTopology(getTopology())
		.withRegionalLeadTimeFactory(getPrimarySfltFactory())
		.withRetailLeadTimeFactory(getSecondarySfltFactory())
		.withRegionalShipmentSchedule(getRegionalShipmentSchedule())
		.withRetailShipmentSchedule(getRetailShipmentSchedule())
		.withNationalReplenishmentSchedule(getNationalReplenishmentSchedule())
		.build();
		
		AbstractScenario scenario = getScenario();
		AbstractSimulator simulator = 
				scenario.run(supplyChain, getSimulationParameters());
		SimulationResults results = new SimulationResults(simulator);
		results.printRetailTrace(System.out);
	}

	private static Topology getTopology() {
		LinkedList<Topology.Entry> entries = new LinkedList<>();
		entries.add(new Topology.Entry.Builder()
		.withRegionalFacilityId("reg")
		.withRetailFacilityId("ret")
		.build());
		return new Topology(entries);
	}

	private static SingleFacilityDemandFactory getDemandFactory() {
		LinkedList<String> retailFacilityIds = new LinkedList<>();
		retailFacilityIds.add("ret");
		return new ConstantSingleFacilityDemandFactory(retailFacilityIds, 10);
	}

	private static SingleFacilityLeadTimeFactory getPrimarySfltFactory() {
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> entries = 
				new LinkedList<>();
		entries.add(new ConstantSingleFacilityLeadTimeFactory.Entry("reg", 1));
		return new ConstantSingleFacilityLeadTimeFactory(entries);
	}

	private static SingleFacilityLeadTimeFactory getSecondarySfltFactory() {
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> entries = 
				new LinkedList<>();
		entries.add(new ConstantSingleFacilityLeadTimeFactory.Entry("ret", 2));
		return new ConstantSingleFacilityLeadTimeFactory(entries);
	}

	private static RegionalShipmentSchedule getRegionalShipmentSchedule() {
		int shipmentCycle = 4;
		int offset = 0;
		LinkedList<RegionalOffsetEntry> entries = 
				new LinkedList<>();
		entries.add(new RegionalOffsetEntry.Builder()
		.withRegionalFacilityId("reg")
		.withOffset(offset)
		.build());
		return new RegionalShipmentSchedule.Builder()
		.withCycle(shipmentCycle)
		.withRegionalOffsetEntries(entries)
		.build();
	}

	private static AbstractShipmentSchedule getRetailShipmentSchedule() {
		return new ConstantShipmentSchedule(shipmentCycle);
	}

	private static NationalReplenishmentSchedule getNationalReplenishmentSchedule() {
		return new NationalReplenishmentSchedule.Builder()
				.withCycle(12)
				.withHorizon(48)
				.withOffset(0)
				.withQuantity(1000)
				.build();
	}
	
	private static AbstractScenario getScenario() {
		int oneTierReportDelay = 1;
		
		AmiOrderUpToLevel orderUpToLevel1 = new AmiOrderUpToLevel.Builder()
		.withHistoryPeriods(12)
		.withOrderUpToPeriods(10)
		.build();
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction1 =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel1);
	
		AmiOrderUpToLevel orderUpToLevel2 = new AmiOrderUpToLevel.Builder()
		.withHistoryPeriods(12)
		.withOrderUpToPeriods(20)
		.build();
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction2 =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel2);
	
		LinkedList<TimeVaryingPair<NationalFacility, XdockLeadTime>> pairs =
				new LinkedList<>();
		pairs.add(new TimeVaryingPair<NationalFacility, XdockLeadTime>
		(retailReplenishmentFunction1, 72));
		pairs.add(new TimeVaryingPair<NationalFacility, XdockLeadTime>
		(retailReplenishmentFunction2, Integer.MAX_VALUE));
		
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunctionTime =
		new TimeVaryingReplenishmentFunction<NationalFacility, XdockLeadTime>
		(pairs);
		
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunctionTime);
			
		return new XdockScenario.Builder()
		.withName("xdock-past")
		.withReplenishmentPolicy(retailReplenishmentPolicy)
		.withRetailInitialInventoryLevel(ZambiaSimulationParameters.retailInitialInventoryLevel)
		.build();
	}
	
	private static SimulationParameters getSimulationParameters() {
		return new SimulationParameters.Builder()
		.withAfterPeriods(48)
		.withBeforePeriods(48)
		.withRandomSeed(0)
		.withSimulationPeriods(48)
		.withStartPeriod(48)
		.withWarmUpPeriods(0)
		.build();
	}
	
}
