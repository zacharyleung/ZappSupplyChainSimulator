package com.gly.scs.sim;

import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;

public class SupplyChain {
	public final RegionalShipmentSchedule regionalShipmentSchedule;
	public final AbstractShipmentSchedule retailShipmentSchedule;
	public final SingleFacilityLeadTimeFactory regionalLeadTimeFactory;
	public final SingleFacilityLeadTimeFactory retailLeadTimeFactory;
	public final SingleFacilityDemandFactory demandFactory;
	public final Topology topology;
	public final NationalReplenishmentSchedule nationalReplenishmentSchedule;
	
	public SupplyChain(Builder builder) {
		this.demandFactory = builder.demandFactory;
		this.topology = builder.topology;
		this.nationalReplenishmentSchedule = builder.nationalReplenishmentPolicy;
		this.regionalLeadTimeFactory = builder.regionalLeadTimeFactory;
		this.regionalShipmentSchedule = builder.regionalShipmentSchedule;
		this.retailLeadTimeFactory = builder.retailLeadTimeFactory;
		this.retailShipmentSchedule = builder.retailShipmentSchedule;
	}
	
	public static class Builder {
		private Topology topology;
		private NationalReplenishmentSchedule nationalReplenishmentPolicy;
		private SingleFacilityDemandFactory demandFactory;
		private SingleFacilityLeadTimeFactory retailLeadTimeFactory;
		private SingleFacilityLeadTimeFactory regionalLeadTimeFactory;
		private AbstractShipmentSchedule retailShipmentSchedule;
		private RegionalShipmentSchedule regionalShipmentSchedule;
	
		public Builder withTopology(Topology topology) {
			this.topology = topology;
			return this;
		}

		public Builder withNationalReplenishmentSchedule(
				NationalReplenishmentSchedule nationalReplenishmentPolicy) {
			this.nationalReplenishmentPolicy = nationalReplenishmentPolicy;
			return this;
		}

		public Builder withDemandFactory(SingleFacilityDemandFactory demandFactory) {
			this.demandFactory = demandFactory;
			return this;
		}

		public Builder withRetailLeadTimeFactory(
				SingleFacilityLeadTimeFactory retailLeadTimeFactory) {
			this.retailLeadTimeFactory = retailLeadTimeFactory;
			return this;
		}

		public Builder withRegionalLeadTimeFactory(
				SingleFacilityLeadTimeFactory regionalLeadTimeFactory) {
			this.regionalLeadTimeFactory = regionalLeadTimeFactory;
			return this;
		}

		public Builder withRetailShipmentSchedule(
				AbstractShipmentSchedule retailShipmentSchedule) {
			this.retailShipmentSchedule = retailShipmentSchedule;
			return this;
		}

		public Builder withRegionalShipmentSchedule(
				RegionalShipmentSchedule regionalShipmentSchedule) {
			this.regionalShipmentSchedule = regionalShipmentSchedule;
			return this;
		}
		
		public SupplyChain build() {
			return new SupplyChain(this);
		}
	}
}
