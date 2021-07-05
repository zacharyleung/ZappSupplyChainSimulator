package com.gly.scs.replen;

import java.util.*;

import com.gly.scs.data.AbstractShipmentRepository;
import com.gly.scs.domain.NationalFacility;
import com.gly.scs.domain.Shipment;
import com.gly.scs.sim.*;
import com.gly.util.MathUtils;
import com.gly.util.NegativeArray;

/**
 * Let t denote the current period.  The national facility replenishment
 * policy ensures that a shipment of <code>quantity</code> units is
 * scheduled to arrive at the national facility every period
 * u = offset mod cycle for u = t, t + 1, ..., t + horizon - 1.
 * @author zacharyleung
 *
 */
public class NationalReplenishmentSchedule {
	private int cycle;
	private int offset;
	private int quantity;
	private int horizon;
	
	private NationalReplenishmentSchedule(Builder builder) {
		this.cycle = builder.cycle;
		this.offset = builder.offset;
		this.quantity = builder.quantity;
		this.horizon = builder.horizon;
	}
	
	/**
	 * Compute the shipments which are scheduled to arrive in periods
	 * [t, t + horizon).  Add new shipments as necessary so that a shipment
	 * of <tt>quantity</tt> units is scheduled to arrive at the national
	 * facility every period u = offset mod cycle for u in [t, t + horizon).
	 * @param t
	 * @param nationalFacility
	 * @param shipmentRepository
	 */
	public void replenish(int t, NationalFacility nationalFacility, 
			AbstractShipmentRepository shipmentRepository) {
		// store the existing shipments into an array
		NegativeArray<Integer> array = new NegativeArray<>(t, t + horizon,
				new Integer(0));
		Collection<Shipment> shipments =
				shipmentRepository.getPipeline(nationalFacility);
		
		//System.out.println("NationalReplenishmentPolicy.replenish()");
		//System.out.println("period = " + t);
		//for (Shipment shipment : shipments) { System.out.println(shipment); }
		
		for (Shipment shipment : shipments) {
			int periodReceived = shipment.periodReceived;
			int q = shipment.quantity;
			array.set(periodReceived, q);	
		}
		
		// check which shipments need to be defined
		for (int u = t; u < t + horizon; ++u) {
			// if period i is a period when a shipment should arrive
			if (getQuantity(u) != 0) {
				// if there is no scheduled shipment to arrive at period i
				if (array.get(u) != quantity) {
					// then schedule a new shipment to arrive at period i
					Shipment shipment = new Shipment.Builder()
					.withPeriodReceived(u)
					.withPeriodSent(u)
					.withQuantity(quantity)
					.withTo(nationalFacility)
					.build();
					shipmentRepository.addShipment(shipment);
				}
			}
		}
	}
	
	public int getQuantity(int t) {
		if (MathUtils.positiveModulo(t, cycle) == offset) {
			return quantity;
		} else {
			return 0;
		}
	}
	
	public static class Builder {
		private int cycle;
		private int offset;
		private int quantity;
		private int horizon;

		public Builder withCycle(int cycle) {
			this.cycle = cycle;
			return this;
		}
		
		public Builder withOffset(int offset) {
			this.offset = offset;
			return this;
		}
		
		public Builder withQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}
		
		public Builder withHorizon(int horizon) {
			this.horizon = horizon;
			return this;
		}
		
		public NationalReplenishmentSchedule build() {
			return new NationalReplenishmentSchedule(this);
		}
	}
}
