package com.gly.scs.exp.exp08;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.ConsumptionOrDemand;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.replen.OrderUpToLevel.Type;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.sim.SupplyChainFactory;
import com.gly.scs.zambia.SupplyChainFactoryFolder;
import com.gly.scs.zambia.ZambiaSimulationParameters;
import com.gly.util.FileUtils;
import com.gly.util.StringUtils;
import com.google.common.base.Stopwatch;

/**
 * Plot the efficient frontier of the four families:
 * current cross-docking policy,
 * cross-docking use last year's demand,
 * LSI policy,
 * optimization policy (good) and optimization policy (bad).
 * 
 * @author zacleung
 */
public class Exp08 {
	private static final ExperimentParameters expParameters = 
			new ExperimentParameters();
	
	private static File outputFolder;
	private static String outputFileExtension = "csv";
	private static double[] optParameters;
	private static double[] otherParameters;


	//-------------------------------------------------------------------
	// Key parameters
	//-------------------------------------------------------------------

	// real experiment
	//private static final String inputFolder = "input/zambia";
	// small test case
	private static final String inputFolder = "input/zambia-2";

	public static final int INITIAL_RANDOM_SEED = 0;
	public static final int NUMBER_OF_REPLICATIONS = 10;

	/** Ratio of supply that arrives each year to average yearly demand. */
	public static final double SDR_OF_YEARLY_SHIPMENTS = 0.7;






	//-------------------------------------------------------------------
	// Derived from key parameters
	//-------------------------------------------------------------------

	public static final double EFFECTIVE_SUPPLY_DEMAND_RATIO = 
			SDR_OF_YEARLY_SHIPMENTS + 
			expParameters.getRetailInitialInventoryPeriods() / 
			expParameters.getNumberOfTimestepsInYear() /
			expParameters.getNumberOfDataCollectionYears();
	public final static OrderUpToLevel RETAIL_INITIAL_INVENTORY_OUTL = 
			new AmiOrderUpToLevel.Builder()
			.withHistoryPeriods(48)
			.withOrderUpToPeriods(expParameters.getRetailInitialInventoryPeriods())
			.withType(Type.DEMAND)
			.build();

	public static void main(String[] args) throws Exception {
		String startTimeString = StringUtils.getTimeAsString();

		optParameters = new double[]{
				1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
				16, 20, 24, 28, 32, 36, 40, 44, 48};
		otherParameters = new double[]{
				1, 1.5, 2, 2.5, 3, 3.5, 4,
				5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
				15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
				25, 30, 35, 40, 45, 50, 55, 60};

		//		// Define the parameters to try
		//		if (String.format("%.2f", EFFECTIVE_SUPPLY_DEMAND_RATIO).equals("0.80")) {
		//			optParameters = new double[]{
		//					1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
		//					16, 20, 24, 28, 32, 36, 40, 44, 48};
		//			otherParameters = new double[]{
		//					1, 1.5, 2, 2.5, 3, 3.5, 4,
		//					5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
		//					15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
		//					25, 30, 35, 40, 45, 50, 55, 60};
		//		} else {
		//			optParameters = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
		//					11, 12, 13, 14, 16, 18, 20};
		//			otherParameters = MathUtils.seq(1, 40, 0.5);
		//		}

		outputFolder = new File(String.format(
				"exp/exp08/java/%.1f/%04d",
				EFFECTIVE_SUPPLY_DEMAND_RATIO, INITIAL_RANDOM_SEED));

		// Delete all CSV files in the output directory
		FileUtils.deleteCsvFiles(outputFolder);

		SupplyChainFactory scf = new SupplyChainFactoryFolder(inputFolder,
				expParameters.getMinimumLeadTimeValue());

		System.out.println("Starting Exp08.main()");
		printDetails(System.out);
		System.out.println();

		Stopwatch stopwatch = Stopwatch.createStarted();

		LinkedList<ParametrizedScenarioFamily> families = new LinkedList<>();
		families.addAll(getAmFamilies());
		families.addAll(getLastYearFamilies());
		families.addAll(getLsiFamilies());

		//JOptionPane.showInputDialog("WARNING: ONLY RUNNING OPTIMIZATION");
		families.addAll(getOptFamilies());

		for (ParametrizedScenarioFamily family : families) {
			System.out.println(family);

			simulate(scf, family, outputFolder, outputFileExtension);
		}

		System.out.println("Total time elapsed = " + stopwatch);
		File outputFile = new File(outputFolder, "out.txt");
		try(PrintStream out = new PrintStream(outputFile)) {
			printDetails(out);
			out.println("start time         = " + startTimeString);
			out.println("end time           = " + StringUtils.getTimeAsString());
			out.println("Total time elapsed = " + stopwatch);
			out.println("os.name            = " + System.getProperty("os.name"));
		}
	}

	public static void printDetails(PrintStream out) {
		out.printf("input folder                  = %s%n", inputFolder);
		out.printf("effective supply/demand ratio = %.1f%n",
				EFFECTIVE_SUPPLY_DEMAND_RATIO);
		out.printf("initial inventory periods     = %.0f%n", 
				expParameters.getRetailInitialInventoryPeriods());
		out.printf("# of warmup years             = %d%n", 
				expParameters.getNumberOfWarmupYears());
		out.printf("# of simulation years         = %d%n", 
				expParameters.getNumberOfDataCollectionYears());
		out.printf("initial random seed           = %d%n", INITIAL_RANDOM_SEED);
		out.printf("number of reps                = %d%n", NUMBER_OF_REPLICATIONS);
	}

	/**
	 * input/zambia
	 * 2 replications
	 * 10 parameters
	 * running time: 1 minute
	 * 
	 * input/zambia
	 * 30 replications
	 * 30 parameters
	 * running time: 45 minutes
	 */
	private static Collection<ParametrizedScenarioFamily> getAmFamilies() {
		LinkedList<ParametrizedScenarioFamily> l = new LinkedList<>();



		// The best policy
		l.add(new CurrentXdockFamily.Builder()
				.withName("amd-ip-delay0-pro")
				.withType(Type.DEMAND)
				.withMinuend(new Minuend.InventoryPosition())
				.withReportDelay(0)
				.withAllocation(new Allocation.Proportional())
				.build(otherParameters));

		//		// With AMI instead of AMD, and FCFS instead of proportional
		//		l.add(new CurrentXdockFamily.Builder()
		//				.withName("ami-ip-delay0-pro")
		//				.withType(Type.CONSUMPTION)
		//				.withMinuend(new Minuend.InventoryPosition())
		//				.withReportDelay(0)
		//				.withAllocation(new Allocation.Proportional())
		//				.build(otherParameters));
		//
		//		// With AMI instead of AMD, and FCFS instead of proportional
		//		l.add(new CurrentXdockFamily.Builder()
		//				.withName("ami-ip-delay0-fcfs")
		//				.withType(Type.CONSUMPTION)
		//				.withMinuend(new Minuend.InventoryPosition())
		//				.withReportDelay(0)
		//				.withAllocation(new Allocation.Fcfs())
		//				.build(otherParameters));
		//
		//		// With I instead of IP
		//		l.add(new CurrentXdockFamily.Builder()
		//				.withName("amd-i-delay0-pro")
		//				.withType(Type.DEMAND)
		//				.withMinuend(new Minuend.InventoryLevel())
		//				.withReportDelay(0)
		//				.withAllocation(new Allocation.Proportional())
		//				.build(otherParameters));
		//
		//		// With FCFS instead of proportional
		//		l.add(new CurrentXdockFamily.Builder()
		//				.withName("amd-ip-delay0-fcfs")
		//				.withType(Type.DEMAND)
		//				.withMinuend(new Minuend.InventoryPosition())
		//				.withReportDelay(0)
		//				.withAllocation(new Allocation.Fcfs())
		//				.build(otherParameters));
		//
		//		// With delay 1 instead of delay 0
		//		l.add(new CurrentXdockFamily.Builder()
		//				.withName("amd-ip-delay1-pro")
		//				.withType(Type.DEMAND)
		//				.withMinuend(new Minuend.InventoryPosition())
		//				.withReportDelay(1)
		//				.withAllocation(new Allocation.Proportional())
		//				.build(otherParameters));

		l.add(new CurrentXdockFamily.Builder()
				.withName("ami-i-delay1-fcfs")
				.withType(Type.CONSUMPTION)
				.withMinuend(new Minuend.InventoryLevel())
				.withReportDelay(1)
				.withAllocation(new Allocation.Fcfs())
				.build(otherParameters));

		return l;
	}

	private static Collection<ParametrizedScenarioFamily> getLsiFamilies() {
		LinkedList<ParametrizedScenarioFamily> l = new LinkedList<>();

		l.add(new LsiFamily.Builder()
				.withName("lsi-d-ip-delay0-pro")
				.withType(Type.DEMAND)
				.withMinuend(new Minuend.InventoryPosition())
				.withReportDelay(0)
				.withAllocation(new Allocation.Proportional())
				.build(otherParameters));
		l.add(new LsiFamily.Builder()
				.withName("lsi-c-i-delay1-fcfs")
				.withType(Type.CONSUMPTION)
				.withMinuend(new Minuend.InventoryLevel())
				.withReportDelay(1)
				.withAllocation(new Allocation.Fcfs())
				.build(otherParameters));

		return l;
	}

	private static Collection<ParametrizedScenarioFamily> getLastYearFamilies() {
		LinkedList<ParametrizedScenarioFamily> l = new LinkedList<>();

		//		families.add(new LastYearFamily.Builder()
		//				.withConsumptionOrDemand(ConsumptionOrDemand.CONSUMPTION)
		//				.withMinuend(new Minuend.InventoryPosition())
		//				.withReportDelay(0)
		//				.withName("lastyear-c-ip-0")
		//				.build(otherParameters));
		//		families.add(new LastYearFamily.Builder()
		//				.withConsumptionOrDemand(ConsumptionOrDemand.CONSUMPTION)
		//				.withMinuend(new Minuend.InventoryPosition())
		//				.withReportDelay(1)
		//				.withName("lastyear-c-ip-1")
		//				.build(otherParameters));
		//		families.add(new LastYearFamily.Builder()
		//				.withConsumptionOrDemand(ConsumptionOrDemand.CONSUMPTION)
		//				.withMinuend(new Minuend.InventoryLevel())
		//				.withReportDelay(0)
		//				.withName("lastyear-c-il-0")
		//				.build(otherParameters));
		l.add(new LastYearFamily.Builder()
				.withConsumptionOrDemand(ConsumptionOrDemand.CONSUMPTION)
				.withMinuend(new Minuend.InventoryLevel())
				.withReportDelay(1)
				.withName("lastyear-c-i-1-fcfs")
				.withAllocation(new Allocation.Fcfs())
				.build(otherParameters));
		l.add(new LastYearFamily.Builder()
				.withConsumptionOrDemand(ConsumptionOrDemand.DEMAND)
				.withMinuend(new Minuend.InventoryPosition())
				.withReportDelay(0)
				.withName("lastyear-d-ip-0-pro")
				.withAllocation(new Allocation.Proportional())
				.build(otherParameters));
		//		families.add(new LastYearFamily.Builder()
		//				.withConsumptionOrDemand(ConsumptionOrDemand.DEMAND)
		//				.withMinuend(new Minuend.InventoryPosition())
		//				.withReportDelay(1)
		//				.withName("lastyear-d-ip-1")
		//				.build(otherParameters));
		//		families.add(new LastYearFamily.Builder()
		//				.withConsumptionOrDemand(ConsumptionOrDemand.DEMAND)
		//				.withMinuend(new Minuend.InventoryLevel())
		//				.withReportDelay(0)
		//				.withName("lastyear-d-il-0")
		//				.build(otherParameters));
		//		families.add(new LastYearFamily.Builder()
		//				.withConsumptionOrDemand(ConsumptionOrDemand.DEMAND)
		//				.withMinuend(new Minuend.InventoryLevel())
		//				.withReportDelay(1)
		//				.withName("lastyear-d-il-1")
		//				.build(otherParameters));

		return l;
	}



	private static Collection<ParametrizedScenarioFamily> getOptFamilies() {
		LinkedList<ParametrizedScenarioFamily> l = new LinkedList<>();

		// The good and bad policies
		l.add(new OptimizationFamily.Builder()
				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
				.withForecastLevel(ExperimentParameters.FORECAST_LEVEL_GOOD)
				.withName("opt-fa-099-0-good")
				.withReportDelay(0)
				.withShipmentLeadTimePercentile(0.99)
				.build(optParameters));
		l.add(new OptimizationFamily.Builder()
				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
				.withForecastLevel(ExperimentParameters.FORECAST_LEVEL_GOOD)
				.withName("opt-fa-099-1-bad")
				.withReportDelay(1)
				.withShipmentLeadTimePercentile(0.99)
				.build(optParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-fa-095-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.95)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-fa-090-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.9)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-fa-080-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.8)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-fa-050-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.50)
		//				.build(otherParameters));
		//		
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.CONSTANT)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-c-099-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.99)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.CONSTANT)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-c-095-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.95)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.CONSTANT)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-c-090-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.9)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.CONSTANT)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-c-080-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.8)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.CONSTANT)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-c-050-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.5)
		//				.build(otherParameters));

		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_BAD)
		//				.withName("opt-fa-099-bad")
		//				.withReportDelay(1)
		//				.withShipmentLeadTimePercentile(0.99)
		//				.build(otherParameters));
		//
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-fa-050-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.5)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_BAD)
		//				.withName("opt-fa-050-bad")
		//				.withReportDelay(1)
		//				.withShipmentLeadTimePercentile(0.5)
		//				.build(otherParameters));
		//		
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_GOOD)
		//				.withName("opt-fa-001-good")
		//				.withReportDelay(0)
		//				.withShipmentLeadTimePercentile(0.01)
		//				.build(otherParameters));
		//		l.add(new OptimizationFamily.Builder()
		//				.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		//				.withForecastLevel(ExpParameters.FORECAST_LEVEL_BAD)
		//				.withName("opt-fa-001-bad")
		//				.withReportDelay(1)
		//				.withShipmentLeadTimePercentile(0.01)
		//				.build(otherParameters));

		return l;
	}


	public static void simulate(
			SupplyChainFactory scf,
			ParametrizedScenarioFamily family,
			File outputFolder,
			String outputFileExtension) 
					throws Exception {
		File outputFile = new File(outputFolder,
				String.format("%s.%s", family.getName(), outputFileExtension));

		try(PrintStream out = new PrintStream(outputFile)) {
			Stopwatch stopwatch = Stopwatch.createStarted();

			out.printf("# input folder = %s%n", inputFolder);

			System.out.println("Exp08.simulate()");
			System.out.println("family = " + family.getName());
			System.out.println(StringUtils.getTimeAsString());
			System.out.println();

			out.println("parameter,random_seed,variable,value");
			for (double parameter : family.getParameters()) {
				System.out.printf("parameter = %.2f, time = %s\n",
						parameter, StringUtils.getTimeAsString());
				for (int r = 0; r < NUMBER_OF_REPLICATIONS; ++r) {
					int randomSeed = INITIAL_RANDOM_SEED + r;

					SupplyChain supplyChain = 
							scf.getSupplyChain(SDR_OF_YEARLY_SHIPMENTS);

					SimulationParameters simulationParameters = 
							new SimulationParameters.Builder()
							.withBeforePeriods(ZambiaSimulationParameters.beforePeriods)
							.withAfterPeriods(ZambiaSimulationParameters.forecastHorizon)
							.withWarmUpPeriods(
									expParameters.getNumberOfWarmupTimesteps())
							.withStartPeriod(ZambiaSimulationParameters.startPeriod)
							.withSimulationPeriods(
									expParameters.getNumberOfDataCollectionTimesteps())
							.withRandomSeed(randomSeed)
							.build();

					AbstractScenario scenario = family.getScenario(
							parameter, expParameters);
					AbstractSimulator simulator = 
							scenario.run(supplyChain, simulationParameters);				
					SimulationResults results = new SimulationResults(simulator);

					// write the output in long form
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"service_level",
							results.getServiceLevel());
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"inventory_level",
							results.getMeanRetailInventoryLevel());
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"stddev_service_level",
							results.getStdDevOfServiceLevel());
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"wgtdstddev_service_level",
							results.getWeightedStdDevOfServiceLevel());
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"min_service_level",
							results.getMinServiceLevel());
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"maxmin_service_level",
							results.getMaxMinDiffServiceLevel());
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"gini",
							results.getGini());
				}
			}

			out.printf("# Time elapsed = %s%n", stopwatch);
		}
	}

}
