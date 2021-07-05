package com.gly.scs.exp.exp08;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.exp.param.OptimizationParameters;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.OptimizationReplenishmentFunction;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.XdockScenario;

public class OptimizationFamily extends ParametrizedScenarioFamily {

	private final int reportDelay;
	private final int forecastLevel;
	private final double shipmentLeadTimePercentile;
	private final HoldingCostType holdingCostType;
	
	private OptimizationFamily(Builder b, double[] parameters) {
		super(b.name, parameters);
		this.reportDelay = b.reportDelay;
		this.forecastLevel = b.forecastLevel;
		this.shipmentLeadTimePercentile = b.shipmentLeadTimePercentile;
		this.holdingCostType = b.holdingCostType;
	}
	
	public static class Builder {
		private int reportDelay = -1;
		private int forecastLevel = -1;
		private String name = null;
		private double shipmentLeadTimePercentile = -1;
		private HoldingCostType holdingCostType = null;
		
		public Builder withReportDelay(int i) {
			this.reportDelay = i;
			return this;
		}

		public Builder withForecastLevel(int i) {
			this.forecastLevel = i;
			return this;
		}

		public Builder withShipmentLeadTimePercentile(double d) {
			this.shipmentLeadTimePercentile = d;
			return this;
		}

		public Builder withName(String s) {
			this.name = s;
			return this;
		}
		
		public Builder withHoldingCostType(HoldingCostType h) {
			this.holdingCostType = h;
			return this;
		}
		
		public OptimizationFamily build(double[] parameters) {
			return new OptimizationFamily(this, parameters);
		}

	}
	
	@Override
	public AbstractScenario getScenario(
			double parameter, ExperimentParameters expParameters) {
		UnmetDemandLowerBoundFactory udlbFactory =
				new UnmetDemandLowerBoundPercentileFactory(
						OptimizationParameters.NUMBER_OF_TANGETS);

		OptimizationReplenishmentFunction replenishmentFunction =
				new OptimizationReplenishmentFunction.Builder()
				.withForecastLevel(forecastLevel)
				.withForecastHorizon(expParameters.getNumberOfTimestepsInYear())
				.withUnmetDemandCost(parameter)
				.withHoldingCostType(holdingCostType)
				.withTangentFactory(udlbFactory)
				.withShipmentLeadTimePercentile(
						shipmentLeadTimePercentile)
				.withShipmentLeadTimeType(OptimizationParameters.LEAD_TIME_TYPE)
				.withUnmetDemandLowerBoundType(
						OptimizationParameters.UNMET_DEMAND_LOWER_BOUND_TYPE)
				.build();

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(reportDelay,
						replenishmentFunction);

		return new XdockScenario.Builder()
				.withName(name)
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						expParameters.getRetailInitialInventoryLevel())
				.build();
	}

}
