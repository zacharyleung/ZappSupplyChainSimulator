package com.gly.scs.zambia;

import java.io.File;
import java.io.PrintStream;

import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.*;
import com.gly.scs.sim.*;
import com.google.common.base.Stopwatch;

public class MainGridSearch {

	private static String inputFolder =
			//"input/test";
			//"input/test-2";
			//"input/zambia-4";
			//"input/zambia-12";
			"input/zambia";
	private static int numberOfYears = 10;
	private static String outputFolder = 
			"exp/GridSearch";

	private static double supplyDemandRatio = 100;
	private static int randomSeed = 1;
	private static int forecastLevel = 0;
	private static int reportDelay = 1;
	private static int beforePeriods = 48;
	private static int afterPeriods = 48;
	private static int warmUpPeriods = 48;
	private static int startPeriod = 0;
	
	/** Unmet demand cost array */
	private static double[] udcArray = {4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0};
	/** Shipment lead time percentile array */
	private static double[] sltpArray = {0.5, 0.75, 0.9, 0.95, 0.98};

	public static void main(String[] args) throws Exception {
		File outputFile = new File(outputFolder, "java.csv");

		try(PrintStream out = new PrintStream(outputFile)) {
			Stopwatch stopwatch = Stopwatch.createStarted();
			out.printf("# input folder = %s\n", inputFolder);
			out.printf("# number of years = %d\n", numberOfYears);
			// header line
			out.println("unmet_demand_cost,shipment_lead_time_percentile,service_level,inventory_level");
			for (double udc : udcArray) {
				for (double sltp : sltpArray) {
					simulate(out, udc, sltp);
				}
			}
			out.printf("# Time elapsed = %s%n", stopwatch);
			System.out.printf("# Time elapsed = %s%n", stopwatch);
		}
	}


	private static void simulate(PrintStream out, double udc, double sltp)
			throws Exception {
		System.out.printf("udc = %.2f\n", udc);
		System.out.printf("sltp = %.2f\n", sltp);
		System.out.println();

		Builder builder = new Builder()
		.withUnmetDemandCost(udc)
		.withShipmentLeadTimePercentile(sltp)
		.withForecastLevel(forecastLevel)
		.withReportDelay(reportDelay);

		SupplyChain supplyChain = new SupplyChainParser(inputFolder)
		.getSupplyChain(supplyDemandRatio);

		SimulationParameters simulationParameters = new SimulationParameters.Builder()
		.withBeforePeriods(beforePeriods)
		.withAfterPeriods(afterPeriods)
		.withWarmUpPeriods(warmUpPeriods)
		.withStartPeriod(startPeriod)
		.withSimulationPeriods(numberOfYears * 48)
		.withRandomSeed(randomSeed)
		.build();

		AbstractScenario scenario = new XdockScenario.Builder()
		.withName("blah")
		.withReplenishmentPolicy(builder.build())
		.withRetailInitialInventoryLevel(
				ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();

		AbstractSimulator simulator = scenario.run(supplyChain, simulationParameters);

		SimulationResults results = new SimulationResults(simulator);
		out.printf("%.2f,%.4f,%.4f,%.2f%n",
				udc,
				sltp,
				results.getServiceLevel(),
				results.getMeanRetailInventoryLevel());
		System.out.printf("%.2f,%.4f,%.4f,%.2f%n",
				udc,
				sltp,
				results.getServiceLevel(),
				results.getMeanRetailInventoryLevel());
	}

	private static class Builder {
		private int numberOfTangents = 7;
		private double unmetDemandCost;
		private double shipmentLeadTimePercentile;
		private int forecastLevel;
		private int reportDelay;

		public Builder withUnmetDemandCost(double d) {
			unmetDemandCost = d;
			return this;
		}

		public Builder withShipmentLeadTimePercentile(double d) {
			shipmentLeadTimePercentile = d;
			return this;
		}

		public Builder withForecastLevel(int i) {
			forecastLevel = i;
			return this;
		}

		public Builder withReportDelay(int i) {
			reportDelay = i;
			return this;
		}

		public XdockReplenishmentPolicy build() {

			UnmetDemandLowerBoundFactory udlbFactory =
					new UnmetDemandLowerBoundPercentileFactory(numberOfTangents);

			OptimizationReplenishmentFunction replenishmentFunction =
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

			return retailReplenishmentPolicy;
		}
	}

}
