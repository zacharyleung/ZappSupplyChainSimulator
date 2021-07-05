package com.gly.scs.opt;

import com.gly.random.IntegerRandomVariable;
import com.gly.scs.opt.IncomingShipment.Builder;

public class ShipmentOpportunity implements Comparable<ShipmentOpportunity> {
	private final int period;
	private final int realizedLeadTime;
	private IntegerRandomVariable leadTimeRandomVariable;
	
	public ShipmentOpportunity(Builder builder) {
		this.period = builder.period;
		this.realizedLeadTime = builder.realizedLeadTime;
		this.leadTimeRandomVariable = builder.leadTimeRandomVariable;
	}
	
	public static class Builder {
		private int period;
		private int realizedLeadTime;
		private IntegerRandomVariable leadTimeRandomVariable;
			
		public Builder withPeriod(int period) {
			this.period = period;
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
		
		public ShipmentOpportunity build() {
			return new ShipmentOpportunity(this);
		}
	}
	
	public int getPeriod() { return period; }
	
	public int getRealizedLeadTime() { return realizedLeadTime; }

	public IntegerRandomVariable getLeadTimeRandomVariable() {
		return leadTimeRandomVariable;
	}
	
	@Override
	public int compareTo(ShipmentOpportunity other) {
		return period - other.period;
	}
	
	@Override
	public String toString() {
		return String.format("ShipmentOpportunity{period = %d,ltrv = %s}",
				period, leadTimeRandomVariable.toString());
	}
}
