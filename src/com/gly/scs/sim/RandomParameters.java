package com.gly.scs.sim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * Parameters used to create random objects (i.e. demand and lead time).
 * @author zacharyleung
 *
 */
public class RandomParameters {
	
	public final long randomSeed;
	public final int startPeriod;
	public final int endPeriod;
	public final long demandRandomSeed;
	public final long retailLeadTimeRandomSeed;
	public final long regionalLeadTimeRandomSeed;
	private RandomDataGenerator random;
	
	protected RandomParameters(Builder builder) {
		this.randomSeed = builder.randomSeed;
		this.startPeriod = builder.startPeriod;
		this.endPeriod = builder.endPeriod;
		
		random = new RandomDataGenerator();
		random.reSeed(randomSeed);
		demandRandomSeed = getNextSeed();
		retailLeadTimeRandomSeed = getNextSeed();
		regionalLeadTimeRandomSeed = getNextSeed();
	}
	
	private long getNextSeed() {
		return random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	public static class Builder {
		private long randomSeed;
		private int startPeriod;
		private int endPeriod;
		
		public Builder withRandomSeed(long randomSeed) {
			this.randomSeed = randomSeed;
			return this;
		}
		
		public Builder withStartPeriod(int startPeriod) {
			this.startPeriod = startPeriod;
			return this;
		}
		
		public Builder withEndPeriod(int endPeriod) {
			this.endPeriod = endPeriod;
			return this;
		}
		
		public RandomParameters build() {
			return new RandomParameters(this);
		}
	}
	
}
