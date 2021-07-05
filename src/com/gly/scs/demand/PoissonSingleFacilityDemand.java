package com.gly.scs.demand;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.random.AbstractRandomVariable;
import com.gly.random.PoissonRandomVariable;
import com.gly.util.MathUtils;
import com.gly.util.NegativeArray;

/**
 * If cycle = 2, low = 10, high = 20, then mean demand is
 * (10,10,20,20,10,10,20,20,...).
 * @author zacleung
 *
 */
public class PoissonSingleFacilityDemand extends SingleFacilityDemand {

	private int cycle;
	private int low;
	private int high;
	private int startTimestep;
	private int endTimestep;
	private NegativeArray<Integer> demand;
	
	private PoissonSingleFacilityDemand(Builder b) {
		this.cycle = b.cycle;
		this.low = b.low;
		this.high = b.high;
		this.startTimestep = b.startTimestep;
		this.endTimestep = b.endTimestep;
		RandomDataGenerator random = b.random;
		demand = new NegativeArray<>(startTimestep, endTimestep);
		for (int i = startTimestep; i < endTimestep; ++i) {
			demand.set(i, (int) random.nextPoisson(getMeanDemand(i)));
		}
	}
	
	public static class Builder {
		private int cycle;
		private int low;
		private int high;
		private int startTimestep;
		private int endTimestep;
		private RandomDataGenerator random;

		public Builder withCycle(int i) {
			this.cycle = i;
			return this;
		}
		
		public Builder withLow(int i) {
			this.low = i;
			return this;
		}
		
		public Builder withHigh(int i) {
			this.high = i;
			return this;
		}
		
		public Builder withStartTimestep(int i) {
			this.startTimestep = i;
			return this;
		}
		
		public Builder withEndTimestep(int i) {
			this.endTimestep = i;
			return this;
		}
		
		public Builder withRandomDataGenerator(RandomDataGenerator random) {
			this.random = random;
			return this;
		}
		
		public PoissonSingleFacilityDemand build() {
			return new PoissonSingleFacilityDemand(this);
		}
		
	}
	
	@Override
	int getDemand(int t) {
		return demand.get(t);
	}
	
	@Override
	double getMeanDemand(int t) {
		int i = MathUtils.positiveModulo(t, 2 * cycle);
		if (i < cycle) {
			return low;
		} else {
			return high;
		}
	}

	@Override
	AbstractRandomVariable getDemandForecast(GetDemandForecastParameters params) {
		return new PoissonRandomVariable(getMeanDemand(params.futurePeriod));
	}
	
	
}
