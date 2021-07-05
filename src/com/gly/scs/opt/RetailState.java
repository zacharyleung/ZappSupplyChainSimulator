package com.gly.scs.opt;

import java.util.Collection;

import com.gly.random.AbstractRandomVariable;
import com.gly.util.SparseArray;

public class RetailState extends FacilityState {
	private SparseArray<Double> accessibility;
	/** Future shipment opportunities to this retail facility. */
	private ShipmentOpportunityRepository shipmentOpportunities;
	private SparseArray<AbstractRandomVariable> demandForecast;
	private SparseArray<Integer> realizedDemand;
	
	public RetailState(Builder builder) {
		super(new FacilityState.Builder()
		.withIncomingShipments(builder.incomingShipments)
		.withInventoryLevel(builder.inventoryLevel)
		.withPeriod(builder.period));

		this.accessibility = builder.accessibility;
		this.shipmentOpportunities = new ShipmentOpportunityRepository(
				builder.shipmentOpportunities);
		this.demandForecast = builder.demandForecast;
		this.realizedDemand = builder.realizedDemand;
	}

	public static class Builder {
		private int period;
		private int inventoryLevel;
		private SparseArray<Double> accessibility;
		private Collection<IncomingShipment> incomingShipments;
		private Collection<ShipmentOpportunity> shipmentOpportunities;
		private SparseArray<AbstractRandomVariable> demandForecast;
		private SparseArray<Integer> realizedDemand;
		
		public Builder withPeriod(int period) {
			this.period = period;
			return this;
		}
		
		public Builder withInventoryLevel(int inventoryLevel) {
			this.inventoryLevel = inventoryLevel;
			return this;
		}
		
		public Builder withAccessibility(SparseArray<Double> accessibility) {
			this.accessibility = accessibility;
			return this;
		}
		
		public Builder withIncomingShipments(Collection<IncomingShipment> incomingShipments) {
			this.incomingShipments = incomingShipments;
			return this;
		}

		public Builder withShipmentOpportunities(Collection<ShipmentOpportunity> shipmentOpportunities) {
			this.shipmentOpportunities = shipmentOpportunities;
			return this;
		}

		public Builder withDemandForecast(SparseArray<AbstractRandomVariable> demandForecast) {
			this.demandForecast = demandForecast;
			return this;
		}
		
		public Builder withRealizedDemand(SparseArray<Integer> realizedDemand) {
			this.realizedDemand = realizedDemand;
			return this;
		}
		
		public RetailState build() {
			return new RetailState(this);
		}
	}

	public double getAccessibility(int period) {
		return accessibility.get(period);
	}
	
	public Collection<ShipmentOpportunity> getShipmentOpportunities() {
		return shipmentOpportunities.getShipmentOpportunities();
	}
	
	public boolean hasShipmentOpportunity(int period) {
		return shipmentOpportunities.hasShipmentOpportunity(period);
	}
	
	public AbstractRandomVariable getDemandForecast(int period) {
		return demandForecast.get(period);
	}
	
	public int getRealizedDemand(int period) {
		return realizedDemand.get(period);
	}
}
