package com.gly.scs.demand;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;

public abstract class AbstractDemandFactory {
	public abstract DemandModel build(String facilityId, 
			RandomParameters randomParameters,
			RandomDataGenerator random);
}
