package com.gly.scs.replen;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;
import com.gly.util.StringUtils;

/**
 * Compute the order-up-to level using the average monthly issues (AMI).
 * 
 * @author zacharyleung
 *
 */
public class AmiOrderUpToLevel extends OrderUpToLevel {

	public final int historyPeriods;
	public final double orderUpToPeriods;
	public final Type type;
	
	private AmiOrderUpToLevel(Builder b) {
		this.historyPeriods = b.historyPeriods;
		this.orderUpToPeriods = b.orderUpToPeriods;
		this.type = b.type;
		
		if (type == null) {
			System.out.println("type = null");
			System.out.println("Need to specify type = Type.CONSUMPTION or Type.DEMAND");
			System.out.println("in AmiOrderUpToLevel.Builder");
			throw new IllegalArgumentException("type = null");
		}
	}
	
	public static class Builder {
		private int historyPeriods;
		private double orderUpToPeriods;
		private Type type;
		
		public Builder withHistoryPeriods(int historyPeriods) {
			this.historyPeriods = historyPeriods;
			return this;
		}
		
		public Builder withOrderUpToPeriods(double orderUpToPeriods) {
			this.orderUpToPeriods = orderUpToPeriods;
			return this;
		}
		
		public Builder withType(Type type) {
			this.type = type;
			return this;
		}
		
		public AmiOrderUpToLevel build() {
			return new AmiOrderUpToLevel(this);
		}

	}
	
	@Override
	public int getOrderUpToLevel(
			int currentTimestep, Report report,
			AbstractShipmentSchedule shipmentSchedule,
			LeadTime leadTime, DemandModel demand) {
		double d = ReplenUtils.meanPast(report, type, historyPeriods);
		double out = d * orderUpToPeriods;
		return (int) Math.round(out);
	}
	
	@Override
	public int getReportHistoryPeriods() {
		return historyPeriods;
	}

	@Override
	public int getReportForecastPeriods() {
		return 0;
	}

	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "{" + NL);
		sb.append(PAD + "history periods = " + historyPeriods + NL);
		sb.append(PAD + "order up to periods = " + orderUpToPeriods + NL);
		sb.append("}");
		return sb.toString();
	}

	
}
