package com.gly.scs.exp.frontier;

import java.io.File;
import java.util.LinkedList;

import com.gly.scs.exp.exp07.FrontierUtils;
import com.gly.scs.exp.exp08.ParametrizedScenarioFamily;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.zambia.SupplyChainParser;
import com.gly.util.MathUtils;
import com.google.common.base.Stopwatch;

/**
 * Running time for 2 replications, 7 parameters: 12 minutes
 * @author zacleung
 *
 */
public class Fro02 {
	private static String inputFolder;
	private static File outputFolder = new File("exp/fro02/java");
	private static int numberOfReplications = 2;
	private static String outputFileExtension = "csv";
	private static SupplyChainParser supplyChainParser;
	private static double supplyDemandRatio = 0.8;
	private static int numberOfParameters = 7;

	public static void main(String[] args) throws Exception {
		inputFolder = "input/zambia";
		//		inputFolder = "input/zambia-test";

		System.out.println("Starting MainFrontier.main()");
		System.out.println("input folder = " + inputFolder);
		System.out.println();

		Stopwatch stopwatch = Stopwatch.createStarted();

		supplyChainParser = new SupplyChainParser(inputFolder);

		LinkedList<ParametrizedScenarioFamily> families = new LinkedList<>();
		double[] d = MathUtils.getSamples(1.0, 24.0, numberOfParameters);
		families.add(new OptimizationFamily("blah", d, 
				HoldingCostType.FACILITY_ACCESSIBILITY, 0.5));

		for (ParametrizedScenarioFamily family : families) {
			FrontierUtils.simulate(supplyChainParser, family, 
					numberOfReplications, supplyDemandRatio,
					outputFolder, outputFileExtension);
		}

		System.out.println(stopwatch);
	}

}
