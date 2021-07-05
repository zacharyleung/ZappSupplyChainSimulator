package com.gly.scs.exp.exp08;

import com.gly.scs.exp.exp07.XdockScenarioFactory;
import com.gly.scs.exp.exp08.LastYearFamily.Builder;
import com.gly.scs.exp.param.ExpParameters;
import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.LsiParameters;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.ConsumptionOrDemand;
import com.gly.scs.replen.LsiOrderUpToLevel;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public class LsiFamily extends ParametrizedScenarioFamily {
	
	private final int reportDelay;
	private final Minuend minuend;
	private final OrderUpToLevel.Type type;	
	private final Allocation allocation;
	
	public LsiFamily(Builder b, double[] parameters) {
		super(b.name, parameters);
		reportDelay = b.reportDelay;
		minuend = b.minuend;
		type = b.type;
		allocation = b.allocation;
	}

	public static class Builder {
		private int reportDelay;
		private Minuend minuend;
		private OrderUpToLevel.Type type = null;
		private String name;
		private Allocation allocation = null;
		
		public Builder withReportDelay(int i) {
			this.reportDelay = i;
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
				
		public Builder withAllocation(Allocation a) {
			this.allocation = a;
			return this;
		}

		public Builder withName(String s) {
			this.name = s;
			return this;
		}
		
		public LsiFamily build(double[] parameters) {
			return new LsiFamily(this, parameters);
		}
	}

	@Override
	public AbstractScenario getScenario(
			double parameter, ExperimentParameters expParameters) {
		OrderUpToLevel outl = new LsiOrderUpToLevel.Builder()
				.withHistoryTimesteps(LsiParameters.HISTORY_TIMESTEPS)
				.withFactor(parameter / 
						expParameters.getNumberOfTimestepsInMonth())
				.withType(type)
				.withYearTimesteps(expParameters.getNumberOfTimestepsInYear())
				.build();
		return XdockScenarioFactory.getScenario(
				LsiParameters.NAME,
				outl, 
				reportDelay,
				minuend,
				allocation,
				expParameters.getRetailInitialInventoryLevel());
	}

}
