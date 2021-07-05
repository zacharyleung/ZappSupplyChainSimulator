package com.gly.scs.zambia;

/**
 * NUMBER_OF_YEARS = 10
 * Running time for zambia-12: 35 s
 * Running time for zambia: 16 min
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;

import com.gly.scs.domain.NationalFacility;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.AbstractReplenishmentFunction;
import com.gly.scs.replen.ForecastOrderUpToLevel;
import com.gly.scs.replen.OptimizationReplenishmentFunction;
import com.gly.scs.replen.OrderUpToReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.sim.XdockScenario;
import com.google.common.base.Stopwatch;

public class MainWarmUp {
	private static String inputFolder = //"input/single";
			//"input/test-2";
			//"input/zambia-12";
			"input/zambia";

	private static final int NUMBER_OF_YEARS = 10;
	private static final int PERIODS_PER_YEAR = 48;
	
	private static File outputFolder = new File("output/MainWarmUp");
	private static String outputFileExtension = "dat";

	private static double supplyDemandRatio = 1.0;
	
	private static SupplyChainParser supplyChainParser;
	private static SimulationParameters simulationParameters =
			new SimulationParameters.Builder()
	.withAfterPeriods(48)
	.withBeforePeriods(48)
	.withRandomSeed(0)
	.withSimulationPeriods(PERIODS_PER_YEAR * NUMBER_OF_YEARS)
	.withStartPeriod(48)
	.withWarmUpPeriods(0)
	.build();

	// cross-docking policy
	private static final double ORDER_UP_TO_PERIODS = 24;
	
	// optimization policy
	private static final double unmetDemandCost = 24;
	private static final int oneTierReportDelay = 0;
	private static final int forecastLevel = 
			ZambiaSimulationParameters.FORECAST_LEVEL_INDUSTRY; 
	private static final int NUMBER_OF_TANGENTS = 11;
	private static final double SHIPMENT_LEAD_TIME_PERCENTILE = 0.998;
	private static final HoldingCostType HOLDING_COST_TYPE =
			HoldingCostType.FACILITY_ACCESSIBILITY;

	private static Stopwatch stopwatch = Stopwatch.createStarted();

	public static void main(String[] args) throws Exception {
		supplyChainParser = new SupplyChainParser(inputFolder);

		Collection<AbstractScenario> scenarios = getScenarios();
		File outputFile = new File(outputFolder, "time.dat");
		for (AbstractScenario scenario : scenarios) {
			try(PrintStream out = new PrintStream(new FileOutputStream(outputFile, true))) {
				System.out.println(scenario.prettyToString());
				out.println(scenario.prettyToString());
				out.println();
			}

			simulateScenario(scenario);
		}

		try(PrintStream out = new PrintStream(new FileOutputStream(outputFile, true))) {
			out.println("Total time elapsed = " + stopwatch);
		}
		System.out.println("Total time elapsed = " + stopwatch);
	}

	private static Collection<AbstractScenario> getScenarios() throws Exception {
		LinkedList<AbstractScenario> scenarios = new LinkedList<>();
		scenarios.add(getXdockForecastScenario());
		scenarios.add(getOptimizationScenario());
		return scenarios;
	}
	
	private static AbstractScenario getXdockForecastScenario() {
		ForecastOrderUpToLevel orderUpToLevel = 
				new ForecastOrderUpToLevel.Builder()
		.withForecastLevel(forecastLevel)
		.withForecastPeriods(ORDER_UP_TO_PERIODS)
		.build();

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel);

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunction);

		String name = "xdock-forecast";
		return new XdockScenario.Builder()
		.withName(name)
		.withReplenishmentPolicy(retailReplenishmentPolicy)
		.withRetailInitialInventoryLevel(
				ZambiaSimulationParameters.retailInitialInventoryLevel)
		.build();
	}

	private static AbstractScenario getOptimizationScenario() {
		UnmetDemandLowerBoundFactory tangentFactory =
				new UnmetDemandLowerBoundPercentileFactory(NUMBER_OF_TANGENTS);

		OptimizationReplenishmentFunction replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
		.withForecastLevel(forecastLevel)
		.withForecastHorizon(ZambiaSimulationParameters.
				OPTIMIZATION_REPLENISHMENT_POLICY_FORECAST_HORIZON)
				.withUnmetDemandCost(unmetDemandCost)
				.withTangentFactory(tangentFactory)
				.withHoldingCostType(HOLDING_COST_TYPE)
				.withShipmentLeadTimePercentile(SHIPMENT_LEAD_TIME_PERCENTILE)
				.withShipmentLeadTimeType(ZambiaSimulationParameters.
						OPTIMIZATION_REPLENISHMENT_POLICY_LEAD_TIME_TYPE)
				.withUnmetDemandLowerBoundType(ZambiaSimulationParameters
						.UNMET_DEMAND_LOWER_BOUND_TYPE)
				.build();

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						replenishmentFunction);

		String name = "optimization";
		return new XdockScenario.Builder()
		.withName(name)
		.withReplenishmentPolicy(retailReplenishmentPolicy)
		.withRetailInitialInventoryLevel(
				ZambiaSimulationParameters.retailInitialInventoryLevel)
		.build();
	}

	private static void simulateScenario(AbstractScenario scenario) throws Exception {
		File outputFile = new File(outputFolder,
				scenario.getName() + "." + outputFileExtension);

		try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
			SupplyChain supplyChain = 
					supplyChainParser.getSupplyChain(supplyDemandRatio);

			AbstractSimulator simulator = 
					scenario.run(supplyChain, simulationParameters);
			SimulationResults results = new SimulationResults(simulator);
			
			out.println("year,service level");
			for (int i = 0; i < NUMBER_OF_YEARS; ++i) {
				int theStartPeriod = i * PERIODS_PER_YEAR;
				int theEndPeriod = (i + 1) * PERIODS_PER_YEAR;
				out.printf("%d,%.3f%n", i, results.getServiceLevel(theStartPeriod, theEndPeriod));
			}
		}
	}

}
