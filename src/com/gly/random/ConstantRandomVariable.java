package com.gly.random;

public class ConstantRandomVariable extends AbstractRandomVariable {

	private double value;

	public ConstantRandomVariable(double value) {
		this.value = value;
	}

	@Override
	public double getMean() {
		return value;
	}

	@Override
	public double getMin() {
		return value;
	}

	@Override
	public double inverseCumulativeProbability(double p) {
		return value;
	}

	@Override
	public double getStdDev() {
		return 0;
	}

	@Override
	public double partialExpectation(double k) {
		if (k < value) {
			return value;
		} else {
			return 0;
		}
	}

}
