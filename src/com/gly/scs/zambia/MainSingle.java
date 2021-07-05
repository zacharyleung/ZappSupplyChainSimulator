package com.gly.scs.zambia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;

import com.gly.scs.domain.NationalFacility;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.*;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.sim.XdockScenario;
import com.google.common.base.Stopwatch;

/**
 * Simulate a single inventory policy.
 *
 * Running time for 10 years.
 * test-2: 5 seconds
 * zambia-12: 25 seconds
 * zambia: 8 minutes
 * 
 * Running time for 10 years of the order-up-to policy:
 * zambia: 40 seconds
 * 
 * @author zacleung
 *
 */
public class MainSingle {
	private static String inputFolder = //"input/single";
			//"input/test-2";
			//"input/zambia-12";
			"input/zambia";
	private static File outputFolder = new File("output/MainSingle/java");
	private static String outputFileExtension = "dat";

	private static double supplyDemandRatio = 100;
	private static int randomSeed = 1;
	private static int forecastLevel = 0;
	private static int reportDelay = 1;
	private static int beforePeriods = 48;
	private static int afterPeriods = 48;
	private static int warmUpPeriods = 48;
	private static int startPeriod = 0;

	private static SupplyChainParser supplyChainParser;
	private static SimulationParameters simulationParameters =
			new SimulationParameters.Builder()
	.withAfterPeriods(afterPeriods)
	.withBeforePeriods(beforePeriods)
	.withRandomSeed(randomSeed)
	.withSimulationPeriods(1 * 48)
	.withStartPeriod(startPeriod)
	.withWarmUpPeriods(warmUpPeriods)
	.build(); 
	private static Stopwatch stopwatch = Stopwatch.createStarted();

	public static void main(String[] args) throws Exception {
		supplyChainParser = new SupplyChainParser(inputFolder);

		AbstractScenario scenario;
		scenario = getOptimizationScenario();
		scenario = getXdockScenario();

		System.out.println(scenario.prettyToString());

		SimulationResults results = simulateScenario(scenario);

		System.out.printf("Service level = %.3f\n",
				results.getServiceLevel());
		System.out.printf("Inventory level = %.3f\n",
				results.getMeanRetailInventoryLevel());

		System.out.println("Total time elapsed = " + stopwatch);
	}

	private static SimulationResults simulateScenario(AbstractScenario scenario) throws Exception {
		File outputFile = new File(outputFolder,
				scenario.getName() + "." + outputFileExtension);

		SimulationResults results = null;
		try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
			SupplyChain supplyChain = 
					supplyChainParser.getSupplyChain(supplyDemandRatio);

			AbstractSimulator simulator = 
					scenario.run(supplyChain, simulationParameters);
			results = new SimulationResults(simulator);
			results.printRetailTrace(out);
		}

		return results;
	}

	private static AbstractScenario getOptimizationScenario() {
		int forecastLevel = 0;
		double unmetDemandCost = 14;
		double shipmentLeadTimePercentile = 0.98;
		int numberOfTangents = 7;
		int reportDelay = 1;

		UnmetDemandLowerBoundFactory udlbFactory =
				new UnmetDemandLowerBoundPercentileFactory(numberOfTangents);

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>
		replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
		.withForecastLevel(forecastLevel)
		.withForecastHorizon(ZambiaSimulationParameters.OPTIMIZATION_REPLENISHMENT_POLICY_FORECAST_HORIZON)
		.withUnmetDemandCost(unmetDemandCost)
		.withTangentFactory(udlbFactory)
		.withShipmentLeadTimePercentile(shipmentLeadTimePercentile)
		.withHoldingCostType(ZambiaSimulationParameters.OPTIMIZATION_REPLENISHMENT_POLICY_HOLDING_COST)
		.withShipmentLeadTimeType(ZambiaSimulationParameters.OPTIMIZATION_REPLENISHMENT_POLICY_LEAD_TIME_TYPE)
		.withUnmetDemandLowerBoundType(ZambiaSimulationParameters	.UNMET_DEMAND_LOWER_BOUND_TYPE)
		.build();

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

	private static AbstractScenario getXdockScenario() {
		// order-up-to replenishment function
		AmiOrderUpToLevel orderUpToLevel = new AmiOrderUpToLevel.Builder()
		.withHistoryPeriods(12)
		.withOrderUpToPeriods(16)
		.build();

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>
		replenishmentFunction = 
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel, 
				new Minuend.InventoryPosition(),
				new Allocation.Fcfs());
		
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
