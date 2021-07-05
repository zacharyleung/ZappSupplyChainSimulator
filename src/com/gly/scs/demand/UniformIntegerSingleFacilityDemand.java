package com.gly.scs.demand;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.random.AbstractRandomVariable;
import com.gly.random.LognormalRandomVariable;
import com.gly.util.NegativeArray;

/**
 * Demand is uniform integer random variable between 0 and max inclusive.
 * 
 * @author znhleung
 *
 */
public class UniformIntegerSingleFacilityDemand extends SingleFacilityDemand {

	private NegativeArray<Integer> demand;
	private int max;
	
	UniformIntegerSingleFacilityDemand(Builder builder) {
		this.max = builder.max;
		int startPeriod = builder.startPeriod;
		int endPeriod = builder.endPeriod;
		RandomDataGenerator random = builder.random;
		
		demand = new NegativeArray<>(startPeriod, endPeriod);
		for (int t = startPeriod; t < endPeriod; ++t) {
			double r = random.nextUniform(0, max + 1);
			int d = (int) r;
			demand.set(t, d);
		}
	}

	static class Builder {
		private int startPeriod;
		private int endPeriod;
		private RandomDataGenerator random;
		private int max;

		Builder withStartPeriod(int startPeriod) {
			this.startPeriod = startPeriod;
			return this;
		}

		Builder withEndPeriod(int endPeriod) {
			this.endPeriod = endPeriod;
			return this;
		}

		Builder withRandomDataGenerator(RandomDataGenerator random) {
			this.random = random;
			return this;
		}

		public Builder withMax(int max) {
			this.max = max;
			return this;
		}

		UniformIntegerSingleFacilityDemand build() {
			return new UniformIntegerSingleFacilityDemand(this);
		}
	}

	@Override
	int getDemand(int t) {
		return demand.get(t);
	}

	@Override
	AbstractRandomVariable getDemandForecast(GetDemandForecastParameters params) {
		return new LognormalRandomVariable.Builder()
		.withMean(max / 2.0)
		.withStandardDeviation((max * max - 1.0) / 12.0)
		.build();
	}

	@Override
	double getMeanDemand(int t) {
		return max / 2.0;
	}

}
