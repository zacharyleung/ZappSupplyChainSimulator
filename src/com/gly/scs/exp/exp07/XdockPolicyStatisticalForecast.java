package com.gly.scs.exp.exp07;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.policy.PolicyFactory;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.ForecastOrderUpToLevel;
import com.gly.scs.replen.ForecastingMethod;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class XdockPolicyStatisticalForecast extends PolicyFactory {

	@Override
	public AbstractScenario getPolicy(ExperimentParameters p) {
		String name = "xdock-forecast-good-delay-0";
		int oneTierReportDelay = 0;
		int forecastLevel = 
				ZambiaSimulationParameters.FORECAST_LEVEL_INDUSTRY;
		ForecastingMethod method =
				new ForecastingMethod.Statistical(forecastLevel);
		OrderUpToLevel outl = new ForecastOrderUpToLevel.Builder()
				.withForecastingMethod(method)
				.withForecastPeriods(ZambiaSimulationParameters.amiHistoryPeriods)
				.withOrderUpToPeriods(ZambiaSimulationParameters.xdockOrderUpToPeriods)
				.build();
		return XdockScenarioFactory.getScenario(name, outl, 
				oneTierReportDelay, new Minuend.InventoryPosition(), 
				new Allocation.Proportional(),
				p.getRetailInitialInventoryLevel());
	}

}
