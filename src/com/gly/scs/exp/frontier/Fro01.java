package com.gly.scs.exp.frontier;

import java.io.File;
import java.util.LinkedList;

import com.gly.scs.exp.exp07.FrontierUtils;
import com.gly.scs.exp.exp08.ParametrizedScenarioFamily;
import com.gly.scs.exp.exp08.XdockFamily;
import com.gly.scs.replen.*;
import com.gly.scs.zambia.SupplyChainParser;
import com.gly.util.MathUtils;
import com.google.common.base.Stopwatch;

/**
 * Running time for 2 replications, 7 parameters: 12 minutes
 * @author zacleung
 *
 */
public class Fro01 {
	private static String inputFolder;
	private static File outputFolder = new File("exp/fro01/java");
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
		families.add(new XdockFamily("past-invlev-prop", d,
				new XdockFamily.Past(),
				new Minuend.InventoryLevel(),
				new Allocation.Proportional()));
		families.add(new XdockFamily("past-invpos-prop", d,
				new XdockFamily.Past(),
				new Minuend.InventoryPosition(),
				new Allocation.Proportional()));
		families.add(new XdockFamily("past-invpos-fcfs", d,
				new XdockFamily.Past(),
				new Minuend.InventoryPosition(),
				new Allocation.Fcfs()));
		//		families.add(new XdockFamily("fore-invlev", d,
		//				new XdockFamily.Forecast(),
		//				new Minuend.InventoryLevel(),
		//				new Allocation.Proportional()));
		//		families.add(new XdockFamily("fore-invpos", d,
		//				new XdockFamily.Forecast(),
		//				new Minuend.InventoryPosition(),
		//				new Allocation.Proportional()));

		for (ParametrizedScenarioFamily family : families) {
			FrontierUtils.simulate(supplyChainParser, family, 
					numberOfReplications, supplyDemandRatio,
					outputFolder, outputFileExtension);
		}

		System.out.println(stopwatch);
	}

}
