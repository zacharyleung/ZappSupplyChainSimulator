package com.gly.random;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a discrete uniform random variable which takes values in the
 * set {min, min + 1, ..., max}.
 * @author zacharyleung
 *
 */
public class DiscreteUniformRandomVariable extends IntegerRandomVariable {

	private int min;
	private int max;

	private DiscreteUniformRandomVariable(Builder builder) {
		this.min = builder.min;
		this.max = builder.max;

		if (min < 0) {
			throw new IllegalArgumentException(
					String.format("min = %d < 0!", min));
		}
	}

	@Override
	public int inverseCumulativeProbability(double p) {
		//System.out.printf("p = %.2f\n", p);
		//System.out.printf("r = %.2f\n", min + p * (max - min + 1));
		return (int) (min + p * (max - min + 1));
	}

	public static class Builder {
		private int min;
		private int max;

		public Builder withMin(int min) {
			this.min = min;
			return this;
		}

		public Builder withMax(int max) {
			this.max = max;
			return this;
		}

		public DiscreteUniformRandomVariable build() {
			return new DiscreteUniformRandomVariable(this);
		}
	}

	@Override
	public double probability(int x) {
		if (x >= min && x <= max) {
			return 1.0 / (max - min + 1);
		} else {
			return 0;
		}
	}

	@Override
	public List<Entry> getProbabilities() {
		LinkedList<Entry> entries = new LinkedList<>();
		for (int i = min; i <= max; ++i) {
			entries.addLast(new Entry.Builder()
			.withValue(i)
			.withProbability(1.0 / (max - min + 1))
			.build());
		}
		return entries;
	}

	@Override
	public int getMin() {
		return min;
	}

}
