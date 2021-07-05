package com.gly.scs.exp.exp06;

import java.util.LinkedList;

import com.gly.scs.domain.NationalFacility;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.replen.AbstractReplenishmentFunction;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToReplenishmentFunction;
import com.gly.scs.replen.RandomAmiOrderUpToLevel;
import com.gly.scs.replen.TimeVaryingPair;
import com.gly.scs.replen.TimeVaryingReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.replen.OrderUpToLevel.Type;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.XdockScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class XdockSetting extends Setting {
	
	/** Number of order-up-to timesteps before the initial shipment */
	public static final int OUTP_BEFORE = 8;
	
	public XdockSetting(Builder b) {
		super(b);
	}

	@Override
	public AbstractScenario getScenario(long randomSeed) {
		System.out.printf("XdockSetting.getScenario(%d)\n", randomSeed);
		System.out.printf("randomSeed = %d\n", randomSeed);

		//Allocation allocation = new Allocation.Proportional();
		//Allocation allocation = new Allocation.Fcfs();
		Allocation allocation = new Allocation.RandomFcfs();
		
		int historyPeriods = ZambiaSimulationParameters.amiHistoryPeriods;
		
		// Number of order-up-to periods for cross-docking policies
		double XDOCK_OUT_PERIODS =
				ZambiaSimulationParameters.xdockOrderUpToPeriods;

		// Replenishment function before the pilot 
		AmiOrderUpToLevel orderUpToLevel1 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(OUTP_BEFORE)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction1 =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel1, minuend, allocation);

		// Replenishment function for the initial shipment 
		RandomAmiOrderUpToLevel orderUpToLevel2 = new RandomAmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(XDOCK_OUT_PERIODS)
				.withType(initialType)
				.withRandomSeed(randomSeed)
				.withScale(initialScale)
				.build();
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction2 =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel2, initialMinuend, allocation);

		// Replenishment function for no shipments after the initial shipment 
		AmiOrderUpToLevel orderUpToLevel3 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(0)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction3 =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel3, minuend, allocation);

		// Replenishment function for regular shipments after start of pilot 
		AmiOrderUpToLevel orderUpToLevel4 = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(XDOCK_OUT_PERIODS)
				.withType(type)
				.build();
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction4 =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel4, minuend, allocation);

		LinkedList<TimeVaryingPair<NationalFacility, XdockLeadTime>> pairs =
				new LinkedList<>();
		pairs.add(new TimeVaryingPair<NationalFacility, XdockLeadTime>
		(retailReplenishmentFunction1, initialShipmentTimestep));
		pairs.add(new TimeVaryingPair<NationalFacility, XdockLeadTime>
		(retailReplenishmentFunction2, initialShipmentTimestep + 4));
		//pairs.add(new TimeVaryingPair<NationalFacility, XdockLeadTime>
		//(retailReplenishmentFunction3, initialShipmentTimestep + 8));
		pairs.add(new TimeVaryingPair<NationalFacility, XdockLeadTime>
		(retailReplenishmentFunction4, Integer.MAX_VALUE));

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunctionTime =
		new TimeVaryingReplenishmentFunction<NationalFacility, XdockLeadTime>
		(pairs);

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunctionTime);

		return new XdockScenario.Builder()
				.withName("xdock-past")
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}

	@Override
	public String getName() {
		return "xdock";
	}

}
