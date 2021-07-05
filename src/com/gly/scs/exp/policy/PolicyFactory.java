package com.gly.scs.exp.policy;

import com.gly.scs.sim.AbstractScenario;

public abstract class PolicyFactory {
	
	public final int reportDelay;
	
	public PolicyFactory(int reportDelay) {
		this.reportDelay = reportDelay;
	}
	
	public abstract AbstractScenario getPolicy();
	
	public String getPolicyName() {
		return this.getClass().getSimpleName() + "Delay" + reportDelay;
	}
	
}
