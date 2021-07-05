package com.gly.scs.exp.param;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import com.gly.scs.exp.exp08.CurrentXdockFamily;
import com.gly.scs.exp.exp08.LastYearFamily;
import com.gly.scs.exp.exp08.LsiFamily;
import com.gly.scs.exp.exp08.OptimizationFamilySltp;
import com.gly.scs.exp.exp08.ParametrizedScenarioFamily;
import com.gly.scs.replen.ConsumptionOrDemand;
import com.gly.scs.replen.Minuend;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SupplyChainFactorySimple;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.zambia.ZambiaSimulationParameters;
import com.gly.util.MathUtils;
import com.google.common.base.Stopwatch;

public class OptVsLastYear {
	
	private static File outputFolder = new File("exp/");
	private static int numberOfReplications = 2;
	private static String outputFileExtension = "csv";
	private static double supplyDemandRatio = 1.2;
	private static int numberOfParameters = 10;

	// parameters about how many timesteps to run
	private static final int warmUpTimesteps = 3 * 48;
	private static final int simulationTimesteps = 1 * 48;

	private static final int NUMBER_OF_LOWER_BOUNDS = 25;

	public static void main(String[] args) throws Exception {
		System.out.println("Starting OptVsLastYear.main()");
		System.out.println();

		Stopwatch stopwatch = Stopwatch.createStarted();

		LinkedList<ParametrizedScenarioFamily> families = new LinkedList<>();

		double[] d = MathUtils.getSamples(1.0, 24.0, numberOfParameters);

		// Generate optimization policies with a fixed shipment lead time
		// percentile
		families.add(new OptimizationFamilySltp(d, 0.50, NUMBER_OF_LOWER_BOUNDS));
		
		// Last Year policy with 
		families.add(new LastYearFamily.Builder()
				.withConsumptionOrDemand(ConsumptionOrDemand.DEMAND)
				.withMinuend(new Minuend.InventoryPosition())
				.withReportDelay(0)
				.withName("lastyear-d-ip-0")
				.build(d));
		
		for (ParametrizedScenarioFamily family : families) {
			System.out.println(family);
			
			simulate(family, 
					numberOfReplications, supplyDemandRatio,
					outputFolder, outputFileExtension);
		}

		System.out.println(stopwatch);
	}
	
	public static void simulate(
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
							new SupplyChainFactorySimple()
							.getSupplyChain(supplyDemandRatio);

					SimulationParameters simulationParameters = new SimulationParameters.Builder()
							.withBeforePeriods(ZambiaSimulationParameters.beforePeriods)
							.withAfterPeriods(ZambiaSimulationParameters.forecastHorizon)
							.withWarmUpPeriods(warmUpTimesteps)
							.withStartPeriod(ZambiaSimulationParameters.startPeriod)
							.withSimulationPeriods(simulationTimesteps)
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
