package com.gly.scs.domain;


public class Shipment {
	public final int periodSent;
	public final int periodReceived;
	public final int quantity;
	public final Facility to;
	private boolean isReceived = false;
	
	private Shipment(Builder builder) {
		this.periodReceived = builder.periodReceived;
		this.periodSent = builder.periodSent;
		this.quantity = builder.quantity;
		this.to = builder.to;
	}
	
	/**
	 * Set this shipment as received.
	 * @throws IllegalStateException If the shipment has already been received.
	 */
	public void setAsReceived() throws IllegalStateException {
		if (isReceived) {
			throw new IllegalStateException("Shipment has been received!");
		}
		isReceived = true;
	}
	
	public boolean hasBeenReceived() {
		return isReceived;
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
		sb.append("Shipment{" + nl);
		sb.append(pad + "to = " + to.getId() + nl);
		sb.append(pad + "quantity = " + quantity + nl);
		sb.append(pad + "period received = " + periodReceived + nl);
		sb.append("}");
		return sb.toString();		
	}
	
	public static class Builder {
		private int periodSent;
		private int periodReceived;
		private int quantity;
		private Facility to;
		
		public Builder withPeriodSent(int periodSent) {
			this.periodSent = periodSent;
			return this;
		}
		
		public Builder withPeriodReceived(int periodReceived) {
			this.periodReceived = periodReceived;
			return this;
		}
		
		public Builder withQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}
		
		public Builder withTo(Facility to) {
			this.to = to;
			return this;
		}
		
		public Shipment build() {
			return new Shipment(this);
		}
	}
}
