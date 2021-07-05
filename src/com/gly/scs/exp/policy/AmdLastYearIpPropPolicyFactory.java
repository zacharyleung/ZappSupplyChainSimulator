package com.gly.scs.exp.policy;

import com.gly.scs.exp.exp07.XdockScenarioFactory;
import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.LastYearParameters;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.ConsumptionOrDemand;
import com.gly.scs.replen.ForecastOrderUpToLevel;
import com.gly.scs.replen.ForecastingMethod;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public class AmdLastYearIpPropPolicyFactory extends PolicyFactory {
	
	ConsumptionOrDemand cod = ConsumptionOrDemand.DEMAND;
	Minuend minuend = new Minuend.InventoryPosition();
	Allocation allocation = new Allocation.Proportional();

	public AmdLastYearIpPropPolicyFactory(int reportDelay) {
		super(reportDelay);
	}
	
	@Override
	public AbstractScenario getPolicy() {
		ForecastingMethod method =
				new ForecastingMethod.LastYear(
						cod,
						ExperimentParameters.getNumberOfTimestepsInYear());
		OrderUpToLevel outl = new ForecastOrderUpToLevel.Builder()
				.withForecastingMethod(method)
				.withForecastPeriods(LastYearParameters.HISTORY_TIMESTEPS)
				.withOrderUpToPeriods(LastYearParameters.ORDER_UP_TO_TIMESTEPS)
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