package com.gly.scs.data;

import java.util.Collection;

import com.gly.scs.domain.Facility;
import com.gly.scs.domain.Shipment;

public class ImmutableShipmentRepository {
	
	AbstractShipmentRepository shipmentRepo;
	
	public ImmutableShipmentRepository(AbstractShipmentRepository shipmentRepo) {
		this.shipmentRepo = shipmentRepo;
	}
	
	public Collection<Shipment> getPipeline(Facility facility) {
		return shipmentRepo.getPipeline(facility);
	}

}
