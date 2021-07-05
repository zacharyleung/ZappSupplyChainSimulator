package com.gly.scs.exp.policy;

import com.gly.scs.exp.exp07.XdockScenarioFactory;
import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.LsiParameters;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.LsiOrderUpToLevel;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public class PolicyFamilyLsi extends PolicyFactory {
	
	private Allocation allocation;
	private Minuend minuend;
	private OrderUpToLevel.Type type;

	public PolicyFamilyLsi(Allocation allocation, Minuend minuend, 
			int reportDelay, OrderUpToLevel.Type type) {
		super(reportDelay);
		this.allocation = allocation;
		this.minuend = minuend;
		this.type = type;
	}
	
	@Override
	public AbstractScenario getPolicy() {
		OrderUpToLevel outl = new LsiOrderUpToLevel.Builder()
				.withHistoryTimesteps(LsiParameters.HISTORY_TIMESTEPS)
				.withFactor(LsiParameters.ORDER_UP_TO_TIMESTEPS / 
						ExperimentParameters.getNumberOfTimestepsInMonth())
				.withType(type)
				.withYearTimesteps(ExperimentParameters.getNumberOfTimestepsInYear())
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
