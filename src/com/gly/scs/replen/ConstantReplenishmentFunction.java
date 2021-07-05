package com.gly.scs.replen;

import java.util.Collection;
import java.util.LinkedList;

import com.gly.scs.data.ImmutableShipmentRepository;
import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Facility;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;

public class ConstantReplenishmentFunction <T extends Facility, U extends LeadTime>
extends AbstractReplenishmentFunction<T, U> {

	public final int quantity;
	
	public ConstantReplenishmentFunction(int quantity) {
		this.quantity = quantity;
	}
	
	@Override
	public Collection<ShipmentDecision> getShipmentDecisions(int currentPeriod,
			T supplierFacility, Collection<Report> reports,
			ImmutableShipmentRepository shipmentRepository, DemandModel demand,
			AbstractShipmentSchedule shipmentSchedule, U leadTime)
					throws Exception {
		LinkedList<ShipmentDecision> shipmentDecisions = new LinkedList<>();
		int inv = supplierFacility.getCurrentInventoryLevel();

		for (Report report : reports) {
			// quantity to ship to this facility
			int q = Math.min(quantity, inv);
			inv -= q;

			// only create a shipment if the shipment quantity > 0
			if (q > 0) {
				ShipmentDecision shipmentDecision = new ShipmentDecision.Builder()
				.withFacility(report.getFacility())
				.withPeriod(currentPeriod)
				.withQuantity(q)
				.build();

				shipmentDecisions.add(shipmentDecision);
			}
		}

		// print output
//		System.out.println("ConstantReplenishmentPolicy.getShipmentList()");
//		for (Shipment shipment : shipments) {
//			System.out.println(shipment == null);
//			System.out.println(shipment);
//		}

		return shipmentDecisions;
	}

	public int getReportHistoryPeriods() { return 0; }

	@Override
	public int getReportForecastPeriods() {	return 0; }

	@Override
	public int getForecastLevel() {	return 0; }

	@Override
	public void resetState() {
		// nothing to do
	}

	@Override
	public String prettyToString() {
		return "ConstantReplenishmentFunction";
	}

}
