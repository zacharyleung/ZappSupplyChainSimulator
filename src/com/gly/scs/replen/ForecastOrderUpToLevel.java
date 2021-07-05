package com.gly.scs.replen;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;

public class ForecastOrderUpToLevel extends OrderUpToLevel {
	/** Number of timesteps to compute the mean future demand. */
	private final int forecastPeriods;
	public final double orderUpToPeriods;
	private final boolean shouldPrint;
	private final ForecastingMethod method;
	
	private ForecastOrderUpToLevel(Builder b) {
		this.forecastPeriods = b.forecastPeriods;
		this.orderUpToPeriods = b.orderUpToPeriods;
		this.shouldPrint = b.shouldPrint;
		this.method = b.method;
		
		if (forecastPeriods < 0) {
			throw new IllegalArgumentException();
		}
	}

	public static class Builder {
		private int forecastPeriods;
		private double orderUpToPeriods;
		private boolean shouldPrint = false;
		private ForecastingMethod method;
		
		public Builder withForecastPeriods(int forecastPeriods) {
			this.forecastPeriods = forecastPeriods;
			return this;
		}

		public Builder withOrderUpToPeriods(double orderUpToPeriods) {
			this.orderUpToPeriods = orderUpToPeriods;
			return this;
		}
		
		public Builder withForecastingMethod(ForecastingMethod f) {
			this.method = f;
			return this;
		}
		
		public Builder withShouldPrint(boolean b) {
			this.shouldPrint = b;
			return this;
		}

		public ForecastOrderUpToLevel build() {
			return new ForecastOrderUpToLevel(this);
		}
	}

	@Override
	public int getOrderUpToLevel(
			int currentTimestep, Report report,
			AbstractShipmentSchedule shipmentSchedule,
			LeadTime leadTime, DemandModel demand) {
		//System.out.println("ForecastOrderUpToLevel.getOrderUpToLevel()");
		//System.out.println(report);

		int t = report.getPeriodSent();
		double sum = 0;
		// add the integer parts of the forecast
		for (int i = 0; i < forecastPeriods; ++i) {
			sum += method.getMeanDemand(demand, report, t + i);
		}
		double mean = sum / forecastPeriods;

		int result = (int) (orderUpToPeriods * mean); 

		if (shouldPrint) {
			System.out.printf("ForecastOrderUpToLevel.getOrderUpToLevel()\n");
			System.out.printf("mean demand forecast [%d,%d) = %.1f\n",
					currentTimestep, currentTimestep + forecastPeriods, mean);
			System.out.printf("order-up-to level = %d\n", result);
		}

		return result;
	}

	@Override
	public int getReportHistoryPeriods() {
		return method.getHistoryTimesteps();
	}

	@Override
	public int getReportForecastPeriods() {
		return forecastPeriods;
	}

	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "{" + NL);
		sb.append(PAD + "forecast periods = " + forecastPeriods + NL);
		sb.append("}");
		return sb.toString();
	}

}
