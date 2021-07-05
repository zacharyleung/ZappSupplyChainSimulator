package com.gly.scs.exp.policy;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.OptimizationParameters;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.OptimizationReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.XdockScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class OptimizationPolicy extends PolicyFactory {

	public OptimizationPolicy(int reportDelay) {
		super(reportDelay);
	}

	private String name = OptimizationParameters.NAME;
	private HoldingCostType holdingCostType = OptimizationParameters.HOLDING_COST_TYPE;
	
	public OptimizationPolicy withName(String name) {
		this.name = name;
		return this;
	}
	
	public OptimizationPolicy withHoldingCostType(HoldingCostType h) {
		this.holdingCostType = h;
		return this;
	}
	
	@Override
	public AbstractScenario getPolicy() {
		System.out.println("ZambiaPerformance.getOptimizationScenario(");
		System.out.printf("  name = %s%n", name);
//		System.out.printf("  reportDelay = %d%n", reportDelay);
//		System.out.printf("  forecastLevel = %d%n", forecastLevel);
//		System.out.println(")");

		UnmetDemandLowerBoundFactory udlbFactory =
				new UnmetDemandLowerBoundPercentileFactory(
						OptimizationParameters.NUMBER_OF_TANGETS);

		OptimizationReplenishmentFunction replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
				.withForecastLevel(OptimizationParameters.FORECAST_LEVEL)
				.withForecastHorizon(ExperimentParameters.getNumberOfTimestepsInYear())
				.withUnmetDemandCost(OptimizationParameters.UNMET_DEMAND_COST)
				.withHoldingCostType(holdingCostType)
				.withTangentFactory(udlbFactory)
				.withShipmentLeadTimePercentile(
						OptimizationParameters.LEAD_TIME_PERCENTILE)
				.withShipmentLeadTimeType(OptimizationParameters.LEAD_TIME_TYPE)
				.withUnmetDemandLowerBoundType(
						OptimizationParameters.UNMET_DEMAND_LOWER_BOUND_TYPE)
				.build();

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(reportDelay,
						replenishmentFunction);

		return new XdockScenario.Builder()
				.withName(getPolicyName())
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ExperimentParameters.getRetailInitialInventoryLevel())
				.build();
	}

}
