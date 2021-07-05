package com.gly.random;

import org.apache.commons.math3.distribution.NormalDistribution;

public class NormalRandomVariable extends AbstractRandomVariable {

	private final double mean;
	private final double standardDeviation;

	private NormalRandomVariable(Builder builder) {
		this.mean = builder.mean;
		this.standardDeviation = builder.standardDeviation;
	}
	
	public static class Builder {
		private double mean;
		private double standardDeviation;
		
		public Builder withMean(double mean) {
			this.mean = mean;
			return this;
		}
		
		public Builder withStandardDeviation(double standardDeviation) {
			this.standardDeviation = standardDeviation;
			return this;
		}
		
		public NormalRandomVariable build() {
			return new NormalRandomVariable(this);
		}
	}
	
	@Override
	public double getMean() {
		return mean;
	}

	@Override
	public double getMin() {
		return Double.MIN_VALUE;
	}

	@Override
	public double getStdDev() {
		return standardDeviation;
	}

	@Override
	public double inverseCumulativeProbability(double p) {
		NormalDistribution normal = 
				new NormalDistribution(mean, standardDeviation);
		return normal.inverseCumulativeProbability(p);
	}

	@Override
	public double partialExpectation(double k) {
		throw new IllegalArgumentException("Not implemented!");
	}
	
	public NormalRandomVariable add(NormalRandomVariable other) {
		return new NormalRandomVariable.Builder()
		.withMean(this.getMean() + other.getMean())
		.withStandardDeviation(Math.sqrt(
				Math.pow(this.getStdDev(), 2) + Math.pow(other.getStdDev(), 2)))
				.build();
	}

}
