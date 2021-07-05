package com.gly.scs.exp.frontier;

import com.gly.scs.domain.Facility;
import com.gly.scs.exp.exp08.ParametrizedScenarioFamily;
import com.gly.scs.leadtime.OneTierLeadTime;
import com.gly.scs.replen.AbstractReplenishmentFunction;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.OneTierReplenishmentPolicy;
import com.gly.scs.replen.OrderUpToReplenishmentFunction;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.IstockScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class IstockFamily extends ParametrizedScenarioFamily {
	private final int oneTierReportDelay = 1;

	public IstockFamily(double[] d) {
		super("istock", d);
	}

	@Override
	public AbstractScenario getScenario(double parameter) {
		// regional replenishment policy
		AmiOrderUpToLevel regionalOrderUpToLevel = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(ZambiaSimulationParameters.amiHistoryPeriods)
				.withOrderUpToPeriods(3 * parameter)
				.build();

		AbstractReplenishmentFunction<Facility, OneTierLeadTime> 
		regionalReplenishmentFunction =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(regionalOrderUpToLevel);

		OneTierReplenishmentPolicy regionalReplenishmentPolicy =
				new OneTierReplenishmentPolicy(oneTierReportDelay,
						regionalReplenishmentFunction);

		// retail replenishment policy
		AmiOrderUpToLevel retailOrderUpToLevel = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(ZambiaSimulationParameters.amiHistoryPeriods)
				.withOrderUpToPeriods(2 * parameter)
				.build();

		AbstractReplenishmentFunction<Facility, OneTierLeadTime> 
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(retailOrderUpToLevel);

		OneTierReplenishmentPolicy retailReplenishmentPolicy =
				new OneTierReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunction);		
		return new IstockScenario.Builder()
				.withName("istock")
				.withRegionalReplenishmentPolicy(regionalReplenishmentPolicy)
				.withRetailReplenishmentPolicy(retailReplenishmentPolicy)
				.withRegionalInitialInventoryLevel(ZambiaSimulationParameters.regionalInitialInventoryLevel)
				.withRetailInitialInventoryLevel(ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}
}
