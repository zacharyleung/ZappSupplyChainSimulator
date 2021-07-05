package com.gly.scs.opt;

public enum HoldingCostType {
	CONSTANT("c"),
	FACILITY_ACCESSIBILITY("fa");

	private final String name;

	HoldingCostType(String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
}
