package com.gly.scs.leadtime;

import com.gly.scs.domain.Topology;
import com.gly.scs.sim.RandomParameters;

public class XdockLeadTimeFactory {

	private final SingleFacilityLeadTimeFactory primarySfltFactory;
	private final SingleFacilityLeadTimeFactory secondarySfltFactory;
	private final Topology topology;

	private XdockLeadTimeFactory(Builder builder) {
		this.primarySfltFactory = builder.primarySfltFactory;
		this.secondarySfltFactory = builder.secondarySfltFactory;
		this.topology = builder.topology;
	}

	public static class Builder {
		private SingleFacilityLeadTimeFactory primarySfltFactory;
		private SingleFacilityLeadTimeFactory secondarySfltFactory;
		private Topology topology;

		public Builder withPrimarySfltFactory(
				SingleFacilityLeadTimeFactory primarySfltFactory) {
			this.primarySfltFactory = primarySfltFactory;
			return this;
		}

		public Builder withSecondarySfltFactory(
				SingleFacilityLeadTimeFactory secondarySfltFactory) {
			this.secondarySfltFactory = secondarySfltFactory;
			return this;
		}

		public Builder withTopology(Topology topology) {
			this.topology = topology;
			return this;
		}

		public XdockLeadTimeFactory build() {
			return new XdockLeadTimeFactory(this);
		}

	}

	public XdockLeadTime build(RandomParameters randomParameters) {
		return new XdockLeadTime.Builder()
		.withPrimarySfltFactory(primarySfltFactory)
		.withSecondarySfltFactory(secondarySfltFactory)
		.withRandomParameters(randomParameters)
		.withTopology(topology)
		.build();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("XdockLeadTimeFactory {%n"));
		sb.append(String.format("primarySfltFactory = %s%n", primarySfltFactory));
		sb.append(String.format("secondarySfltFactory = %s%n", secondarySfltFactory));
		sb.append(String.format("topology = %s%n", topology));
		sb.append("}");
		return sb.toString();
	}
	
}
