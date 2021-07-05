package com.gly.scs.opt;

public enum UnmetDemandLowerBoundType {
	ACTUAL("actual"),
	MEAN("mean"),
	SINGLE_PERIOD("single"),
	MULTI_PERIOD("multi");
	
	private final String name;

	UnmetDemandLowerBoundType(String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
}
