package com.gly.scs.demand;

import java.util.Collection;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;


public class ModuloSingleFacilityDemandFactory extends
		SingleFacilityDemandFactory {

	private Collection<String> retailFacilityIds;
	private final int divisor;
	
	public ModuloSingleFacilityDemandFactory(
			Collection<String> retailFacilityIds, int divisor) {
		this.divisor = divisor;
	}
	
	@Override
	public SingleFacilityDemand build(String facilityId,
			RandomParameters randomParameters, RandomDataGenerator random) {
		return new ModuloSingleFacilityDemand(divisor);
	}

	@Override
	public double getMeanDemandPerPeriod() {
		return retailFacilityIds.size() * divisor / 2.0;
	}

}