package com.gly.scs.opt;

/**
 * Representing an unmet demand lower bound
 *     U >= slope x I + intercept
 * where U = unmet demand, I = inventory level. 
 * @author zacharyleung
 *
 */
public class UnmetDemandLowerBound {
	public final double intercept;
	public final double slope;
	
	private UnmetDemandLowerBound(Builder builder) {
		this.intercept = builder.intercept;
		this.slope = builder.slope;
	}
	
	public static class Builder {
		private double intercept;
		private double slope;
		
		public Builder withIntercept(double intercept) {
			this.intercept = intercept;
			return this;
		}
		
		public Builder withSlope(double slope) {
			this.slope = slope;
			return this;
		}
		
		public UnmetDemandLowerBound build() {
			return new UnmetDemandLowerBound(this);
		}
	}
	
	@Override
	public String toString() {
		return String.format("Tangent{slope = %.2f, intercept = %.2f}",
				slope, intercept);
	}
}
