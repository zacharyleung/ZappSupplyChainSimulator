package com.gly.scs.replen;

import java.util.LinkedList;
import java.util.List;

import com.gly.scs.demand.AbstractDemandFactory;
import com.gly.scs.demand.ConstantDemandFactory;
import com.gly.scs.domain.Topology;
import com.gly.scs.leadtime.AbstractLeadTimeFactory;
import com.gly.scs.leadtime.ConstantLeadTimeFactory;
import com.gly.scs.leadtime.XdockLeadTimeFactory;
import com.gly.scs.sched.RegionalOffsetEntry;
import com.gly.scs.sched.RegionalShipmentSchedule;
import com.gly.scs.sched.XdockShipmentSchedule;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.XdockSimulator;

public class ClairvoyantReplenishmentPolicyTest {

	public static void main(String[] args) throws Exception {
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
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new ClairvoyantReplenishmentPolicy.Builder()
		.withShipmentSchedule(xdockShipmentSchedule)
		.withForecastHorizon(forecastHorizon)
		.withUnmetDemandCost(unmetDemandCost)
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
