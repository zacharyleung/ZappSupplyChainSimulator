package com.gly.scs.replen;

import java.util.Scanner;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Facility;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;

public class LsiOrderUpToLevel extends OrderUpToLevel {

	public final double factor;
	/** The number of timesteps in a year. */
	public final int yearTimesteps;
	public final int historyTimesteps;
	/**
	 * Timesteps to add some correction in case the seasonality has
	 * shifted earlier or later.
	 */
	public final int correctionTimesteps = 4;
	private final boolean shouldPrint;
	public final Type type;

	private LsiOrderUpToLevel(Builder b) {
		this.historyTimesteps = b.historyTimesteps;
		this.factor = b.factor;
		this.yearTimesteps = b.yearTimesteps;
		this.shouldPrint = b.shouldPrint;
		this.type = b.type;
	}

	public static class Builder {
		private int historyTimesteps;
		private double factor;
		private int yearTimesteps;
		private boolean shouldPrint = false;
		private Type type;

		public Builder withHistoryTimesteps(int historyTimesteps) {
			this.historyTimesteps = historyTimesteps;
			return this;
		}

		public Builder withYearTimesteps(int i) {
			this.yearTimesteps = i;
			return this;
		}

		public Builder withType(Type t) {
			this.type = t;
			return this;
		}

		public Builder withFactor(double d) {
			this.factor = d;
			return this;
		}

		public Builder withShouldPrint(boolean b) {
			this.shouldPrint = b;
			return this;
		}

		public LsiOrderUpToLevel build() {
			return new LsiOrderUpToLevel(this);
		}
	}


	@Override
	public int getReportHistoryPeriods() {
		return Math.max(yearTimesteps + correctionTimesteps,
				historyTimesteps);
	}

	@Override
	public int getReportForecastPeriods() {
		return 0;
	}

	@Override
	public String prettyToString() {
		return String.format("LsiOrderUpToLevel(%.2f,%d)", 
				factor, historyTimesteps);
	}

	@Override
	public int getOrderUpToLevel(
			int currentTimestep, Report report,
			AbstractShipmentSchedule shipmentSchedule,
			LeadTime leadTime, DemandModel demand) {
		Facility customer = report.getFacility();
		String customerId = report.getFacilityId();
		int timestepSent = report.getPeriodSent();
		int t = currentTimestep;
		// median shipment arrival timestep of current shipment
		int t1 = ReplenUtils.getMedianSat(leadTime, customer, t);
		// median shipment arrival timestep of next shipment
		int u = t + 1;
		while (!shipmentSchedule.isShipmentPeriod(customerId, u)) {
			++u;
		}
		int t2 = ReplenUtils.getMedianSat(leadTime, customer, u);

		// if the arrival timestep of the next shipment is the same
		// as that of the current shipment, set the order-up-to level
		// of zero 
		if (t1 == t2) {
			return 0;
		}

		int u1 = t1 - correctionTimesteps - yearTimesteps;
		int u2 = t2 + correctionTimesteps - yearTimesteps;
		// don't allow the timestep u2 to exceed the current timestep - 1
		u2 = Math.min(u2, timestepSent - 1);

		// debugging code
		if (shouldPrint) {
		//if (true) {
			System.out.println("LsiOrderUpToLevel");
			System.out.println(report);
			System.out.printf("current period = %d\n", t);
			System.out.printf("period sent = %d\n", report.getPeriodSent());
			System.out.printf("t1 = %d\n", t1);
			System.out.printf("t2 = %d\n", t2);
			System.out.printf("u1 = %d\n", u1);
			System.out.printf("u2 = %d\n", u2);
			System.out.printf("getReportHistoryPeriods() = %d\n", 
					getReportHistoryPeriods());
		}

		double num = ReplenUtils.meanPastType(report, type, u1, u2);
		// Ensure that the denominator is at least 1
		double den = ReplenUtils.meanPast(report, type, historyTimesteps);
		den = Math.max(den, 1.0);
		double lsi = num / den;
		double result = factor * (t2 - t1) * lsi * den;

		//if (true) {
		if (shouldPrint) {
			System.out.println("LsiOrderUpToLevel");
			System.out.println(report);
			System.out.printf("current period = %d\n", t);
			System.out.printf("u = %d\n", u);
			System.out.printf("median shipment arrival timestep (current) = %d\n", t1);
			System.out.printf("median shipment arrival timestep (next)    = %d\n", t2);
			System.out.printf("factor = %.2f\n", factor);
			System.out.printf("timesteps = %d\n", t2 - t1);
			System.out.printf("lsi numerator = %.2f (timesteps [%d,%d))\n",
					num, u1, u2);
			System.out.printf("mean past demand = %.2f (note minimum is set to 1.0)\n", 
					den);
			System.out.printf("result = %.0f\n", result);
			
			Scanner reader = new Scanner(System.in);
			System.out.println("Press <Enter> to continue...");
			reader.nextLine();
		}

		return (int) result;
	}

}
