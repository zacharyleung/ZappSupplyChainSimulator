package com.gly.scs.data;

import java.util.List;
import java.util.Collection;

import com.gly.scs.domain.Facility;
import com.gly.scs.domain.Shipment;

public abstract class AbstractShipmentRepository {
	
	/**
	 * Return the shipments that are to be received by the specified customer
	 * in the specified period.
	 * Mark the shipments as received.
	 * @param customer
	 * @param period
	 * @return
	 */
	public Collection<Shipment> getShipmentsToBeReceived(
			Facility customer, int period) {
		List<Shipment> out = subGetShipmentsToBeReceived(customer, period);
		for (Shipment shipment : out) {
			shipment.setAsReceived();
		}
		return out;
	}
	
	public abstract List<Shipment> subGetShipmentsToBeReceived(
			Facility customer, int period);

	public abstract Collection<Shipment> getPipeline(Facility facility);
	
	/**
	 * Add a shipment to the repository.
	 * @param shipment
	 */
	public abstract void addShipment(Shipment shipment);
	
	/**
	 * Return the shipments sent to the specified customer.
	 * @param customer
	 * @return
	 */
	public abstract Collection<Shipment> getShipmentHistory(Facility customer);

}
