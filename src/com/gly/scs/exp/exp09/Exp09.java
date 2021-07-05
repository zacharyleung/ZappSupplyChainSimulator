package com.gly.scs.exp.exp09;

import java.io.File;
import java.util.LinkedList;

import com.gly.scs.exp.exp07.FrontierUtils;
import com.gly.scs.exp.exp08.CurrentXdockFamily;
import com.gly.scs.exp.exp08.LastYearFamily;
import com.gly.scs.exp.exp08.LsiFamily;
import com.gly.scs.exp.exp08.OptimizationFamilyGood;
import com.gly.scs.exp.exp08.ParametrizedScenarioFamily;
import com.gly.scs.exp.param.ExpParameters;
import com.gly.scs.zambia.SupplyChainParser;
import com.gly.util.MathUtils;
import com.google.common.base.Stopwatch;

public class Exp09 {

	private static String inputFolder = 
			//"input/test-2";
			//"input/zambia-4";
			//"input/zambia-12";
			"input/zambia";
	private static File outputFolder = new File("exp/exp09/java");
	private static int numberOfReplications = 2;
	private static String outputFileExtension = "csv";
	private static double supplyDemandRatio = 1.2;
	private static int numberOfParameters = 5;

	public static void main(String[] args) throws Exception {
		SupplyChainParser supplyChainParser = 
				new SupplyChainParser(inputFolder,
						ExpParameters.MINIMUM_LEAD_TIME);

		System.out.println("Starting Exp08.main()");
		System.out.println("input folder = " + inputFolder);
		System.out.println();

		Stopwatch stopwatch = Stopwatch.createStarted();

		LinkedList<ParametrizedScenarioFamily> families = new LinkedList<>();

		double[] d = MathUtils.getSamples(1.0, 24.0, numberOfParameters);
		families.add(new CurrentXdockFamily(d));
		families.add(new CurrentXdockFamily(d)
				);
		
		for (ParametrizedScenarioFamily family : families) {
			FrontierUtils.simulate(supplyChainParser, family, 
					numberOfReplications, supplyDemandRatio,
					outputFolder, outputFileExtension);
		}

		System.out.println(stopwatch);

	}

}
