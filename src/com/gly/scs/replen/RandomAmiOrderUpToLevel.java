package com.gly.scs.replen;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;
import com.gly.util.MathUtils;

/**
 * Multiply the AMI order-up-to level by a random scaling factor.
 * 
 * @author znhleung
 */
public class RandomAmiOrderUpToLevel extends OrderUpToLevel {

	private final double[] scale;
	private RandomDataGenerator random;
	private AmiOrderUpToLevel outl;
	
	private RandomAmiOrderUpToLevel(Builder b) {
		outl = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(b.historyPeriods)
				.withOrderUpToPeriods(b.orderUpToPeriods)
				.withType(b.type)
				.build();
		random = new RandomDataGenerator();
		random.reSeed(b.randomSeed);
		scale = b.scale;
	}
	
	public static class Builder {
		private int historyPeriods;
		private double orderUpToPeriods;
		private Type type;
		private long randomSeed;
		private double[] scale;
		
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
		
		public Builder withRandomSeed(long l) {
			this.randomSeed = l;
			return this;
		}
		
		public Builder withScale(double[] scale) {
			this.scale = MathUtils.copyOf(scale);
			return this;
		}
		
		public RandomAmiOrderUpToLevel build() {
			return new RandomAmiOrderUpToLevel(this);
		}

	}

	@Override
	public int getReportHistoryPeriods() {
		return outl.getReportHistoryPeriods();
	}

	@Override
	public int getReportForecastPeriods() {
		return outl.getReportForecastPeriods();
	}

	@Override
	public String prettyToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOrderUpToLevel(int currentTimestep, Report report,
			AbstractShipmentSchedule shipmentSchedule,
			LeadTime leadTime, DemandModel demand) {
		double r = MathUtils.sample(random, scale);
		double result = r * outl.getOrderUpToLevel(currentTimestep, report, 
				shipmentSchedule, leadTime, demand);
		
//		System.out.printf("RandomAmiOrderUpToLevel.getOrderUpToLevel(%d)\n",
//				currentTimestep);
//		System.out.printf("scale = %.2f\n", r);
		
		return (int) result;
	}


}
