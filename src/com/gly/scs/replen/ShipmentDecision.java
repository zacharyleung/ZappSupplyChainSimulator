package com.gly.scs.replen;

import com.gly.scs.domain.Facility;


public class ShipmentDecision {
	public final int period;
	public final int quantity;
	public final Facility facility;
	
	private ShipmentDecision(Builder builder) {
		this.period = builder.period;
		this.quantity = builder.quantity;
		this.facility = builder.facility;
	}
	
	@Override
	public String toString() {
		return toString(2);
	}
	
	public String toString(int indent) {
		String nl = System.getProperty("line.separator");
		String pad = "";
		for (int i = 0; i < indent; ++i) {
			pad += " ";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("ShipmentDecision{" + nl);
		sb.append(pad + "facility = " + facility.getId() + nl);
		sb.append(pad + "quantity = " + quantity + nl);
		sb.append(pad + "period = " + period + nl);
		sb.append("}");
		return sb.toString();		
	}
	
	public static class Builder {
		private int period;
		private int quantity;
		private Facility facility;
		
		public Builder withPeriod(int period) {
			this.period = period;
			return this;
		}
		
		public Builder withQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}
		
		public Builder withFacility(Facility facility) {
			this.facility = facility;
			return this;
		}
		
		public ShipmentDecision build() {
			return new ShipmentDecision(this);
		}
	}
}
