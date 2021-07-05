package com.gly.scs.domain;

import java.util.Collection;

public class ReportParameters {

	public final int currentPeriod;
	public final int historyPeriods;
	public final Collection<Shipment> shipments;
	
	private ReportParameters(Builder builder) {
		this.currentPeriod = builder.currentPeriod;
		this.historyPeriods = builder.historyPeriods;
		this.shipments = builder.shipments;
	}
	
	public static class Builder {
		private int currentPeriod;
		private int historyPeriods;
		private Collection<Shipment> shipments;
		
		public Builder withCurrentPeriod(int currentPeriod) {
			this.currentPeriod = currentPeriod;
			return this;
		}
		
		public Builder withHistoryPeriods(int historyPeriods) {
			this.historyPeriods = historyPeriods;
			return this;
		}
		
		public Builder withShipments(Collection<Shipment> shipments) {
			this.shipments = shipments;
			return this;
		}
		
		public ReportParameters build() {
			return new ReportParameters(this);
		}
	}
	
}
