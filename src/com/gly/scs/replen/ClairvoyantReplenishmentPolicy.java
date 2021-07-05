package com.gly.scs.replen;

public class ClairvoyantReplenishmentPolicy extends XdockReplenishmentPolicy {
	/**
	 * The clairvoyant policy has zero reporting delay.
	 */
	public ClairvoyantReplenishmentPolicy(Builder builder) {
		super(0, new ClairvoyantReplenishmentFunction.Builder()
		.withForecastHorizon(builder.forecastHorizon)
		.withUnmetDemandCost(builder.unmetDemandCost)
		.build(), true);
	}

	public static class Builder {
		private int forecastHorizon;
		private double unmetDemandCost;
	
		public Builder withForecastHorizon(int forecastHorizon) {
			this.forecastHorizon = forecastHorizon;
			return this;
		}
	
		public Builder withUnmetDemandCost(double unmetDemandCost) {
			this.unmetDemandCost = unmetDemandCost;
			return this;
		}
		
		public ClairvoyantReplenishmentPolicy build() {
			return new ClairvoyantReplenishmentPolicy(this);
		}
	}

	/**
	 * The clairvoyant policy doesn't need any demand history.
	 */
	@Override
	public int getReportHistoryPeriods() {
		return 0;
	}
	
}
