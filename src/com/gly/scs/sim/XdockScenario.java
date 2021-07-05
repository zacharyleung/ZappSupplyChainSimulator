package com.gly.scs.sim;

import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.replen.XdockReplenishmentPolicy;
import com.gly.scs.sim.IstockScenario.Builder;
import com.gly.util.StringUtils;

public class XdockScenario extends AbstractScenario {

	private XdockReplenishmentPolicy replenishmentPolicy;
	private OrderUpToLevel retailInitialInventoryLevel;

	public XdockScenario(Builder builder) {
		super(builder.name);
		this.replenishmentPolicy = builder.replenishmentPolicy;
		this.retailInitialInventoryLevel = builder.retailInitialInventoryLevel;
	}
	
	@Override
	public AbstractSimulator run(SupplyChain supplyChain,
			SimulationParameters simulationParameters) throws Exception {
		return new XdockSimulator.Builder()
		.withSupplyChain(supplyChain)
		.withSimulationParameters(simulationParameters)
		.withReplenishmentPolicy(replenishmentPolicy)
		.withRetailInitialInventoryLevel(retailInitialInventoryLevel)
		.build();
	}

	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "{" + NL);

		sb.append(StringUtils.prependToEachLine(
				"retail initial inventory level = " + retailInitialInventoryLevel.prettyToString(),
				PAD));
		sb.append(NL);
		
		sb.append(StringUtils.prependToEachLine(
				"replenishment policy = " + replenishmentPolicy.prettyToString(),
				PAD));
		sb.append(NL);

		sb.append("}");
		return sb.toString();
	}
	
	public static class Builder {
		private String name;
		private XdockReplenishmentPolicy replenishmentPolicy;
		private OrderUpToLevel retailInitialInventoryLevel;

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withReplenishmentPolicy(
				XdockReplenishmentPolicy replenishmentPolicy) {
			this.replenishmentPolicy = replenishmentPolicy;
			return this;
		}
		
		public Builder withRetailInitialInventoryLevel(
				OrderUpToLevel retailInitialInventoryLevel) {
			this.retailInitialInventoryLevel = retailInitialInventoryLevel;
			return this;
		}

		public XdockScenario build() {
			return new XdockScenario(this);
		}
	}
}
