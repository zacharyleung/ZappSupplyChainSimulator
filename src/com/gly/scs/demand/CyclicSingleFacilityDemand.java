package com.gly.scs.demand;

import com.gly.util.MathUtils;

/**
 * This class implements the method getDemandMean for demand models
 * where the mean demand is cyclic.  For example, if the mean demand
 * is (1, 2, 3, 4, 1, 2, 3, 4, ...) then the mean demand is cyclic. 
 * @author zacleung
 *
 */
public abstract class CyclicSingleFacilityDemand extends SingleFacilityDemand {
	
	private final double[] demandMean;

	protected CyclicSingleFacilityDemand(double[] demandMean) {
		this.demandMean = demandMean;
	}
	
	@Override
	double getMeanDemand(int t) {
		return demandMean[MathUtils.positiveModulo(t, demandMean.length)];
	}

}
