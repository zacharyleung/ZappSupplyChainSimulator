package com.gly.scs.zambia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;

import com.gly.scs.exp.exp07.Policies;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.google.common.base.Stopwatch;

public class MainScatter {

	private static String inputFolder = //"input/zambia-test";
	//"input/zambia-4";
	"input/zambia";

	private static double supplyDemandRatio = 1.2;
	private static int randomSeed = 3;

	private static File outputFolder = new File("exp/Scatter");

	private static SupplyChainParser supplyChainParser;

	public static void main(String[] args) throws Exception {
		System.out.println("Starting MainScatter.main()");
		
		Stopwatch stopwatch = Stopwatch.createStarted();

		supplyChainParser = new SupplyChainParser(inputFolder);
		for (AbstractScenario scenario : Policies.getScenarios()) {
			simulateScenario(scenario);
		}
		
		//Iterator<AbstractScenario> iterator = Policies.getScenarios().iterator(); 
		//simulateScenario(iterator.next());
		//simulateScenario(iterator.next());
		
		System.out.println(stopwatch);
	}

	private static void simulateScenario(AbstractScenario scenario) throws Exception {
		File outputFile = new File(outputFolder, scenario.getName() + ".csv");

		try(PrintStream out = new PrintStream(outputFile)) {
			Stopwatch stopwatch = Stopwatch.createStarted();

			out.printf("# input folder = %s%n", inputFolder);

			System.out.println("MainScatter.simulateScenario()");
			System.out.println("scenario = " + scenario.getName());
			System.out.println("supply/demand ratio = " + supplyDemandRatio);
			System.out.println("random seed = " + randomSeed);
			Calendar cal = Calendar.getInstance();
			cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println( sdf.format(cal.getTime()) );
			System.out.println();

			SupplyChain supplyChain = 
					supplyChainParser.getSupplyChain(supplyDemandRatio);

			SimulationParameters simulationParameters = new SimulationParameters.Builder()
			.withBeforePeriods(ZambiaSimulationParameters.beforePeriods)
			.withAfterPeriods(ZambiaSimulationParameters.forecastHorizon)
			.withWarmUpPeriods(ZambiaSimulationParameters.warmUpPeriods)
			.withStartPeriod(ZambiaSimulationParameters.startPeriod)
			.withSimulationPeriods(ZambiaSimulationParameters.simulationPeriods)
			.withRandomSeed(randomSeed)
			.build();

			AbstractSimulator simulator = 
					scenario.run(supplyChain, simulationParameters);
			SimulationResults results = new SimulationResults(simulator);

			out.println("ServiceLevel,RealizedMeanSecondaryLeadTime");
			for (String retailFacilityId : supplyChain.topology.getRetailIds()) {
				out.printf("%.4f,%.2f%n",
						results.getServiceLevel(retailFacilityId),
						results.getRealizedMeanSecondaryLeadTime(retailFacilityId));
			}
			out.printf("# Time elapsed = %s%n", stopwatch);
		}
	}

}
