package com.gly.scs.replen;

import java.util.LinkedList;
import java.util.List;

import com.gly.scs.demand.*;
import com.gly.scs.domain.Topology;
import com.gly.scs.leadtime.*;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.sched.*;
import com.gly.scs.sim.*;

public class GlyReplenishmentPolicyTest {

	public static void main(String[] args) throws Exception {
		//basicTest();
		//leadTimesTest();
		demandTest();
	}
	
	
	/**
	 * Most basic test with constant lead times and constant demands.  
	 * @param args
	 * @throws Exception
	 */
	public static void basicTest() throws Exception {
		int forecastHorizon = 48;
		double unmetDemandCost = 100;
		
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
		AbstractLeadTimeFactory primaryLeadTimeFactory =
				new ConstantLeadTimeFactory(1);
		AbstractLeadTimeFactory secondaryLeadTimeFactory =
				new ConstantLeadTimeFactory(2);
		XdockLeadTimeFactory xdockLeadTimeFactory =
				new XdockLeadTimeFactory.Builder()
		.withPrimaryLeadTimeFactory(primaryLeadTimeFactory)
		.withSecondaryLeadTimeFactory(secondaryLeadTimeFactory)
		.withTopology(retailToRegional)
		.build();
		
		// national replenishment policy
		NationalReplenishmentSchedule nationalReplenishmentPolicy =
				new NationalReplenishmentSchedule.Builder()
		.withCycle(12)
		.withHorizon(forecastHorizon)
		.withOffset(0)
		.withQuantity(1000)
		.build();
	

		// retail replenishment policy
		UnmetDemandLowerBoundFactory tangentFactory = new UnmetDemandLowerBoundPercentileFactory(7);
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new OptimizationReplenishmentFunction.Builder()
		.withShipmentSchedule(xdockShipmentSchedule)
		.withForecastHorizon(forecastHorizon)
		.withUnmetDemandCost(unmetDemandCost)
		.withDelay(oneTierReportDelay)
		.withUdlbFactory(tangentFactory)
		.build();
		
		XdockSimulator simulator = new XdockSimulator.Builder()
		.withTopology(retailToRegional)
		.withDemandFactory(demandFactory)
		.withNationalReplenishmentSchedule(nationalReplenishmentPolicy)
		.withRetailReplenishmentPolicy(retailReplenishmentPolicy)
		.withShipmentSchedule(xdockShipmentSchedule)
		.withXdockLeadTimeFactory(xdockLeadTimeFactory)
		.build();
		
		SimulationParameters simulationParameters = 
				new SimulationParameters.Builder()
		.withBeforePeriods(beforePeriods)
		.withAfterPeriods(forecastHorizon)
		.withStartPeriod(simulationStartPeriod)
		.withSimulationPeriods(simulationEndPeriod)
		.withRandomSeed(randomSeed)
		.build();
		
		simulator.runSimulation(simulationParameters);
		
		SimulationResults results = new SimulationResults(simulator);
		results.printTrace(simulationStartPeriod, simulationEndPeriod);

	}

	/**
	 * Test with random lead times and constant demands.  
	 * @param args
	 * @throws Exception
	 */
	public static void leadTimesTest() throws Exception {
		int forecastHorizon = 24;
		double unmetDemandCost = 100;
		
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
		AbstractLeadTimeFactory primaryLeadTimeFactory =
				new ConstantLeadTimeFactory(1);
		AbstractLeadTimeFactory secondaryLeadTimeFactory =
				new UniformLeadTimeFactory(4, 12);
		XdockLeadTimeFactory xdockLeadTimeFactory =
				new XdockLeadTimeFactory.Builder()
		.withPrimarySfltFactory(primaryLeadTimeFactory)
		.withSecondarySfltFactory(secondaryLeadTimeFactory)
		.withTopology(retailToRegional)
		.build();
		
		// national replenishment policy
		NationalReplenishmentSchedule nationalReplenishmentPolicy =
				new NationalReplenishmentSchedule.Builder()
		.withCycle(12)
		.withHorizon(forecastHorizon)
		.withOffset(0)
		.withQuantity(1000)
		.build();
	

		// retail replenishment policy
		UnmetDemandLowerBoundFactory tangentFactory = new UnmetDemandLowerBoundPercentileFactory(7);
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new OptimizationReplenishmentFunction.Builder()
		.withShipmentSchedule(xdockShipmentSchedule)
		.withForecastHorizon(forecastHorizon)
		.withUnmetDemandCost(unmetDemandCost)
		.withDelay(oneTierReportDelay)
		.withUdlbFactory(tangentFactory)
		.withShouldPrint(true)
		.build();
		
		XdockSimulator simulator = new XdockSimulator.Builder()
		.withTopology(retailToRegional)
		.withDemandFactory(demandFactory)
		.withNationalReplenishmentSchedule(nationalReplenishmentPolicy)
		.withRetailReplenishmentPolicy(retailReplenishmentPolicy)
		.withShipmentSchedule(xdockShipmentSchedule)
		.withXdockLeadTimeFactory(xdockLeadTimeFactory)
		.build();
		
		SimulationParameters simulationParameters = 
				new SimulationParameters.Builder()
		.withBeforePeriods(beforePeriods)
		.withAfterPeriods(forecastHorizon)
		.withStartPeriod(simulationStartPeriod)
		.withSimulationPeriods(simulationEndPeriod)
		.withRandomSeed(randomSeed)
		.build();
		
		simulator.runSimulation(simulationParameters);
		
		SimulationResults results = new SimulationResults(simulator);
		results.printTrace(simulationStartPeriod, simulationEndPeriod);

	}

	
	/**
	 * Test with constant lead times and random demands.  
	 * @param args
	 * @throws Exception
	 */
	public static void demandTest() throws Exception {
		int forecastHorizon = 24;
		double unmetDemandCost = 100;
		
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
				new UniformDemandFactory(50);
		
		// lead time factory
		AbstractLeadTimeFactory primaryLeadTimeFactory =
				new ConstantLeadTimeFactory(2);
		AbstractLeadTimeFactory secondaryLeadTimeFactory =
				new ConstantLeadTimeFactory(1);
		XdockLeadTimeFactory xdockLeadTimeFactory =
				new XdockLeadTimeFactory.Builder()
		.withPrimaryLeadTimeFactory(primaryLeadTimeFactory)
		.withSecondaryLeadTimeFactory(secondaryLeadTimeFactory)
		.withTopology(retailToRegional)
		.build();
		
		// national replenishment policy
		NationalReplenishmentSchedule nationalReplenishmentPolicy =
				new NationalReplenishmentSchedule.Builder()
		.withCycle(12)
		.withHorizon(forecastHorizon)
		.withOffset(0)
		.withQuantity(1000)
		.build();
	

		// retail replenishment policy
		UnmetDemandLowerBoundFactory tangentFactory = new UnmetDemandLowerBoundPercentileFactory(7);
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new OptimizationReplenishmentFunction.Builder()
		.withShipmentSchedule(xdockShipmentSchedule)
		.withForecastHorizon(forecastHorizon)
		.withUnmetDemandCost(unmetDemandCost)
		.withDelay(oneTierReportDelay)
		.withUdlbFactory(tangentFactory)
		.withShouldPrint(true)
		.build();
		
		XdockSimulator simulator = new XdockSimulator.Builder()
		.withTopology(retailToRegional)
		.withDemandFactory(demandFactory)
		.withNationalReplenishmentSchedule(nationalReplenishmentPolicy)
		.withRetailReplenishmentPolicy(retailReplenishmentPolicy)
		.withShipmentSchedule(xdockShipmentSchedule)
		.withXdockLeadTimeFactory(xdockLeadTimeFactory)
		.build();
		
		SimulationParameters simulationParameters = 
				new SimulationParameters.Builder()
		.withBeforePeriods(beforePeriods)
		.withAfterPeriods(forecastHorizon)
		.withStartPeriod(simulationStartPeriod)
		.withSimulationPeriods(simulationEndPeriod)
		.withRandomSeed(randomSeed)
		.build();
		
		simulator.runSimulation(simulationParameters);
		
		SimulationResults results = new SimulationResults(simulator);
		results.printTrace(simulationStartPeriod, simulationEndPeriod);

	}

	
}
