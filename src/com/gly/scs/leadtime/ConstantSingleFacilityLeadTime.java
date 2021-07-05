package com.gly.scs.leadtime;

import com.gly.random.ConstantIntegerRandomVariable;
import com.gly.random.IntegerRandomVariable;

public class ConstantSingleFacilityLeadTime extends SingleFacilityLeadTime {

	private final int value;
	
	ConstantSingleFacilityLeadTime(int value) {
		this.value = value;
	}
	
	
	@Override
	int getLeadTime(int t) {
		return value;
	}

	@Override
	IntegerRandomVariable getLeadTimeRandomVariable(int t) {
		return new ConstantIntegerRandomVariable(value);
	}


	@Override
	double getAccessibility(int t) {
		return 1;
	}

}
