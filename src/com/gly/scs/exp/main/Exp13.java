package com.gly.scs.exp.main;

import java.io.*;
import java.util.*;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.policy.*;
import com.gly.scs.sim.*;
import com.gly.scs.zambia.SupplyChainFactoryFolder;
import com.gly.scs.zambia.ZambiaSimulationParameters;
import com.gly.util.FileUtils;
import com.gly.util.MathUtils;
import com.gly.util.StringUtils;
import com.google.common.base.Stopwatch;

/**
 * Compare the performance of different inventory replenishment policies
 * and supply chain configurations.
 *
 * @author zacharyleung
 */
public class Exp13 {
	//-------------------------------------------------------------------
	// Key parameters
	//-------------------------------------------------------------------

	/** Note the effective supply demand ratio is 0.1 + this value */
	//public static final double supplyDemandRatio = 0.7; // For 0.8
	//public static final double supplyDemandRatio = 0.8; // For 0.9
	//public static final double supplyDemandRatio = 0.9; // For 1.0
	public static final double supplyDemandRatio = 1.0; // For 1.1

	// real experiment
	private static final String inputFolder = "input/zambia";
	// small test case
	//private static final String inputFolder = "input/zambia-2";

	private static final int INITIAL_RANDOM_SEED = 10;
	private static final int NUMBER_OF_REPLICATIONS = 10;

	private static final int numberOfPhiParameters = 11;
	private static final double MINIMUM_PHI_PARAMETER = 0;
	private static final double MAXIMUM_PHI_PARAMETER = 1;
	private static double[] phiParameters;




	//-------------------------------------------------------------------
	// Other variables
	//-------------------------------------------------------------------

	private static File outputFolder = new File(String.format(
			"exp/exp13/java/%.1f/%04d", 
			supplyDemandRatio + 0.1, INITIAL_RANDOM_SEED));
	private static String outputFileExtension = "csv";
	private static SupplyChainFactory scf;
	private static SimulationParameters simulationParameters; 
	private static Stopwatch stopwatch = Stopwatch.createStarted();





	/** The main method */
	public static void main(String[] args) throws Exception {
		System.out.println("Working Directory = " +
				System.getProperty("user.dir"));

		// Create the output folder if it does not exist
		if (!outputFolder.exists()){
			outputFolder.mkdir();
		}

		// Delete all CSV files in the output directory
		FileUtils.deleteCsvFiles(outputFolder);

		phiParameters = MathUtils.getSamples(
				MINIMUM_PHI_PARAMETER, MAXIMUM_PHI_PARAMETER, numberOfPhiParameters);

		File outputFile = new File(outputFolder, "out.txt");
		try(PrintStream out = new PrintStream(outputFile)) {
			out.println("Number of replications = " + NUMBER_OF_REPLICATIONS);
			out.println("Input folder = " + inputFolder);
			out.println("Supply/demand ratios = " + Arrays.toString(phiParameters));
			out.println();
		}

		System.out.println("Number of replications = " + NUMBER_OF_REPLICATIONS);
		System.out.println("Input folder = " + inputFolder);
		System.out.println("Supply/demand ratios = " + Arrays.toString(phiParameters));

		scf = new SupplyChainFactoryFolder(inputFolder,
				ExperimentParameters.getMinimumLeadTimeValue());

		Collection<AbstractScenario> scenarios;
		scenarios = new LinkedList<AbstractScenario>();

		// Simulate delay = 0 & 1
		for (int reportDelay : new int[]{0, 1}) {
			scenarios.add(new AmiIpFcfsPolicyFactory(reportDelay).getPolicy());
			scenarios.add(new AmiIpPropPolicyFactory(reportDelay).getPolicy());
			scenarios.add(new AmdLastYearIpFcfsPolicyFactory(reportDelay).getPolicy());
			scenarios.add(new AmdLastYearIpPropPolicyFactory(reportDelay).getPolicy());
			// LSI policies
			scenarios.add(new LsiAmdIpFcfsPolicyFactory(reportDelay).getPolicy());
			scenarios.add(new LsiAmdIpPropPolicyFactory(reportDelay).getPolicy());
			scenarios.add(new LsiAmdIPropPolicyFactory(reportDelay).getPolicy());
			scenarios.add(new LsiAmiIpPropPolicyFactory(reportDelay).getPolicy());
			scenarios.add(new LsiAmiIPropPolicyFactory(reportDelay).getPolicy());
			// Optimization policy
			scenarios.add(new OptimizationPolicy(reportDelay).getPolicy());
		}

		for (AbstractScenario scenario : scenarios) {
			try(PrintStream out = new PrintStream(new FileOutputStream(outputFile, true))) {
				System.out.println(scenario.prettyToString());
				out.println(scenario.prettyToString());
				out.println();
			}

			simulateScenario(supplyDemandRatio, scenario);
		}

		try(PrintStream out = new PrintStream(new FileOutputStream(outputFile, true))) {
			out.println("Total time elapsed = " + stopwatch);
			out.println("os.name            = " + System.getProperty("os.name"));
		}
		System.out.println("Total time elapsed = " + stopwatch);
		System.out.println("os.name            = " + System.getProperty("os.name"));
	}

	private static void simulateScenario(
			double supplyDemandRatio, AbstractScenario scenario) 
					throws Exception {
		File outputFile = new File(outputFolder,
				scenario.getName() + "." + outputFileExtension);

		try(PrintStream out = new PrintStream(outputFile)) {
			Stopwatch stopwatch = Stopwatch.createStarted();

			out.printf("# input folder = %s%n", inputFolder);

			out.printf("PhiParameter,SupplyDemandRatio,RandomSeed,Variable,Value%n");
			System.out.println("ZambiaPerformance.simulateScenario()");
			System.out.printf("scenario            = %s\n", scenario.getName());
			for (double phiParameter : phiParameters) {
				System.out.printf("supply/demand ratio = %.2f, time = %s\n",
						supplyDemandRatio, StringUtils.getTimeAsString());
				for (int r = 0; r < NUMBER_OF_REPLICATIONS; ++r) {
					int randomSeed = INITIAL_RANDOM_SEED + r;
					if (randomSeed % 10 == -1) {
						System.out.printf("random seed         = %d\n", randomSeed);
						System.out.println(StringUtils.getTimeAsString());
						System.out.println();
					}

					SupplyChain supplyChain = 
							scf.getSupplyChainWithDemandAndAccessParameter(
									supplyDemandRatio, phiParameter);

					simulationParameters = new SimulationParameters.Builder()
							.withBeforePeriods(ZambiaSimulationParameters.beforePeriods)
							.withAfterPeriods(ZambiaSimulationParameters.forecastHorizon)
							.withWarmUpPeriods(
									ExperimentParameters.getNumberOfWarmupTimesteps())
							.withStartPeriod(ZambiaSimulationParameters.startPeriod)
							.withSimulationPeriods(
									ExperimentParameters.getNumberOfDataCollectionTimesteps())
							.withRandomSeed(randomSeed)
							.build();

					AbstractSimulator simulator;
					try {
						simulator =	scenario.run(supplyChain, simulationParameters);
					} catch (Exception e) {
						System.out.println("ZambiaPerformance.simulateScenario()");
						System.out.println("scenario = " + scenario.getName());
						System.out.println("supply/demand ratio = " + supplyDemandRatio);
						System.out.println("random seed = " + randomSeed);
						System.out.println("Total time elapsed = " + Exp13.stopwatch);
						throw e;
					}

					// the effective supply/demand ratio
					double esdr = supplyDemandRatio + 
							ExperimentParameters.getRetailInitialInventoryPeriods() / 
							ExperimentParameters.getNumberOfTimestepsInYear() /
							ExperimentParameters.getNumberOfDataCollectionYears();
					SimulationResults results = new SimulationResults(simulator);
					out.printf("%.2f,%.2f,%d,%s,%.4f%n",
							phiParameter, esdr, randomSeed,
							"ServiceLevel", results.getServiceLevel());
					out.printf("%.2f,%.2f,%d,%s,%.2f%n",
							phiParameter, esdr, randomSeed,
							"MeanInventoryLevel", 
							results.getMeanRetailInventoryLevel());
					out.printf("%.2f,%.2f,%d,%s,%.4f%n",
							phiParameter, esdr, randomSeed,
							"StdDevOfServiceLevel", 
							results.getStdDevOfServiceLevel());
					out.printf("%.2f,%.2f,%d,%s,%.4f%n",
							phiParameter, esdr, randomSeed,
							"WeightedStdDevOfServiceLevel", 
							results.getWeightedStdDevOfServiceLevel());
					out.printf("%.2f,%.2f,%d,%s,%.4f%n",
							phiParameter, esdr, randomSeed,
							"MinServiceLevel", 
							results.getMinServiceLevel());
					out.printf("%.2f,%.2f,%d,%s,%.4f%n",
							phiParameter, esdr, randomSeed,
							"MaxMinDiffServiceLevel", 
							results.getMaxMinDiffServiceLevel());
					out.printf("%.2f,%.2f,%d,%s,%.4f%n",
							phiParameter, esdr, randomSeed,
							"Gini", 
							results.getGini());
					out.printf("%.2f,%.2f,%d,%s,%.4f%n",
							phiParameter, esdr, randomSeed,
							"RealizedSupplyDemandRatio", 
							results.getRealizedSupplyDemandRatio());					
				}
			}

			out.printf("# Time elapsed = %s%n", stopwatch);
		}		
	}
}
