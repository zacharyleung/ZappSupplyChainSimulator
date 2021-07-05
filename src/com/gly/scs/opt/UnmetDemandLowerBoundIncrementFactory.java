package com.gly.scs.opt;

import java.util.LinkedList;
import java.util.List;

import com.gly.random.AbstractRandomVariable;

public class UnmetDemandLowerBoundIncrementFactory extends UnmetDemandLowerBoundFactory {
	private final double increment;
	private final double epsilon;
	
	private UnmetDemandLowerBoundIncrementFactory(Builder builder) {
		this.increment = builder.increment;
		this.epsilon = builder.epsilon;
	}
	
	public static class Builder {
		private double increment;
		private double epsilon;
		
		public Builder withIncrement(double increment) {
			this.increment = increment;
			return this;
		}
		
		public Builder withEpsilon(double epsilon) {
			this.epsilon = epsilon;
			return this;
		}
		
		public UnmetDemandLowerBoundIncrementFactory build() {
			return new UnmetDemandLowerBoundIncrementFactory(this);
		}
	}
	
	/**
	 * Compute tangents assuming that the demand random variable is
	 * lognormally distributed.
	 * @param randomVariable
	 * @return
	 */
	@Override
	public List<UnmetDemandLowerBound> subComputeTangents(AbstractRandomVariable randomVariable) {
		LinkedList<UnmetDemandLowerBound> tangents = new LinkedList<>();
		
		double inv = 0;
		double unmet = randomVariable.getMean();
		while (unmet > epsilon) {
			double newInv = inv + increment;
			double newUnmet = unmetDemand(newInv, randomVariable);
			
			double slope = (newUnmet - unmet) / increment;
			double intercept = newUnmet - slope * newInv;
			UnmetDemandLowerBound tangent = new UnmetDemandLowerBound.Builder()
			.withSlope(slope)
			.withIntercept(intercept)
			.build();
			tangents.addLast(tangent);
			
			inv = newInv;
			unmet = newUnmet;
		}
		
		return tangents;
	}
	
}
