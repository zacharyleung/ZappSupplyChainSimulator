package com.gly.scs.exp.exp07;

import com.gly.scs.domain.NationalFacility;
import com.gly.scs.exp.param.ExpParameters;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.replen.AbstractReplenishmentFunction;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.replen.OrderUpToReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.XdockScenario;

public class XdockScenarioFactory {
	public static AbstractScenario getScenario(String name,
			OrderUpToLevel orderUpToLevel,
			int oneTierReportDelay,
			Minuend minuend,
			Allocation allocation,
			OrderUpToLevel retailInitialInventoryLevel) {
		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel,
				minuend, 
				allocation);

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunction);

		return new XdockScenario.Builder()
				.withName(name)
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(retailInitialInventoryLevel)
				.build();
	}
}
