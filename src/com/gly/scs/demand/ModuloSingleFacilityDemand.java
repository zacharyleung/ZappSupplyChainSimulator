package com.gly.scs.demand;

import com.gly.random.AbstractRandomVariable;
import com.gly.random.ConstantRandomVariable;
import com.gly.util.MathUtils;

public class ModuloSingleFacilityDemand extends SingleFacilityDemand {

	private final int divisor;
	
	public ModuloSingleFacilityDemand(int divisor) {
		this.divisor = divisor;
	}
	
	@Override
	int getDemand(int t) {
		return (int) getMeanDemand(t);
	}

	@Override
	AbstractRandomVariable getDemandForecast(GetDemandForecastParameters params) {
		return new ConstantRandomVariable(getDemand(params.futurePeriod));
	}

	@Override
	double getMeanDemand(int t) {
		return MathUtils.positiveModulo(t, divisor);
	}

}
