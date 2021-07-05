package com.gly.scs.exp.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.OptimizationParameters;
import com.gly.scs.exp.policy.AmdLastYearIpFcfsPolicyFactory;
import com.gly.scs.exp.policy.AmdLastYearIpPropPolicyFactory;
import com.gly.scs.exp.policy.AmiIpFcfsPolicyFactory;
import com.gly.scs.exp.policy.AmiIpPropPolicyFactory;
import com.gly.scs.exp.policy.LsiAmdIFcfsPolicyFactory;
import com.gly.scs.exp.policy.LsiAmdIPropPolicyFactory;
import com.gly.scs.exp.policy.LsiAmdIpFcfsPolicyFactory;
import com.gly.scs.exp.policy.LsiAmdIpPropPolicyFactory;
import com.gly.scs.exp.policy.LsiAmiIPropPolicyFactory;
import com.gly.scs.exp.policy.LsiAmiIpPropPolicyFactory;
import com.gly.scs.exp.policy.OptimizationPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.sim.SupplyChainFactory;
import com.gly.scs.zambia.SupplyChainFactoryFolder;
import com.gly.scs.zambia.ZambiaSimulationParameters;
import com.gly.util.FileUtils;
import com.google.common.base.Stopwatch;

/**
 * Plot a scatter plot of service level against mean lead time, where we
 * draw a point for each facility.
 * 
 * Note: 1 replication takes about 6 minutes.
 * 
 * @author znhleung
 */
public class Exp10 {
	
	//-------------------------------------------------------------------
	// Key parameters
	//-------------------------------------------------------------------

	// real experiment
	private static final String inputFolder = "input/zambia";
	// small test case
	//private static final String inputFolder = "input/zambia-2";

	private static final int INITIAL_RANDOM_SEED = 60;
	private static final int NUMBER_OF_REPLICATIONS = 90;
	/** Ratio of supply that arrives each year to average yearly demand. */
	public static final double SDR_OF_YEARLY_SHIPMENTS = 0.7;
	
	//-------------------------------------------------------------------
	// Other variables
	//-------------------------------------------------------------------
	public static final double EFFECTIVE_SUPPLY_DEMAND_RATIO = 
			SDR_OF_YEARLY_SHIPMENTS + 
			ExperimentParameters.getRetailInitialInventoryPeriods() / 
			ExperimentParameters.getNumberOfTimestepsInYear() /
			ExperimentParameters.getNumberOfDataCollectionYears();

	private static File outputFolder = new File(String.format(
			"exp/exp10/java/%04d", INITIAL_RANDOM_SEED));
	private static SupplyChainFactory scf;

	public static void main(String[] args) throws Exception {
		// Create the output folder if it does not exist
		if (!outputFolder.exists()){
			outputFolder.mkdir();
		}

		// Delete all CSV files in the output directory
		FileUtils.deleteCsvFiles(outputFolder);

		scf = new SupplyChainFactoryFolder(inputFolder,
				ExperimentParameters.getMinimumLeadTimeValue());

		Collection<AbstractScenario> scenarios;
		scenarios = new LinkedList<AbstractScenario>();
		int reportDelay;
		
		// Delay = 0
		reportDelay = 0;
		// LSI policies
		scenarios.add(new LsiAmdIFcfsPolicyFactory(reportDelay).getPolicy());
		scenarios.add(new LsiAmdIPropPolicyFactory(reportDelay).getPolicy());
		scenarios.add(new LsiAmdIpFcfsPolicyFactory(reportDelay).getPolicy());
		scenarios.add(new LsiAmdIpPropPolicyFactory(reportDelay).getPolicy());
		scenarios.add(new LsiAmiIpPropPolicyFactory(reportDelay).getPolicy());
		scenarios.add(new LsiAmiIPropPolicyFactory(reportDelay).getPolicy());
		// Current policies
		scenarios.add(new AmiIpFcfsPolicyFactory(reportDelay).getPolicy());
		scenarios.add(new AmiIpPropPolicyFactory(reportDelay).getPolicy());
		// Last year policies
		scenarios.add(new AmdLastYearIpFcfsPolicyFactory(reportDelay).getPolicy());
		scenarios.add(new AmdLastYearIpPropPolicyFactory(reportDelay).getPolicy());
		// Optimization policy
		scenarios.add(new OptimizationPolicy(reportDelay).getPolicy());

		Stopwatch stopwatch = Stopwatch.createStarted();

		for (AbstractScenario scenario : scenarios) {
			simulateScenario(scenario);
		}

		File outputFile = new File(outputFolder, "out.txt");
		System.out.printf("# Time elapsed = %s%n", stopwatch);
		try(PrintStream out = new PrintStream(new FileOutputStream(outputFile, false))) {
			out.printf( "input folder           = %s%n", inputFolder);
			out.printf( "number of lower bounds = %d%n", OptimizationParameters.NUMBER_OF_TANGETS);
			out.printf( "number of reps         = %d%n", NUMBER_OF_REPLICATIONS);
			out.println("Total time elapsed     = " + stopwatch);
			out.println("os.name                = " + System.getProperty("os.name"));
		}
	}

	private static void simulateScenario(AbstractScenario scenario) throws Exception {
		File outputFile = new File(outputFolder, scenario.getName() + ".csv");
		SimulationResults results = null;
		
		try(PrintStream out = new PrintStream(outputFile)) {
			Stopwatch stopwatch = Stopwatch.createStarted();

			out.printf("# input folder = %s%n", inputFolder);
			out.println("replication,facility,service_level,total_consumption,total_demand,total_lead_time,secondary_lead_time");

			for (int r = 0; r < NUMBER_OF_REPLICATIONS; ++r) {
				int randomSeed = INITIAL_RANDOM_SEED + r;
				System.out.println("MainScatter.simulateScenario()");
				System.out.println("scenario = " + scenario.getName());
				System.out.printf("supply/demand ratio = %.2f\n",
						EFFECTIVE_SUPPLY_DEMAND_RATIO);
				System.out.println("random seed = " + randomSeed);
				Calendar cal = Calendar.getInstance();
				cal.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				System.out.println( sdf.format(cal.getTime()) );
				System.out.println();

				SupplyChain supplyChain = scf.getSupplyChain(SDR_OF_YEARLY_SHIPMENTS);

				SimulationParameters simulationParameters = new SimulationParameters.Builder()
						.withBeforePeriods(ZambiaSimulationParameters.beforePeriods)
						.withAfterPeriods(ZambiaSimulationParameters.forecastHorizon)
						.withWarmUpPeriods(
								ExperimentParameters.getNumberOfWarmupTimesteps())
						.withStartPeriod(ZambiaSimulationParameters.startPeriod)
						.withSimulationPeriods(
								ExperimentParameters.getNumberOfDataCollectionTimesteps())
						.withRandomSeed(randomSeed)
						.build();

				AbstractSimulator simulator = 
						scenario.run(supplyChain, simulationParameters);
				results = new SimulationResults(simulator);

				for (String retailFacilityId : supplyChain.topology.getRetailIds()) {
					out.printf("%d,%s,%.4f,%d,%d,%.2f,%.2f%n",
							randomSeed,
							retailFacilityId,
							results.getServiceLevel(retailFacilityId),
							results.getTotalConsumption(retailFacilityId),
							results.getTotalDemand(retailFacilityId),
							results.getRealizedMeanTotalLeadTime(retailFacilityId),
							results.getRealizedMeanSecondaryLeadTime(retailFacilityId));
				}
			}
			out.printf("# Time elapsed = %s%n", stopwatch);
		}
		
		// For debugging, print the long trace
//		File traceFile = new File(outputFolder, "trace.txt"); 
//		try(PrintStream out = new PrintStream(traceFile)) {
//			results.printTraceLongFormat(out);
//		}
	}

}
