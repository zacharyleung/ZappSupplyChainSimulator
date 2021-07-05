package com.gly.random;

import java.util.List;

/**
 * A random variable which takes the integer values 0, 1, 2, ...
 * @author zacharyleung
 *
 */
public abstract class IntegerRandomVariable {

	public abstract int getMin();
	
	/**
	 * 
	 * @param p
	 * @return
	 */
	public abstract int inverseCumulativeProbability(double p);
	
	/**
	 * For a random variable X whose values are distributed according to
	 * this distribution, this method returns P(X = x).
	 * @param x
	 * @return
	 */
	public abstract double probability(int x);
	
	/**
	 * Return a list of (value, probability) entries representing the
	 * probability that the random variable takes each value.  The list
	 * is sorted in increasing order. 
	 * @return
	 */
	public abstract List<Entry> getProbabilities();
	
	public static class Entry {
		public final int value;
		public final double probability;

		private Entry(Builder builder) {
			this.value = builder.value;
			this.probability = builder.probability;
			if (probability < 0 || probability > 1) {
				throw new IllegalArgumentException(
						"Invalid probability = " + probability);
			}
		}
		
		public static class Builder {
			private int value;
			private double probability;

			public Builder withValue(int value) {
				this.value = value;
				return this;
			}
			
			public Builder withProbability(double probability) {
				this.probability = probability;
				return this;
			}
			
			public Entry build() {
				return new Entry(this);
			}
		}
		
		@Override
		public String toString() {
			return String.format("Entry{value = %d, probability = %.2e}", 
					value, probability);
		}
	}
	
	
}
