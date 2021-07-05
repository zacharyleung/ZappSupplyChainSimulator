package com.gly.scs.leadtime;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;

public abstract class SingleFacilityLeadTimeFactory {
	abstract SingleFacilityLeadTime build(String facilityId, 
			RandomParameters randomParameters,
			RandomDataGenerator random);
}
