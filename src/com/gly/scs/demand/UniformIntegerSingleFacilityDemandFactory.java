package com.gly.scs.demand;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;

public class UniformIntegerSingleFacilityDemandFactory 
extends SingleFacilityDemandFactory {

	private final int max;

	public UniformIntegerSingleFacilityDemandFactory(int max) {
		this.max = max;
	}

	@Override
	public SingleFacilityDemand build(String facilityId, RandomParameters randomParameters,
			RandomDataGenerator random) {
		return new UniformIntegerSingleFacilityDemand.Builder()
				.withMax(max)
				.withRandomDataGenerator(random)
				.withStartPeriod(randomParameters.startPeriod)
				.withEndPeriod(randomParameters.endPeriod)
				.build();
	}

	@Override
	public double getMeanDemandPerPeriod() {
		return max / 2.0;
	}

}
