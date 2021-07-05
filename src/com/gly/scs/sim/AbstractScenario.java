package com.gly.scs.sim;

import com.gly.util.PrettyToString;

/**
 * An abstract scenario consists of:
 * <ol>
 * <li>initial conditions (i.e. initial inventory levels)</li>
 * <li>a replenishment policy</li>
 * </ol>
 * 
 * @author zacleung
 *
 */
public abstract class AbstractScenario implements PrettyToString {
	
	private final String name;
	
	protected AbstractScenario(String name) {
		this.name = name;
	}
	
	public abstract AbstractSimulator run(SupplyChain supplyChain,
			SimulationParameters simulationParameters)
					throws Exception;
	
	public String getName() {
		return name;
	}
	
}
