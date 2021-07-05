package com.gly.scs.test;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.gly.scs.demand.AbstractDemandFactory;
import com.gly.scs.demand.MmfeLevelsSingleFacilityDemandFactory;
import com.gly.scs.demand.SingleFacilityDemandFactory;
import com.gly.scs.demand.MmfeLevelsSingleFacilityDemandFactory.Builder;
import com.gly.scs.domain.NationalFacility;
import com.gly.scs.domain.Topology;
import com.gly.scs.domain.Topology.Entry;
import com.gly.scs.exp.param.ExpParameters;
import com.gly.scs.exp.param.ExpParameters.Optimization;
import com.gly.scs.leadtime.ConstantSingleFacilityLeadTimeFactory;
import com.gly.scs.leadtime.SingleFacilityLeadTimeFactory;
import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTimeFactory;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTime.MinimumValue;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.AbstractReplenishmentFunction;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.NationalReplenishmentSchedule;
import com.gly.scs.replen.OptimizationReplenishmentFunction;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.replen.OrderUpToLevelConstant;
import com.gly.scs.replen.OrderUpToReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sched.ConstantShipmentSchedule;
import com.gly.scs.sched.RegionalOffsetEntry;
import com.gly.scs.sched.RegionalShipmentSchedule;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.sim.XdockScenario;
import com.gly.scs.zambia.MmfeDemandParser;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class LowAccessibilityTest {

	private static MinimumValue minimumValue = MinimumValue.ONE;
	private static int shipmentCycle = 4;

	public static void main(String[] args) throws Exception {
		SupplyChain supplyChain = getSupplyChain();
		AbstractScenario scenario = null;
		scenario = getOptimizationScenario();
		scenario = getOrderUpToScenario();

		SimulationParameters simulationParameters = new SimulationParameters.Builder()
		.withBeforePeriods(12)
		.withAfterPeriods(24)
		.withWarmUpPeriods(0)
		.withStartPeriod(0)
		.withSimulationPeriods(48)
		.withRandomSeed(0)
		.build();

		AbstractSimulator simulator = 
				scenario.run(supplyChain, simulationParameters);

		scenario.run(supplyChain, simulationParameters);
		
		SimulationResults result = new SimulationResults(simulator);
		result.printRetailTrace(System.out);
		System.out.printf("mean retail inventory level = %.2f\n",
				result.getMeanRetailInventoryLevel());
	}

	private static SupplyChain getSupplyChain() {
		LinkedList<Entry> entries = 
				new LinkedList<>();
		entries.add(new Entry.Builder()
				.withRegionalFacilityId("regional")
				.withRetailFacilityId("retail")
				.build());
		Topology topology = new Topology(entries);

		// create the regional shipment schedule object
		LinkedList<RegionalOffsetEntry> regionalOffsetEntries =
				new LinkedList<>();
		regionalOffsetEntries.add(
				new RegionalOffsetEntry.Builder()
				.withOffset(0)
				.withRegionalFacilityId("regional")
				.build());
		RegionalShipmentSchedule regionalShipmentSchedule =
				new RegionalShipmentSchedule.Builder()
				.withCycle(shipmentCycle)
				.withRegionalOffsetEntries(regionalOffsetEntries)
				.build();

		return new SupplyChain.Builder()
				.withDemandFactory(getDemandFactory())
				.withRegionalLeadTimeFactory(getPrimaryLeadTimeFactory())
				.withRetailLeadTimeFactory(getSecondaryLeadTimeFactory())
				.withRegionalShipmentSchedule(regionalShipmentSchedule)
				.withRetailShipmentSchedule(
						new ConstantShipmentSchedule(shipmentCycle))
				.withTopology(topology)
				.withNationalReplenishmentSchedule(
						getNationalReplenishmentSchedule())
				.build();
	}

	private static SingleFacilityDemandFactory getDemandFactory() {
		LinkedList<MmfeLevelsSingleFacilityDemandFactory.Entry>
		entries = new LinkedList<>();
		entries.add(new MmfeLevelsSingleFacilityDemandFactory.Entry(
				"retail", new double[] {10}));
		return new MmfeLevelsSingleFacilityDemandFactory.Builder()
				.withEntries(entries)
				.withStandardDeviation(new double[] {0.5, 0.3, 0.1})
				.withVarianceProportion(new double[] {1})
				.build();
	}

	private static SingleFacilityLeadTimeFactory getPrimaryLeadTimeFactory() {
		List<ConstantSingleFacilityLeadTimeFactory.Entry>
		entries = new LinkedList<>();
		entries.add(
				new ConstantSingleFacilityLeadTimeFactory.Entry(
						"regional", 1));
		return new ConstantSingleFacilityLeadTimeFactory(entries);
	}

	private static SingleFacilityLeadTimeFactory getSecondaryLeadTimeFactory() {
		// lead time factory for secondary lead time
		LinkedList<VehicleAccessibilitySingleFacilityLeadTimeFactory.Entry>
		secondaryLeadTimeEntries =	new LinkedList<>();
		double mean = 2.5;
		double[] accessibility = {
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0.75, 0.75, 0.75, 0.75, 0.5, 0.5, 0.5, 0.5
				};
		secondaryLeadTimeEntries.add(
				new VehicleAccessibilitySingleFacilityLeadTimeFactory.Entry.Builder()
				.withFacilityId("retail")
				.withMean(mean)
				.withAccessibility(accessibility)
				.build());
		return new VehicleAccessibilitySingleFacilityLeadTimeFactory(
				secondaryLeadTimeEntries, minimumValue);
	}

	private static NationalReplenishmentSchedule 
	getNationalReplenishmentSchedule() {
		return new NationalReplenishmentSchedule.Builder()
				.withCycle(12)
				.withHorizon(12)
				.withOffset(0)
				.withQuantity(1000)
				.build();
	}

	private static AbstractScenario getOptimizationScenario() {
		UnmetDemandLowerBoundFactory udlbFactory =
				new UnmetDemandLowerBoundPercentileFactory(2);

		OptimizationReplenishmentFunction replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
				.withForecastLevel(0)
				.withForecastHorizon(24)
				.withUnmetDemandCost(8)
				.withHoldingCostType(Optimization.HOLDING_COST_TYPE)
				.withTangentFactory(udlbFactory)
				.withShipmentLeadTimePercentile(
						Optimization.LEAD_TIME_PERCENTILE)
				.withShipmentLeadTimeType(Optimization.LEAD_TIME_TYPE)
				.withUnmetDemandLowerBoundType(
						Optimization.UNMET_DEMAND_LOWER_BOUND_TYPE)
				.build();

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(0,
						replenishmentFunction);

		return new XdockScenario.Builder()
				.withName(Optimization.NAME)
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}

	private static AbstractScenario getOrderUpToScenario() {
		OrderUpToLevel orderUpToLevel = new OrderUpToLevelConstant(120); 
		int reportDelay = 0;
		Minuend minuend = new Minuend.InventoryPosition();
		//minuend = new Minuend.InventoryLevel();
		
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel,
				minuend, 
				new Allocation.Proportional());

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(reportDelay,
						retailReplenishmentFunction);

		return new XdockScenario.Builder()
				.withName(orderUpToLevel.toString())
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ExpParameters.RETAIL_INITIAL_INVENTORY_LEVEL)
				.build();

	}
	
}
