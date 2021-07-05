package com.gly.scs.exp.exp07;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.gly.scs.exp.exp08.ParametrizedScenarioFamily;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.zambia.SupplyChainParser;
import com.gly.scs.zambia.ZambiaSimulationParameters;
import com.google.common.base.Stopwatch;

public class FrontierUtils {
	public static void simulate(
			SupplyChainParser supplyChainParser,
			ParametrizedScenarioFamily family,
			int numberOfReplications,
			double supplyDemandRatio,
			File outputFolder,
			String outputFileExtension) 
			throws Exception {
		File outputFile = new File(outputFolder,
				String.format("%s.%s", family.getName(), outputFileExtension));

		try(PrintStream out = new PrintStream(outputFile)) {
			Stopwatch stopwatch = Stopwatch.createStarted();

			out.printf("# input folder = %s%n", 
					supplyChainParser.getInputFolder());

			System.out.println("Fro01.simulate()");
			System.out.println("family = " + family.getName());
			Calendar cal = Calendar.getInstance();
			cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println(sdf.format(cal.getTime()));
			System.out.println();

			out.println("Parameter,RandomSeed,Variable,Value");
			for (double parameter : family.getParameters()) {
				for (int i = 0; i < numberOfReplications; ++i) {
					int randomSeed = i;

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

					AbstractScenario scenario = family.getScenario(parameter);
					AbstractSimulator simulator = 
							scenario.run(supplyChain, simulationParameters);				
					SimulationResults results = new SimulationResults(simulator);

					// write the output in long form
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"ServiceLevel",
							results.getServiceLevel());
					out.printf("%.2f,%d,%s,%.4f%n",
							parameter,
							randomSeed,
							"InventoryLevel",
							results.getMeanRetailInventoryLevel());
				}
			}

			out.printf("# Time elapsed = %s%n", stopwatch);
		}
	}
}
