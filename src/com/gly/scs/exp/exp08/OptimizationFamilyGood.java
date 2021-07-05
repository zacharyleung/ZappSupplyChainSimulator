package com.gly.scs.exp.exp08;

import com.gly.scs.exp.param.ExpParameters;
import com.gly.scs.exp.param.ExpParameters.Optimization;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.OptimizationReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.XdockScenario;
import com.gly.util.LinearInterpolation;

public class OptimizationFamilyGood extends OptimizationFamilyVariable {

	private int reportDelay = 0;
	private int forecastLevel = ExpParameters.FORECAST_LEVEL_GOOD;
	private static final String name = "opt-good";
	
	public OptimizationFamilyGood(double[] parameters, int numberOfLowerBounds) {
		super(name, parameters, numberOfLowerBounds);
	}

	@Override
	public AbstractScenario getScenario(double parameter) {
		UnmetDemandLowerBoundFactory udlbFactory =
				new UnmetDemandLowerBoundPercentileFactory(
						getNumberOfLowerBounds());

		OptimizationReplenishmentFunction replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
				.withForecastLevel(forecastLevel)
				.withForecastHorizon(Optimization.FORECAST_HORIZON)
				.withUnmetDemandCost(parameter)
				.withHoldingCostType(getHoldingCostType(parameter))
				.withTangentFactory(udlbFactory)
				.withShipmentLeadTimePercentile(
						getLeadTimePercentile(parameter))
				.withShipmentLeadTimeType(Optimization.LEAD_TIME_TYPE)
				.withUnmetDemandLowerBoundType(
						Optimization.UNMET_DEMAND_LOWER_BOUND_TYPE)
				.build();

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(reportDelay,
						replenishmentFunction);

		System.out.printf("Opt{unmet demand cost=%.1f,lead time percentile=%.2f}%n",
				parameter,
				getLeadTimePercentile(parameter));
		
		return new XdockScenario.Builder()
				.withName(name)
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ExpParameters.RETAIL_INITIAL_INVENTORY_LEVEL)
				.build();
	}
	
}
