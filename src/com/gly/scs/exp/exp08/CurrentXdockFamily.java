package com.gly.scs.exp.exp08;

import com.gly.scs.exp.exp07.XdockScenarioFactory;
import com.gly.scs.exp.param.CurrentXdockParameters;
import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public class CurrentXdockFamily extends ParametrizedScenarioFamily {

	private final int reportDelay;
	private final int historyPeriods;
	private final Allocation allocation;
	private final Minuend minuend;
	private final OrderUpToLevel.Type type;	

	private CurrentXdockFamily(Builder b, double[] parameters) {
		super(b.name, parameters);
		this.reportDelay = b.reportDelay;
		this.historyPeriods = b.historyPeriods;
		this.allocation = b.allocation;
		this.minuend = b.minuend;
		this.type = b.type;
	}
	
	public static class Builder {
		// Variables with default values
		private int historyPeriods = CurrentXdockParameters.HISTORY_TIMESTEPS;
		private int reportDelay = -1;
		private Allocation allocation = null;
		private Minuend minuend = null;
		private OrderUpToLevel.Type type = null;
		private String name = null;
		
		public Builder withReportDelay(int i) {
			this.reportDelay = i;
			return this;
		}
		
		public Builder withAllocation(Allocation a) {
			this.allocation = a;
			return this;
		}
		
		public Builder withMinuend(Minuend m) {
			this.minuend = m;
			return this;
		}
		
		public Builder withType(OrderUpToLevel.Type t) {
			this.type = t;
			return this;
		}
		
		public Builder withName(String s) {
			this.name = s;
			return this;
		}
		
		public CurrentXdockFamily build(double[] parameters) {
			return new CurrentXdockFamily(this, parameters);
		}
	}
	
	@Override
	public AbstractScenario getScenario(
			double parameter, ExperimentParameters expParameters) {
		OrderUpToLevel outl = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(historyPeriods)
				.withOrderUpToPeriods(parameter)
				.withType(type)
				.build();
		return XdockScenarioFactory.getScenario(name,
				outl, 
				reportDelay,
				minuend,
				allocation,
				expParameters.getRetailInitialInventoryLevel());
	}
	
}
