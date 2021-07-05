package com.gly.scs.leadtime;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.random.*;
import com.gly.random.IntegerRandomVariable.Entry;
import com.gly.util.MathUtils;
import com.gly.util.NegativeArray;

public class VehicleAccessibilitySingleFacilityLeadTime extends
		SingleFacilityLeadTime {

	public static enum MinimumValue {
		ZERO(0), ONE(1);
		
		MinimumValue(int value) {
			this.value = value;
		}
		
		private int value;
		
		public int intValue() {
			return value;
		}
	};
	
	private final MinimumValue minimumValue;
	private final double mean;
	private final int startPeriod;
	private final int endPeriod;
	private double[] accessibility;
	/**
	 * visit.get(t) = true if regional facility makes visit in period t.
	 */
	private NegativeArray<Boolean> hasVisit;
	
	private VehicleAccessibilitySingleFacilityLeadTime(Builder builder) {
		this.startPeriod = builder.startPeriod;
		this.endPeriod = builder.endPeriod;
		RandomDataGenerator random = builder.random;
		this.accessibility = builder.accessibility;
		this.mean = builder.mean;
		this.minimumValue = builder.minimumValue;

		if (minimumValue == null) {
			throw new IllegalArgumentException("minimumValue = null");
		}
		
		// check that each entry of the accessibility vector is in [0,1]
		for (int t = 0; t < accessibility.length; ++t) {
			if (accessibility[t] < 0 || accessibility[t] > 1) {
				throw new IllegalArgumentException(
						String.format("accessibility[%d] = %.2f should be in [0,1]!",
								t, accessibility[t]));
			}
		}
		
		hasVisit = new NegativeArray<>(startPeriod, endPeriod);
		for (int t = startPeriod; t < endPeriod; ++t) {
			double r = random.nextUniform(0.0, 1.0);
			double vp = visitProbability(t);
			//System.out.printf("visit probability[%d] = %.2f%n", t, vp);
			hasVisit.set(t, r < vp);
		}
	}
	
	static class Builder {
		private MinimumValue minimumValue;
		private int startPeriod;
		private int endPeriod;
		private RandomDataGenerator random;
		private double mean;
		private double[] accessibility;
		
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

		public Builder withMean(double mean) {
			this.mean = mean;
			return this;
		}

		public Builder withMinimumValue(MinimumValue minimumValue) {
			this.minimumValue = minimumValue;
			return this;
		}
		
		public Builder withAccessibility(double[] accessibility) {
			this.accessibility =
					Arrays.copyOf(accessibility, accessibility.length);
			return this;
		}

		public VehicleAccessibilitySingleFacilityLeadTime build() {
			return new VehicleAccessibilitySingleFacilityLeadTime(this);
		}

	}
	
	private double visitProbability(int t) {
		// probability that vehicle is available in each week
		
		double v = 0;
		switch (minimumValue) {
		case ZERO:
			v = 1 / (mean + 1);
			break;
		case ONE:
			v = 1 / mean;
			break;
		}
		return v * getAccessibility(t);
	}
	
	@Override
	int getLeadTime(int period) throws NoSuchElementException {
		for (int t = period + minimumValue.intValue(); t < endPeriod; ++t) {
			if (hasVisit.get(t)) {
				return t - period;
			}
		}
		String errorMessage = String.format(
				"startPeriod = %d, endPeriod = %d, period = %d",
				startPeriod, endPeriod, period);
		throw new NoSuchElementException(errorMessage);
	}

	@Override
	IntegerRandomVariable getLeadTimeRandomVariable(int period) {
		LinkedList<Entry> entries = new LinkedList<>();
		// remaining probability
		double rp = 1.0;
		int t;
		for (t = period + minimumValue.intValue(); t < endPeriod; ++t) {
			double vp = visitProbability(t);
			if (vp > 0) {
				entries.add(new Entry.Builder()
				.withValue(t - period)
				.withProbability(vp * rp)
				.build());
				rp = rp * (1 - vp);
			}

			// if the remaining probability is too small, then it is
			// time to quit
			if (rp < EmpiricalIntegerRandomVariable.EPSILON) {
				break;
			}
		}
		
		entries.add(new Entry.Builder()
		.withValue(t + 1 - period)
		.withProbability(rp)
		.build());
		
		return new EmpiricalIntegerRandomVariable(entries);
	}

	@Override
	double getAccessibility(int t) {
		int C = accessibility.length;
		return accessibility[MathUtils.positiveModulo(t, C)];
	}

}
