package com.gly.scs.opt;

import java.util.*;

public class ShipmentOpportunityRepository {

	private List<ShipmentOpportunity> shipmentOpportunities;
	
	public ShipmentOpportunityRepository(
			Collection<ShipmentOpportunity> shipmentOpportunities) {
		this.shipmentOpportunities = new ArrayList<>(shipmentOpportunities);
		Collections.<ShipmentOpportunity>sort(this.shipmentOpportunities);
	}
	
	public Collection<ShipmentOpportunity> getShipmentOpportunities() {
		return shipmentOpportunities;
	}

	public boolean hasShipmentOpportunity(int period) {
		for (ShipmentOpportunity so : this.shipmentOpportunities) {
			if (so.getPeriod() == period) {
				return true;
			}
		}
		return false;
	}
}
