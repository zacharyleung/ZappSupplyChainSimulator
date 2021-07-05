package com.gly.random;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public class LognormalRandomVariable extends AbstractRandomVariable {

	public final LogNormalDistribution lognormal;
	
	private LognormalRandomVariable(Builder builder) {
		// mu and sigma of the normal random variable
		double mean = builder.mean;
		double std = builder.standardDeviation;
		
		double sigma = Math.sqrt(Math.log(std * std / (mean * mean) + 1));
		double mu = Math.log(mean) - sigma * sigma / 2;
		
		this.lognormal = new LogNormalDistribution(mu, sigma);
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
		
		public LognormalRandomVariable build() {
			return new LognormalRandomVariable(this);
		}
	}

	@Override
	public double getMean() {
		return lognormal.getNumericalMean();
	}

	@Override
	public double getMin() {
		return 0;
	}

	@Override
	public double getStdDev() {
		return Math.sqrt(lognormal.getNumericalVariance());
	}

	@Override
	public double inverseCumulativeProbability(double p) {
		return lognormal.inverseCumulativeProbability(p);
	}

	@Override
	public double partialExpectation(double k) {
		throw new IllegalArgumentException("Not implemented!");
	}

}
