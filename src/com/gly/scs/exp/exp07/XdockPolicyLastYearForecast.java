package com.gly.scs.exp.exp07;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.LastYearParameters;
import com.gly.scs.exp.policy.PolicyFactory;
import com.gly.scs.replen.ConsumptionOrDemand;
import com.gly.scs.replen.ForecastOrderUpToLevel;
import com.gly.scs.replen.ForecastingMethod;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public class XdockPolicyLastYearForecast extends PolicyFactory {
	
	private int reportDelay = 0;

	public XdockPolicyLastYearForecast withReportDelay(int i) {
		this.reportDelay = i;
		return this;
	}
	
	@Override
	public AbstractScenario getPolicy(ExperimentParameters p) {
		ForecastingMethod method =
				new ForecastingMethod.LastYear(
						ConsumptionOrDemand.DEMAND,
						p.getNumberOfTimestepsInYear());
		OrderUpToLevel outl = new ForecastOrderUpToLevel.Builder()
				.withForecastingMethod(method)
				.withForecastPeriods(LastYearParameters.HISTORY_TIMESTEPS)
				.withOrderUpToPeriods(LastYearParameters.ORDER_UP_TO_TIMESTEPS)
				.build();
		return XdockScenarioFactory.getScenario(
				"last-year-delay" + reportDelay,
				outl, 
				LastYearParameters.REPORT_DELAY,
				LastYearParameters.MINUEND,
				LastYearParameters.ALLOCATION,
				p.getRetailInitialInventoryLevel());
	}
}
