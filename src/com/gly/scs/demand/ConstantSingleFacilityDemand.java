package com.gly.scs.demand;

import com.gly.random.AbstractRandomVariable;
import com.gly.random.ConstantRandomVariable;

public class ConstantSingleFacilityDemand extends SingleFacilityDemand {

	private int value;
	
	public ConstantSingleFacilityDemand(int value) {
		this.value = value;
	}

	@Override
	int getDemand(int t) {
		return value;
	}

	@Override
	AbstractRandomVariable getDemandForecast(GetDemandForecastParameters params) {
		return new ConstantRandomVariable(value);
	}

	@Override
	double getMeanDemand(int t) {
		return value;
	}

}
