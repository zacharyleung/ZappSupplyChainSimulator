package com.gly.scs.exp.exp07;

import com.gly.scs.exp.param.ExpParameters;
import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.LsiParameters;
import com.gly.scs.exp.policy.PolicyFactory;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.LsiOrderUpToLevel;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.replen.OrderUpToLevel.Type;
import com.gly.scs.sim.AbstractScenario;

public class LsiBestPolicy extends PolicyFactory {

	private int reportDelay = 0;

	public LsiBestPolicy withReportDelay(int i) {
		this.reportDelay = i;
		return this;
	}
	
	@Override
	public AbstractScenario getPolicy(ExperimentParameters p) {
		Minuend minuend = new Minuend.InventoryPosition();
		Allocation allocation = new Allocation.Proportional();	
		Type type = Type.DEMAND;
		
		OrderUpToLevel outl = new LsiOrderUpToLevel.Builder()
				.withHistoryTimesteps(LsiParameters.HISTORY_TIMESTEPS)
				.withFactor(LsiParameters.ORDER_UP_TO_TIMESTEPS / 
						ExperimentParameters.NUMBER_OF_TIMESTEPS_IN_MONTH)
				.withType(type)
				.withYearTimesteps(p.getNumberOfTimestepsInYear())
				.build();
		return XdockScenarioFactory.getScenario(
				"lsi-best-delay" + reportDelay,
				outl, 
				reportDelay,
				minuend,
				allocation,
				p.getRetailInitialInventoryLevel());
	}
	
}
