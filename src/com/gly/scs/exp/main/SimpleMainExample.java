package com.gly.scs.exp.main;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.policy.*;
import com.gly.scs.sim.*;
import com.gly.scs.zambia.SupplyChainFactoryFolder;
import com.gly.scs.zambia.ZambiaSimulationParameters;
import com.google.common.base.Stopwatch;

/**
 * Run a simple main example of the supply chain simulator.
 *
 * @author zacharyleung
 */
public class SimpleMainExample {
	//-------------------------------------------------------------------
	// Key parameters
	//-------------------------------------------------------------------
	private static final String inputFolder = "input/zambia-2";
	private static final double supplyDemandRatio = 0.8;
	private static final int reportDelay = 0;
	private static final long randomSeed = 0;

	//-------------------------------------------------------------------
	// Useful variables
	//-------------------------------------------------------------------	
	private static SupplyChainFactory scf;
	private static SimulationParameters simulationParameters; 
	private static Stopwatch stopwatch = Stopwatch.createStarted();

	public static void main(String[] args) throws Exception {
		scf = new SupplyChainFactoryFolder(inputFolder,
				ExperimentParameters.getMinimumLeadTimeValue());

		AbstractScenario scenario = 
				new AmiIpPropPolicyFactory(reportDelay).getPolicy();
		simulateScenario(scenario);

		System.out.println("Total time elapsed = " + stopwatch);
		System.out.println("os.name            = " + System.getProperty("os.name"));
	}

	private static void simulateScenario(AbstractScenario scenario) throws Exception {
		SupplyChain supplyChain = 
				scf.getSupplyChain(supplyDemandRatio);

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
			throw e;
		}

		// the effective supply/demand ratio
		double esdr = supplyDemandRatio + 
				ExperimentParameters.getRetailInitialInventoryPeriods() / 
				ExperimentParameters.getNumberOfTimestepsInYear() /
				ExperimentParameters.getNumberOfDataCollectionYears();
		SimulationResults results = new SimulationResults(simulator);

		System.out.printf("Effective supply/demand ratio = %.2f\n", esdr);
		System.out.printf("Realized service level        = %.2f\n",
				results.getServiceLevel());
		System.out.printf("Mean retail inventory level   = %.2f\n",
				results.getMeanRetailInventoryLevel());
	}
	
}
