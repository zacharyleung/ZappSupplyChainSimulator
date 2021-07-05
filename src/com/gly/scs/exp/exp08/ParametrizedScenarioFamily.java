package com.gly.scs.exp.exp08;

import java.util.Arrays;

import com.gly.scs.exp.param.ExperimentParameters;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.AbstractScenario;

public abstract class ParametrizedScenarioFamily {
	protected final String name;
	private double[] parameters;
	
	protected ParametrizedScenarioFamily(String name, double[] parameters) {
		this.name = name;
		this.parameters = Arrays.copyOf(parameters, parameters.length);
	}
	
	public String getName() {
		return name;
	}
	
	public double[] getParameters() {
		return Arrays.copyOf(parameters, parameters.length);
	}
	
	public abstract AbstractScenario getScenario(
			double parameter, ExperimentParameters expParameters);
}
