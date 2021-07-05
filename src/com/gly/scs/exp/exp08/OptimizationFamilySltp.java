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

/**
 * Generate optimization policies with a fixed shipment lead time
 * percentile.
 * 
 * @author znhleung
 */
public class OptimizationFamilySltp extends ParametrizedScenarioFamily {

	private int reportDelay = 0;
	private int forecastLevel = ExpParameters.FORECAST_LEVEL_GOOD;
	private final int numberOfLowerBounds;
	private double shipmentLeadTimePercentile;

	public OptimizationFamilySltp(double[] parameters, 
			double shipmentLeadTimePercentile, int numberOfLowerBounds) {
		super(String.format("opt-sltp-%.2f", shipmentLeadTimePercentile),
				parameters);
		this.shipmentLeadTimePercentile = shipmentLeadTimePercentile;
		this.numberOfLowerBounds = numberOfLowerBounds;
	}

	@Override
	public AbstractScenario getScenario(double parameter) {
		UnmetDemandLowerBoundFactory udlbFactory =
				new UnmetDemandLowerBoundPercentileFactory(
						numberOfLowerBounds);

		OptimizationReplenishmentFunction replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
				.withForecastLevel(forecastLevel)
				.withForecastHorizon(Optimization.FORECAST_HORIZON)
				.withUnmetDemandCost(parameter)
				.withHoldingCostType(HoldingCostType.CONSTANT)
				.withTangentFactory(udlbFactory)
				.withShipmentLeadTimePercentile(shipmentLeadTimePercentile)
				.withShipmentLeadTimeType(Optimization.LEAD_TIME_TYPE)
				.withUnmetDemandLowerBoundType(
						Optimization.UNMET_DEMAND_LOWER_BOUND_TYPE)
				.build();

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(reportDelay,
						replenishmentFunction);

		System.out.printf("OptSltp{unmet demand cost=%.1f,lead time percentile=%.2f}%n",
				parameter,
				shipmentLeadTimePercentile);
		
		return new XdockScenario.Builder()
				.withName(name)
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ExpParameters.RETAIL_INITIAL_INVENTORY_LEVEL)
				.build();
	}

}
