package com.gly.scs.replen;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.demand.GetDemandForecastParameters;
import com.gly.scs.domain.Report;

public abstract class ForecastingMethod {
	/** Forecast the mean demand in period t . */
	public abstract double getMeanDemand(
			DemandModel demand, Report report, int t);
	
	/**
	 * Return the number of history timesteps needed by this
	 * forecasting method.
	 */
	public abstract int getHistoryTimesteps();
	
	public static class Statistical extends ForecastingMethod {
		private final int forecastLevel;
		
		public Statistical(int forecastLevel) {
			this.forecastLevel = forecastLevel;
		}
		
		@Override
		public double getMeanDemand(DemandModel demand, Report report, int t) {
			int currentTimestep = report.getPeriodSent();
			String facility = report.getFacilityId();
			GetDemandForecastParameters p =
					new GetDemandForecastParameters.Builder()
					.withCurrentTimestep(currentTimestep)
					.withForecastLevel(forecastLevel)
					.withFutureTimestep(t)
					.withRetailFacility(facility)
					.build();
			return demand.getDemandForecast(p).getMean();
		}

		@Override
		public int getHistoryTimesteps() {
			return 0;
		}
	}

	/**
	 * To predict the demand in timestep t, use the demand in the
	 * previous year.
	 * @todo Can implement type = demand/consumption here.
	 */
	public static class LastYear extends ForecastingMethod {
		private final int yearTimesteps;
		private ConsumptionOrDemand cod;
		
		public LastYear(ConsumptionOrDemand cod, int yearTimesteps) {
			this.cod = cod;
			this.yearTimesteps = yearTimesteps;
		}
		
		@Override
		public double getMeanDemand(DemandModel demand, Report report, int t) {
			switch (cod) {
			case CONSUMPTION:
				return report.getConsumptionAtTimestep(t - yearTimesteps);
			default: //case DEMAND:
				String facility = report.getFacilityId();
				return demand.getDemand(facility, t - yearTimesteps);
			}
		}

		@Override
		public int getHistoryTimesteps() {
			return yearTimesteps;
			//return 0;
		}
	}
}
