package com.gly.scs.exp.exp08;

import com.gly.scs.opt.HoldingCostType;
import com.gly.util.LinearInterpolation;

/**
 * Parameters such as the holding cost type and lead time percentile
 * are a function of the parameter which is the unmet demand cost.
 * 
 * @author znhleung
 */
public abstract class OptimizationFamilyVariable extends ParametrizedScenarioFamily {

	private final int numberOfLowerBounds; 
	
	protected OptimizationFamilyVariable(String name, double[] parameters,
			int numberOfLowerBounds) {
		super(name, parameters);
		this.numberOfLowerBounds = numberOfLowerBounds;
	}

	protected HoldingCostType getHoldingCostType(double unmetDemandCost) {
		if (unmetDemandCost <= 16) {
			return HoldingCostType.CONSTANT;
		} else {
			return HoldingCostType.FACILITY_ACCESSIBILITY;
		}
		//return HoldingCostType.CONSTANT;
	}
	
	protected double getLeadTimePercentile(double unmetDemandCost) {
		LinearInterpolation linear = new LinearInterpolation(
				new double[]{0, 5, 10, 15, 20, 24}, 
				new double[]{0.5, 0.5, 0.8, 0.9, 0.95, 0.99});
		return linear.getValue(unmetDemandCost);
	}
	
	public int getNumberOfLowerBounds() {
		return numberOfLowerBounds;
	}

}
