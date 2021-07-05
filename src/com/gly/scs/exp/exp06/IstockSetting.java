package com.gly.scs.exp.exp06;

import java.util.LinkedList;

import com.gly.scs.domain.Facility;
import com.gly.scs.domain.NationalFacility;
import com.gly.scs.leadtime.OneTierLeadTime;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.replen.AbstractReplenishmentFunction;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OneTierReplenishmentPolicy;
import com.gly.scs.replen.OrderUpToLevel.Type;
import com.gly.scs.replen.OrderUpToReplenishmentFunction;
import com.gly.scs.replen.RandomAmiOrderUpToLevel;
import com.gly.scs.replen.TimeVaryingPair;
import com.gly.scs.replen.TimeVaryingReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.IstockScenario;
import com.gly.scs.sim.XdockScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

/**
 * Intermediate stocking setting class.
 * 
 * The regional facilities order a initial shipment from the national facility
 * one month = 4 timesteps before the retail facilities order a initial
 * shipment from their regional facilities. 
 * 
 * @author znhleung
 *
 */
public class IstockSetting extends Setting {
	/** Number of order-up-to timesteps before the initial shipment */
	public static final int REGIONAL_OUTP_BEFORE = 12;
	public static final int RETAIL_OUTP_BEFORE = 8;

	public IstockSetting(Builder b) {
		super(b);
	}

	@Override
	public String getName() {
		return "istock";
	}

	@Override
	public AbstractScenario getScenario(long randomSeed) {
		System.out.printf("IstockSetting.getScenario(%d)\n", randomSeed);
		System.out.printf("randomSeed = %d\n", randomSeed);

		return new IstockScenario.Builder()
				.withName("istock")
				.withRegionalReplenishmentPolicy(getRegionalReplenishmentPolicy(randomSeed))
				.withRetailReplenishmentPolicy(getRetailReplenishmentPolicy(randomSeed + 1))
				.withRegionalInitialInventoryLevel(ZambiaSimulationParameters.regionalInitialInventoryLevel)
				.withRetailInitialInventoryLevel(ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();

	}
	
	private OneTierReplenishmentPolicy getRegionalReplenishmentPolicy(long randomSeed) {
		int historyPeriods = ZambiaSimulationParameters.amiHistoryPeriods;
		// Number of order-up-to periods for intermediate-stocking policies
		double ISTOCK_OUT_PERIODS =
				ZambiaSimulationParameters.istockRegionalOrderUpToPeriods;
		Allocation allocation = new Allocation.RandomFcfs();

		// Replenishment function before the pilot 
		AmiOrderUpToLevel orderUpToLevel1 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(REGIONAL_OUTP_BEFORE)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction1 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel1, minuend, allocation);

		// Replenishment function for the initial shipment 
		RandomAmiOrderUpToLevel orderUpToLevel2 = new RandomAmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(ISTOCK_OUT_PERIODS)
				.withType(initialType)
				.withRandomSeed(randomSeed)
				//.withScale(initialScale)
				.withScale(new double[]{2})
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction2 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel2, initialMinuend, allocation);

		// Replenishment function for no shipments after the initial shipment 
		AmiOrderUpToLevel orderUpToLevel3 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(0)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction3 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel3, minuend, allocation);

		// Replenishment function for regular shipments after start of pilot 
		AmiOrderUpToLevel orderUpToLevel4 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(ISTOCK_OUT_PERIODS)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction4 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel4, minuend, allocation);

		LinkedList<TimeVaryingPair<Facility, OneTierLeadTime>> pairs =
				new LinkedList<>();
		pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		(retailReplenishmentFunction1, initialShipmentTimestep - 4));
		pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		(retailReplenishmentFunction2, initialShipmentTimestep));
		//pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		//(retailReplenishmentFunction3, initialShipmentTimestep + 8));
		pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		(retailReplenishmentFunction4, Integer.MAX_VALUE));

		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		regionaReplenishmentFunctionTime =
		new TimeVaryingReplenishmentFunction<Facility, OneTierLeadTime>
		(pairs);

		return new OneTierReplenishmentPolicy(oneTierReportDelay,
				regionaReplenishmentFunctionTime);
	}
	
	private OneTierReplenishmentPolicy getRetailReplenishmentPolicy(long randomSeed) {
		int historyPeriods = ZambiaSimulationParameters.amiHistoryPeriods;
		// Number of order-up-to periods for intermediate-stocking policies
		double ISTOCK_OUT_PERIODS =
				ZambiaSimulationParameters.istockRetailOrderUpToPeriods;
		
		//Allocation allocation = new Allocation.Proportional();
		//Allocation allocation = new Allocation.Fcfs();
		Allocation allocation = new Allocation.RandomFcfs();

		// Replenishment function before the pilot 
		AmiOrderUpToLevel orderUpToLevel1 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(RETAIL_OUTP_BEFORE)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction1 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel1, minuend, allocation);

		// Replenishment function for the initial shipment 
		RandomAmiOrderUpToLevel orderUpToLevel2 = new RandomAmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(ISTOCK_OUT_PERIODS)
				.withType(initialType)
				.withRandomSeed(randomSeed)
				.withScale(initialScale)
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction2 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel2, initialMinuend, allocation);

		// Replenishment function for no shipments after the initial shipment 
		AmiOrderUpToLevel orderUpToLevel3 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(0)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction3 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel3, minuend, allocation);

		// Replenishment function for regular shipments after start of pilot 
		AmiOrderUpToLevel orderUpToLevel4 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(ISTOCK_OUT_PERIODS)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunction4 =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(orderUpToLevel4, minuend, allocation);

		LinkedList<TimeVaryingPair<Facility, OneTierLeadTime>> pairs =
				new LinkedList<>();
		pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		(retailReplenishmentFunction1, initialShipmentTimestep));
		pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		(retailReplenishmentFunction2, initialShipmentTimestep + 4));
		//pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		//(retailReplenishmentFunction3, initialShipmentTimestep + 8));
		pairs.add(new TimeVaryingPair<Facility, OneTierLeadTime>
		(retailReplenishmentFunction4, Integer.MAX_VALUE));

		AbstractReplenishmentFunction<Facility, OneTierLeadTime>  
		retailReplenishmentFunctionTime =
		new TimeVaryingReplenishmentFunction<Facility, OneTierLeadTime>
		(pairs);
		
		return new OneTierReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunctionTime);
	}

}
