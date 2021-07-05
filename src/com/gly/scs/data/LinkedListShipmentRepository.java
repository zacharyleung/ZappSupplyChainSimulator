package com.gly.scs.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.gly.scs.domain.Facility;
import com.gly.scs.domain.Shipment;

/**
 * This class is an implementation of AbstractShipmentRepository using
 * a linked list.
 * A single linked list includes all shipments, i.e. shipments in
 * transit and shipments already received. 
 *  
 * @author zacharyleung
 *
 */
public class LinkedListShipmentRepository extends AbstractShipmentRepository {
	LinkedList<Shipment> shipments = new LinkedList<>();
	
	@Override
	public void addShipment(Shipment shipment) {
		shipments.addLast(shipment);
	}

	@Override
	public List<Shipment> getPipeline(Facility facility) {
		LinkedList<Shipment> out = new LinkedList<>();
		for (Shipment shipment : shipments) {
			// if the shipment customer is the specified facility,
			// and the shipment has not yet been received,
			// then add the shipment to the list of output shipments
			boolean isValid = shipment.to == facility &
					!shipment.hasBeenReceived();
			if (isValid) {
				out.add(shipment);
			}
		}
		return out;
	}


	@Override
	public List<Shipment> subGetShipmentsToBeReceived(Facility customer, int period) {
		LinkedList<Shipment> out = new LinkedList<>();
		for (Shipment shipment : shipments) {
			// if the shipment received period is the current period,
			// and the shipment customer is in the list of customers,
			// then add the shipment to the list of output shipments
			boolean isValid = shipment.periodReceived == period &
					shipment.to == customer &
					!shipment.hasBeenReceived();
			if (isValid) {
				out.add(shipment);
			}
		}
		return out;
	}

	@Override
	public Collection<Shipment> getShipmentHistory(Facility customer) {
		LinkedList<Shipment> out = new LinkedList<>();
		for (Shipment shipment : shipments) {
			if (shipment.to == customer) {
				out.add(shipment);
			}
		}
		return out;
	}
	
}
