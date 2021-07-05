package com.gly.scs.exp.policy;

import com.gly.scs.exp.exp07.XdockScenarioFactory;
import com.gly.scs.exp.param.CurrentXdockParameters;
import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public class AmiIpFcfsPolicyFactory extends PolicyFactory {
	
	OrderUpToLevel.Type type = OrderUpToLevel.Type.CONSUMPTION;
	Minuend minuend = new Minuend.InventoryPosition();
	Allocation allocation = new Allocation.RandomFcfs();
	
	public AmiIpFcfsPolicyFactory(int reportDelay) {
		super(reportDelay);
	}
	
	@Override
	public AbstractScenario getPolicy() {
		OrderUpToLevel outl = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(CurrentXdockParameters.HISTORY_TIMESTEPS)
				.withOrderUpToPeriods(CurrentXdockParameters.ORDER_UP_TO_TIMESTEPS)
				.withType(type)
				.build();
		return XdockScenarioFactory.getScenario(
				getPolicyName(),
				outl,
				reportDelay,
				minuend,
				allocation,
				ExperimentParameters.getRetailInitialInventoryLevel());
	}
	
}
