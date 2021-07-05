package com.gly.scs.exp.exp08;

import com.gly.scs.exp.exp07.XdockScenarioFactory;
import com.gly.scs.exp.param.ExpParameters;
import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.LastYearParameters;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.ConsumptionOrDemand;
import com.gly.scs.replen.ForecastOrderUpToLevel;
import com.gly.scs.replen.ForecastingMethod;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public class LastYearFamily extends ParametrizedScenarioFamily {
	
	private final int reportDelay;
	private final Minuend minuend;
	private final ConsumptionOrDemand cod;
	private final Allocation allocation;
	
	public LastYearFamily(Builder b, double[] parameters) {
		super(b.name, parameters);
		reportDelay = b.reportDelay;
		minuend = b.minuend;
		cod = b.cod;
		allocation = b.allocation;
	}
	
	public static class Builder {
		private int reportDelay;
		private Minuend minuend;
		private ConsumptionOrDemand cod;
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
		
		public Builder withConsumptionOrDemand(ConsumptionOrDemand cod) {
			this.cod = cod;
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
		
		public LastYearFamily build(double[] parameters) {
			return new LastYearFamily(this, parameters);
		}
	}

	@Override
	public AbstractScenario getScenario(
			double parameter, ExperimentParameters expParameters) {
		ForecastingMethod method =
				new ForecastingMethod.LastYear(
						cod, expParameters.getNumberOfTimestepsInYear());
		OrderUpToLevel outl = new ForecastOrderUpToLevel.Builder()
				.withForecastingMethod(method)
				.withForecastPeriods(LastYearParameters.HISTORY_TIMESTEPS)
				.withOrderUpToPeriods(parameter)
				.build();
		return XdockScenarioFactory.getScenario(
				LastYearParameters.NAME,
				outl, 
				reportDelay,
				minuend,
				allocation,
				expParameters.getRetailInitialInventoryLevel());
	}
	
	@Override
	public String toString() {
		return reportDelay + "-" + minuend + "-" + cod;
	}

}
