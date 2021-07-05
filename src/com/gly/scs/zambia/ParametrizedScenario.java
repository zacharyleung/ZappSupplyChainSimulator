package com.gly.scs.zambia;

import com.gly.scs.sim.AbstractScenario;
import com.gly.util.PrettyToString;

public abstract class ParametrizedScenario implements PrettyToString {

	private String name;
	
	protected ParametrizedScenario(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract AbstractScenario getScenario(double parameter);

}
