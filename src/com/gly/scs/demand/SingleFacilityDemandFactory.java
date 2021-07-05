package com.gly.scs.demand;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;

public abstract class SingleFacilityDemandFactory {
	public abstract SingleFacilityDemand build(String facilityId,
			RandomParameters randomParameters, 
			RandomDataGenerator random);
	
	/**
	 * Return the mean total demand per period (i.e. the sum of the
	 * demand over every retail facility).
	 * @return
	 */
	public abstract double getMeanDemandPerPeriod();
}
