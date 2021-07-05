package com.gly.scs.zambia;

import com.gly.scs.domain.NationalFacility;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.replen.AbstractReplenishmentFunction;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.OrderUpToReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;

/**
 * 
 * @author zacharyleung
 *
 */
public class ZambiaTest {

	// replenishment policy
	private static int amiHistoryPeriods = 12;
	// cross-docking parameters
	private static int xdockOrderUpToPeriods = 16;

	public static void main(String[] args) {
		int oneTierReportDelay = 1;

		AmiOrderUpToLevel orderUpToLevel = new AmiOrderUpToLevel.Builder()
		.withHistoryPeriods(amiHistoryPeriods)
		.withOrderUpToPeriods(xdockOrderUpToPeriods)
		.build();

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel);

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunction);

	}

}
