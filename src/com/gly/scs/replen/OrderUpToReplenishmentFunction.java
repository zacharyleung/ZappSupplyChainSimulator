package com.gly.scs.replen;

import java.util.Collection;
import java.util.LinkedList;

import com.gly.scs.data.ImmutableShipmentRepository;
import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.domain.Facility.Increment;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.replen.Allocation.Order;
import com.gly.scs.sched.AbstractShipmentSchedule;
import com.gly.util.StringUtils;

public class OrderUpToReplenishmentFunction <T extends Facility, U extends LeadTime>
extends AbstractReplenishmentFunction<T, U> {

	private OrderUpToLevel orderUpToLevel;
	private Minuend minuend;
	private Allocation allocation;

	private final boolean shouldPrint = false;
	private final boolean inDebugMode = false;
	
	public OrderUpToReplenishmentFunction(
			OrderUpToLevel orderUpToLevel, Minuend minuend,
			Allocation allocation) {
		this.orderUpToLevel = orderUpToLevel;
		this.minuend = minuend;
		this.allocation = allocation;
	}

	@Override
	public Collection<ShipmentDecision> getShipmentDecisions(int currentPeriod,
			T supplierFacility, Collection<Report> reports,
			ImmutableShipmentRepository shipmentRepository, DemandModel demand,
			AbstractShipmentSchedule shipmentSchedule, U leadTime)
					throws Exception {

		LinkedList<ShipmentDecision> shipmentDecisions = new LinkedList<>();
		int inv = supplierFacility.getCurrentInventoryLevel();

		LinkedList<Order> orders = new LinkedList<>();
		for (Report report : reports) {
			// order-up-to level
			int out = orderUpToLevel.getOrderUpToLevel(
					currentPeriod, report, 
					shipmentSchedule, leadTime, demand);

			// order quantity base level
			int b = minuend.getMinuend(report);

			if (inDebugMode) {
				int i = new Minuend.InventoryLevel().getMinuend(report);
				System.out.printf("InventoryLevel: minuend = %d, quantity = %d\n",
						i, out - i); 

				i = new Minuend.InventoryPosition().getMinuend(report);
				System.out.printf("InventoryPosition: minuend = %d, quantity = %d\n", 
						i, out - i);
				System.out.println();
			}

			// shipment quantity which must be at least 0
			int q = Math.max(0, out - b);

			// increment the supplier facility's record of demand 
			supplierFacility.incrementDemand(new Increment.Builder()
					.withPeriod(currentPeriod)
					.withQuantity(q)
					.build());

			//System.out.println("OrderUpToReplenishmentFunction.getShipmentDecisions()");
			//System.out.println(report);
			//System.out.println("order-up-to level = " + out);
			//System.out.println("shipment quantity = " + q);

			if (q > 0) {
				orders.add(new Order(report.getFacility(), q));
			}
		}

		int sumOfShipments = 0;
		Collection<Order> alloc = allocation.getAllocation(orders, inv);
		for (Order order : alloc) {
			// only create a shipment if the shipment quantity > 0
			if (order.quantity > 0) {
				ShipmentDecision shipmentDecision = new ShipmentDecision.Builder()
						.withFacility(order.customer)
						.withPeriod(currentPeriod)
						.withQuantity(order.quantity)
						.build();

				shipmentDecisions.add(shipmentDecision);
				sumOfShipments += order.quantity;
			}
			//System.out.println(order);
		}

		// debug code
		if (shouldPrint) {
			System.out.println("OrderUpToReplenishmentFunction.getShipmentDecisions()");
			System.out.println(supplierFacility);
			System.out.println("Sum of shipments = " + sumOfShipments);
		}

		// print output
		//		System.out.println("ConstantReplenishmentPolicy.getShipmentList()");
		//		for (Shipment shipment : shipments) {
		//			System.out.println(shipment);
		//		}

		return shipmentDecisions;
	}

	@Override
	public int getReportHistoryPeriods() {
		return orderUpToLevel.getReportHistoryPeriods();
	}

	@Override
	public int getReportForecastPeriods() {
		return orderUpToLevel.getReportForecastPeriods();
	}

	@Override
	public void resetState() {
		// nothing to do
	}

	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OrderUpToReplenishmentPolicy{" + NL);
		sb.append(StringUtils.prependToEachLine(
				"order up to level = " + orderUpToLevel.prettyToString(),
				PAD));
		sb.append(NL);
		sb.append("}");
		return sb.toString();
	}

}
