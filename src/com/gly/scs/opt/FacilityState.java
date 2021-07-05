package com.gly.scs.opt;

import java.util.Collection;

public abstract class FacilityState {
	private final int period;
	private final int inventoryLevel;
	/** Shipments incoming at the supplier. */
	private Collection<IncomingShipment> incomingShipments;

	protected FacilityState(Builder builder) {
		this.period = builder.period;
		this.inventoryLevel = builder.inventoryLevel;
		this.incomingShipments = builder.incomingShipments;
	}
	
	protected static class Builder {
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
	}
	
	public int getPeriod() { return period; }

	public int getInventoryLevel() { return inventoryLevel; }

	public Collection<IncomingShipment> getIncomingShipments() {
		return incomingShipments;
	}
}
