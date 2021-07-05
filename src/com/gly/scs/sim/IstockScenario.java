package com.gly.scs.sim;

import com.gly.scs.replen.OneTierReplenishmentPolicy;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.sim.IstockSimulator.Builder;
import com.gly.util.StringUtils;

public class IstockScenario extends AbstractScenario {

	private OneTierReplenishmentPolicy retailReplenishmentPolicy;
	private OneTierReplenishmentPolicy regionalReplenishmentPolicy;
	private OrderUpToLevel regionalInitialInventoryLevel;
	private OrderUpToLevel retailInitialInventoryLevel;
	
	private IstockScenario(Builder builder) {
		super(builder.name);
		this.regionalReplenishmentPolicy = builder.regionalReplenishmentPolicy;
		this.retailReplenishmentPolicy = builder.retailReplenishmentPolicy;
		this.regionalInitialInventoryLevel = builder.regionalInitialInventoryLevel;
		this.retailInitialInventoryLevel = builder.retailInitialInventoryLevel;
	}
	
	public static class Builder {
		private String name;
		private OneTierReplenishmentPolicy retailReplenishmentPolicy;
		private OneTierReplenishmentPolicy regionalReplenishmentPolicy;
		private OrderUpToLevel retailInitialInventoryLevel = null;
		private OrderUpToLevel regionalInitialInventoryLevel = null;
		
		public Builder withName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder withRegionalReplenishmentPolicy(
				OneTierReplenishmentPolicy regionalReplenishmentPolicy) {
			this.regionalReplenishmentPolicy = regionalReplenishmentPolicy;
			return this;
		}
		
		public Builder withRetailReplenishmentPolicy(
				OneTierReplenishmentPolicy retailReplenishmentPolicy) {
			this.retailReplenishmentPolicy = retailReplenishmentPolicy;
			return this;
		}
		
		public Builder withRegionalInitialInventoryLevel(
				OrderUpToLevel regionalInitialInventoryLevel) {
			this.regionalInitialInventoryLevel = regionalInitialInventoryLevel;
			return this;
		}

		public Builder withRetailInitialInventoryLevel(
				OrderUpToLevel retailInitialInventoryLevel) {
			this.retailInitialInventoryLevel = retailInitialInventoryLevel;
			return this;
		}

		public IstockScenario build() {
			return new IstockScenario(this);
		}
	}

	@Override
	public AbstractSimulator run(SupplyChain supplyChain,
			SimulationParameters simulationParameters) throws Exception {
		return new IstockSimulator.Builder()
		.withSupplyChain(supplyChain)
		.withSimulationParameters(simulationParameters)
		.withRegionalReplenishmentPolicy(regionalReplenishmentPolicy)
		.withRetailReplenishmentPolicy(retailReplenishmentPolicy)
		.withRegionalInitialInventoryLevel(regionalInitialInventoryLevel)
		.withRetailInitialInventoryLevel(retailInitialInventoryLevel)
		.build();
	}
	
	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IstockScenario{" + NL);
		
		sb.append(StringUtils.prependToEachLine(
				"retail initial inventory level = " + retailInitialInventoryLevel.prettyToString(),
				PAD));
		sb.append(NL);
		
		sb.append(StringUtils.prependToEachLine(
				"regional initial inventory level = " + regionalInitialInventoryLevel.prettyToString(),
				PAD));
		sb.append(NL);
		
		sb.append(StringUtils.prependToEachLine(
				"retail replenishment policy = " + retailReplenishmentPolicy.prettyToString(),
				PAD));
		sb.append(NL);
		
		sb.append(StringUtils.prependToEachLine(
				"regional replenishment policy = " + regionalReplenishmentPolicy.prettyToString(),
				PAD));
		sb.append(NL);

		sb.append("}");
		return sb.toString();
	}
	
}
