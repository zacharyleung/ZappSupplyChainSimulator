package com.gly.scs.exp.frontier;

import com.gly.scs.exp.exp08.ParametrizedScenarioFamily;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.OptimizationReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.XdockScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class OptimizationFamily extends ParametrizedScenarioFamily {

	private final int oneTierReportDelay = 0;
	private final int forecastLevel = 
			ZambiaSimulationParameters.FORECAST_LEVEL_INDUSTRY; 
	private final HoldingCostType holdingCostType;
	private final double shipmentLeadTimePercentile;

	public OptimizationFamily(String name, double[] d, 
			HoldingCostType holdingCostType,
			double shipmentLeadTimePercentile) {
		super(String.format("opt-%s-%.3f",
				holdingCostType.getName(), shipmentLeadTimePercentile),
				d);
		this.holdingCostType = holdingCostType;
		this.shipmentLeadTimePercentile = shipmentLeadTimePercentile;
	}

	@Override
	public AbstractScenario getScenario(double parameter) {
		double unmetDemandCost = parameter;

		UnmetDemandLowerBoundFactory tangentFactory =
				new UnmetDemandLowerBoundPercentileFactory(
						ZambiaSimulationParameters.Optimization.NUMBER_OF_TANGENTS);

		OptimizationReplenishmentFunction replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
				.withForecastLevel(forecastLevel)
				.withForecastHorizon(ZambiaSimulationParameters.
						OPTIMIZATION_REPLENISHMENT_POLICY_FORECAST_HORIZON)
				.withUnmetDemandCost(unmetDemandCost)
				.withTangentFactory(tangentFactory)
				.withHoldingCostType(holdingCostType)
				.withShipmentLeadTimePercentile(shipmentLeadTimePercentile)
				.withShipmentLeadTimeType(ZambiaSimulationParameters.
						OPTIMIZATION_REPLENISHMENT_POLICY_LEAD_TIME_TYPE)
				.withUnmetDemandLowerBoundType(ZambiaSimulationParameters
						.UNMET_DEMAND_LOWER_BOUND_TYPE)
				.build();

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						replenishmentFunction);

		return new XdockScenario.Builder()
				.withName(name)
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}

}

