package com.gly.scs.exp.exp06;

import java.io.*;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTime.MinimumValue;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel.Type;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.AbstractSimulator;
import com.gly.scs.sim.SimulationParameters;
import com.gly.scs.sim.SimulationResults;
import com.gly.scs.sim.SupplyChain;
import com.gly.scs.sim.SupplyChainFactory;
import com.gly.scs.zambia.*;
import com.google.common.base.Stopwatch;

/**
 * We start the simulation in timestep 0, and run the simulation for
 * 48 timesteps.  The initial shipment is sent between timesteps
 * [20,24).
 * 
 * Let X denote the parameter START_OF_INITIAL_SHIPMENT_PERIOD.
 * Before period X:
 *   The national facility ships using an order-up-to level defined by
 *   ORDER_UP_TO_PERIODS_BEFORE.
 * In the periods [X, X + 4):
 *   The national facility makes an initial shipment to health
 *   facilities, with the pack-size-specific order-up-to level.
 * In the periods [X + 4, X + 8):
 *   The national facility does not make any shipments to health  
 *   facilities.
 * After periods X + 8:
 *   The national facility ships using an order-up-to
 *   level defined by ORDER_UP_TO_PERIODS_AFTER.
 *   
 * The INITIAL_SHIPMENT_PERIODS parameters come from the Google
 * Spreadsheet "Validation Proportion of Drugs".
 * 
 * |--------------------|--------------|--------------|
 * |                    | Replications | Running Time |
 * |--------------------|--------------|--------------|
 * | Dell Optiplex 2020 |           10 |    5 minutes |
 * | Dell Optiplex 2020 |          100 |   50 minutes |
 * | Dell Optiplex 2020 |         1000 |    8.3 hours |
 * |--------------------|--------------|--------------|
 *
 * @author zacharyleung
 */
public class Exp06 {

	// Parameters for 2017-02-08
	//private static final double INITIAL_OUT_FACTOR_6 = 3.20 / 4;
	//private static final double INITIAL_OUT_FACTOR_12 = 7.38 / 4;
	//private static final double INITIAL_OUT_FACTOR_18 = 6.16 / 4;
	//private static final double INITIAL_OUT_FACTOR_24 = 3.87 / 4;
	/** Number of timesteps for order-up-to level before pilot. */
	//private static final double OUT_TIMESTEPS_BEFORE = 8;
	//private static final Minuend initialMinuend = new Minuend.Zero();
	//private static int START_OF_INITIAL_SHIPMENT_PERIOD = 24;
	// For secondary lead times, is the smallest possible lead time value
	// zero or one?
	//private static final MinimumValue leadTimeMinValue = MinimumValue.ZERO;
	///** Number of timesteps for order-up-to level before pilot. */
	//private static final double OUT_TIMESTEPS_BEFORE = 12;
	/** To calculate the initial shipment quantity,
	 do we use consumption or demand? */
	//private static final Type initialType = Type.CONSUMPTION;
	private static final Type initialType = Type.DEMAND;

	// Parameters for 2017-02-28
	private static final double[] INITIAL_SCALE_6 = {0.9722};
	private static final double[] INITIAL_SCALE_12 = {1.8925};
	private static final double[] INITIAL_SCALE_18 = {2.0150};
	private static final double[] INITIAL_SCALE_24 = {1.2013};
//	private static final double[] INITIAL_SCALE_6 = {
//			0.4971, 0.4008, 3.9579, 0.9882, 1.1721, 0.9562, 0.4562,
//			1.1761, 0.2742, 0.6559, 1.0872, 2.1001, 0.7515, 1.3203,
//			0.6949, 1.0435};
//	private static final double[] INITIAL_SCALE_12 = {
//			1.0353, 1.0215, 5.8697, 1.8319, 0.9312, 3.3934, 3.3831,
//			2.7253, 0.5083, 1.8238, 3.1147, 5.1097, 0.9706, 2.4475,
//			1.8036, 1.9531};
//	private static final double[] INITIAL_SCALE_18 = {
//			0.9149, 1.4772, 6.8085, 1.8213, 1.2345, 1.7624, 4.4848, 
//			3.6128, 0.6738, 1.6118, 3.6432, 6.4511, 1.3623, 5.4074, 
//			2.2087, 2.8768};
//	private static final double[] INITIAL_SCALE_24 = {
//			0.7924, 0.4921, 3.6933, 1.0920, 1.4803, 1.1741, 2.4648,
//			2.0217, 0.2693, 1.2885, 1.0679, 4.1257, 0.3933, 1.7291,
//			1.2287, 1.1499};
	/** Number of timesteps for order-up-to level before pilot. */
	private static final double OUT_TIMESTEPS_BEFORE = 8;
	private static final Minuend initialMinuend = new Minuend.InventoryPosition();
	private static int START_OF_INITIAL_SHIPMENT_PERIOD_XDOCK = 18;
	private static int START_OF_INITIAL_SHIPMENT_PERIOD_ISTOCK = 21;
	private static final MinimumValue leadTimeMinValue = MinimumValue.ZERO;

	/*
	 * Important parameters
	 */


	// To calculate the shipment quantity, do we use the inventory
	// on-hand or the inventory position?
	//private static final Minuend minuend = new Minuend.InventoryLevel();
	private static final Minuend minuend = new Minuend.InventoryPosition();

	// To calculate the shipment quantities using the 
	// do we use consumption or demand?
	private static final Type type = Type.CONSUMPTION;

	private static final int SIMULATION_START_TIMESTEP = 0;
	private static final int oneTierReportDelay = 1;

	private static SupplyChainFactory scf;

	private static final String outputDirectory = "exp/exp06";

	public static void main(String[] args) throws Exception {
		Params params = new Params20170315();
		int packSize = 0;
		//private static String inputFolder = "input/test-2";
		//private static String inputFolder = "input/zambia-fast12";
		String inputFolder = "input/zambia";
		// String inputFolder = "input/test-flat-2";
		int NUMBER_OF_REPLICATIONS = 500;
		
		if (args.length == 1) {
			NUMBER_OF_REPLICATIONS = Integer.parseInt(args[0]);
		}

		System.out.println("Parameters:");
		System.out.printf("Number of replications = %d\n",
				NUMBER_OF_REPLICATIONS);
		System.out.printf("Start of initial shipment period = %d (x-dock)\n",
				START_OF_INITIAL_SHIPMENT_PERIOD_XDOCK);
		System.out.printf("Start of initial shipment period = %d (i-stock)\n",
				START_OF_INITIAL_SHIPMENT_PERIOD_ISTOCK);

		Stopwatch stopwatch = Stopwatch.createStarted();

		scf = new SupplyChainFactoryFolder(inputFolder, leadTimeMinValue);

		double supplyDemandRatio = 100;

		SupplyChain supplyChain = 
				scf.getSupplyChain(supplyDemandRatio);

		LinkedList<Setting> settings = new LinkedList<>();
		packSize = 6;
		settings.add(new IstockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_ISTOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(packSize)));
		packSize = 12;
		settings.add(new IstockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_ISTOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(12)));
		packSize = 18;
		settings.add(new IstockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_ISTOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(18)));
		packSize = 24;
		settings.add(new IstockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_ISTOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(24)));
		
		packSize = 6;
		settings.add(new XdockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_XDOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(6)));
		packSize = 12;
		settings.add(new XdockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_XDOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(12)));
		packSize = 18;
		settings.add(new XdockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_XDOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(18)));
		packSize = 24;
		settings.add(new XdockSetting(new Setting.Builder()
				.withInitialScale(params.getInitialScale(packSize))
				.withInitialShipmentTimestep(START_OF_INITIAL_SHIPMENT_PERIOD_XDOCK)
				.withInitialType(initialType)
				.withInitialMinuend(initialMinuend)
				.withOneTierReportDelay(oneTierReportDelay)
				.withOutputDirectory(outputDirectory)
				.withMinuend(minuend)
				.withType(type)
				.withPackSize(24)));

		for (Setting setting : settings) {
			System.out.printf("Running %s\n", setting.getOutputDirectory());	
			File outputDirectory = new File(setting.getOutputDirectory());
			// Recursively delete the output directory
			FileUtils.deleteDirectory(outputDirectory);
			// Create the output directory
			outputDirectory.mkdirs();
			for (int i = 0; i < NUMBER_OF_REPLICATIONS; ++i) {
				AbstractScenario scenario = setting.getScenario(i);
				AbstractSimulator simulator = 
						scenario.run(supplyChain, getSimulationParameters(i));
				SimulationResults results = new SimulationResults(simulator);
				File outputFile = new File(outputDirectory, i + ".csv"); 
				try (PrintStream out = new PrintStream(outputFile)) {
					results.printTraceLongFormat(out);
				}
			}
		}

		System.out.println(stopwatch);
	}

	private static SimulationParameters getSimulationParameters(int randomSeed) {
		return new SimulationParameters.Builder()
				.withAfterPeriods(48)
				.withBeforePeriods(48)
				.withRandomSeed(randomSeed)
				.withSimulationPeriods(48)
				.withStartPeriod(SIMULATION_START_TIMESTEP)
				.withWarmUpPeriods(0)
				.build();
	}

}
