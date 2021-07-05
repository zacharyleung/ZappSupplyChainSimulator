package com.gly.random;

import java.util.List;

public class PoissonRandomVariable extends AbstractRandomVariable {
	private final double mean;
	public PoissonRandomVariable(double mean) {
		this.mean = mean;
	}
	
	@Override
	public double getMean() {
		return mean;
	}
	
	@Override
	public double getMin() {
		return 0;
	}
	
	@Override
	public double getStdDev() {
		return Math.sqrt(mean);
	}

	@Override
	public double inverseCumulativeProbability(double p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double partialExpectation(double k) {
		// TODO Auto-generated method stub
		return 0;
	}
}
