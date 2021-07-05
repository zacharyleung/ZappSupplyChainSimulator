package com.gly.scs.demand;

import java.util.Collection;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;

public class ConstantSingleFacilityDemandFactory extends
		SingleFacilityDemandFactory {

	private Collection<String> retailFacilityIds;
	private final int value;
	
	public ConstantSingleFacilityDemandFactory(
			Collection<String> retailFacilityIds, int value) {
		this.value = value;
	}
	
	@Override
	public SingleFacilityDemand build(String facilityId,
			RandomParameters randomParameters, RandomDataGenerator random) {
		return new ConstantSingleFacilityDemand(value);
	}

	@Override
	public double getMeanDemandPerPeriod() {
		return retailFacilityIds.size() * value;
	}

}
