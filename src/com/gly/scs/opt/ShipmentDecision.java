package com.gly.scs.opt;

public class ShipmentDecision {
	public final int period;
	public final int quantity;
	public final int retailId;
	
	ShipmentDecision(Builder builder) {
		this.period = builder.period;
		this.quantity = builder.quantity;
		this.retailId = builder.retailId;
	}
	
	public static class Builder {
		private int period;
		private int quantity;
		private int retailId;
		
		public Builder withPeriod(int period) {
			this.period = period;
			return this;
		}
		
		public Builder withQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}
		
		public Builder withRetailId(int retailId) {
			this.retailId = retailId;
			return this;
		}
		
		public ShipmentDecision build() {
			return new ShipmentDecision(this);
		}
	}
}
