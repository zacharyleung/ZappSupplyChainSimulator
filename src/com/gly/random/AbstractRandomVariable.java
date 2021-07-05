package com.gly.random;

public abstract class AbstractRandomVariable {
	public abstract double getMean();
	
	public abstract double getMin();
	
	public abstract double getStdDev();
	
	public abstract double inverseCumulativeProbability(double p);
	
	public abstract double partialExpectation(double k);
	
	public double getCoefficientOfVariation() {
		return getStdDev() / getMean();
	}
}
