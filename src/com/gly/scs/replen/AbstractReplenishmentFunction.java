package com.gly.scs.replen;

import java.util.Collection;

import com.gly.scs.data.*;
import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.sched.*;
import com.gly.util.PrettyToString;

public abstract class AbstractReplenishmentFunction
<T extends Facility, U extends LeadTime>
implements ReportProcessor, PrettyToString {

	/**
	 * Based on the reports that the national facility has received
	 * in period t, and the current state of the national facility,
	 * the demand model, lead time model and shipment schedule,
	 * compute shipment decisions for the current period.
	 * @param currentPeriod
	 * @param inventoryLevel
	 * @param reports
	 * @param shipmentRepository
	 * @param demand
	 * @param leadTime
	 * @return
	 */
	public abstract Collection<ShipmentDecision> getShipmentDecisions(
			int currentPeriod,
			T supplierFacility,
			Collection<Report> reports,
			ImmutableShipmentRepository shipmentRepository,
			DemandModel demand,
			AbstractShipmentSchedule shipmentSchedule,
			U leadTime)
					throws Exception;
	
	/** Reset the state of a stateful replenishment policy. */
	public abstract void resetState();

}
