package com.gly.scs.leadtime;

import java.util.NoSuchElementException;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.random.*;
import com.gly.util.NegativeArray;

public class UniformSingleFacilityLeadTime extends SingleFacilityLeadTime {

	private final int min;
	private final int max;
	private NegativeArray<Integer> leadTime;
	private int startPeriod;
	private int endPeriod;

	private UniformSingleFacilityLeadTime(Builder builder) {
		this.min = builder.min;
		this.max = builder.max;
		this.startPeriod = builder.startPeriod;
		this.endPeriod = builder.endPeriod;
		RandomDataGenerator random = builder.random;

		leadTime = new NegativeArray<>(startPeriod, endPeriod);
		for (int t = startPeriod; t < endPeriod; ++t) {
			double r = random.nextUniform(min, max + 1);
			int l = (int) r;
			leadTime.set(t, l);
		}
	}

	static class Builder {
		private int startPeriod;
		private int endPeriod;
		private RandomDataGenerator random;
		private int min;
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

		public Builder withMin(int min) {
			this.min = min;
			return this;
		}

		UniformSingleFacilityLeadTime build() {
			return new UniformSingleFacilityLeadTime(this);
		}

	}

	@Override
	int getLeadTime(int t) throws NoSuchElementException {
		try {
			return leadTime.get(t);
		} catch (Exception e) {
			throw new NoSuchElementException();
		}
	}

	@Override
	IntegerRandomVariable getLeadTimeRandomVariable(int t) {
		return new DiscreteUniformRandomVariable.Builder()
		.withMin(min)
		.withMax(max)
		.build()	;
	}

	@Override
	double getAccessibility(int t) {
		return 1;
	}

}
