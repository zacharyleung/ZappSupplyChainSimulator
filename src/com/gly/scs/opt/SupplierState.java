package com.gly.scs.opt;

import java.util.Collection;

public class SupplierState extends FacilityState {
	public SupplierState(Builder builder) {
		super(new FacilityState.Builder()
		.withIncomingShipments(builder.incomingShipments)
		.withInventoryLevel(builder.inventoryLevel)
		.withPeriod(builder.period));
	}
	
	public static class Builder {
		private int period;
		private int inventoryLevel;
		private Collection<IncomingShipment> incomingShipments;
		
		public Builder withPeriod(int period) {
			this.period = period;
			return this;
		}
		
		public Builder withInventoryLevel(int inventoryLevel) {
			this.inventoryLevel = inventoryLevel;
			return this;
		}
		
		public Builder withIncomingShipments(Collection<IncomingShipment> incomingShipments) {
			this.incomingShipments = incomingShipments;
			return this;
		}
		
		public SupplierState build() {
			return new SupplierState(this);
		}
	}

}
