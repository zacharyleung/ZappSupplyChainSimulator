package com.gly.scs.opt;

import com.gly.random.IntegerRandomVariable;

public class IncomingShipment {
	private final int periodSent;
	private final int periodArrival;
	private final int quantity;
	private final int realizedLeadTime;
	private IntegerRandomVariable leadTimeRandomVariable;
	
	public IncomingShipment(Builder builder) {
		this.periodArrival = builder.periodArrival;
		this.periodSent = builder.periodSent;
		this.quantity = builder.quantity;
		this.realizedLeadTime = builder.realizedLeadTime;
		this.leadTimeRandomVariable = builder.leadTimeRandomVariable;
	}

	public static class Builder {
		private int periodSent;
		private int periodArrival;
		private int quantity;
		private int realizedLeadTime;
		private IntegerRandomVariable leadTimeRandomVariable;
		
		public Builder withPeriodSent(int periodSent) {
			this.periodSent = periodSent;
			return this;
		}
		
		public Builder withPeriodArrival(int periodArrival) {
			this.periodArrival = periodArrival;
			return this;
		}
		
		public Builder withQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}
		
		public Builder withRealizedLeadTime(int realizedLeadTime) {
			this.realizedLeadTime = realizedLeadTime;
			return this;
		}
		
		public Builder withLeadTimeRandomVariable(
				IntegerRandomVariable leadTimeRandomVariable) {
			this.leadTimeRandomVariable = leadTimeRandomVariable;
			return this;
		}
		
		public IncomingShipment build() {
			return new IncomingShipment(this);
		}
	}
	
	public int getQuantity() { return quantity; }
	
	public int getPeriodArrival() { return periodArrival; }
	
	public int getPeriodSent() { return periodSent; }

	public int getRealizedLeadTime() { return realizedLeadTime; }

	public IntegerRandomVariable getLeadTimeRandomVariable() {
		return leadTimeRandomVariable;
	}
	
	@Override
	public String toString() {
		return String.format("IncomingShipment{periodArrival=%d}", periodArrival);
	}
}
