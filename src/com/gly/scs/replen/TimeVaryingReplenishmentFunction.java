package com.gly.scs.replen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.gly.scs.data.ImmutableShipmentRepository;
import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Facility;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;

/**
 * This class assumes that all of the replenishment functions
 * have the same number of history and forecast periods.
 * @author zacharyleung
 *
 * @param <T>
 * @param <U>
 */

public class TimeVaryingReplenishmentFunction <T extends Facility, U extends LeadTime>
extends AbstractReplenishmentFunction<T, U> {
	private LinkedList<TimeVaryingPair<T, U>> pairList;
	
	public TimeVaryingReplenishmentFunction(LinkedList<TimeVaryingPair<T, U>> pairList) {
		this.pairList = new LinkedList<TimeVaryingPair<T, U>>(pairList);
	}	
	
	public AbstractReplenishmentFunction<T, U> getReplenishmentFunction(int t)
	throws NoSuchElementException{
		for (TimeVaryingPair<T, U> pair : pairList) {
			if (t < pair.nextPolicyTime) {
				return pair.replenishmentFunction;
			}
		}
		throw new NoSuchElementException();
	}
	
	@Override
	public int getReportHistoryPeriods() {
		return pairList.peek().replenishmentFunction.getReportHistoryPeriods();
	}

	@Override
	public int getReportForecastPeriods() {
		return pairList.peek().replenishmentFunction.getReportForecastPeriods();
	}

	@Override
	public int getForecastLevel() {
		return pairList.peek().replenishmentFunction.getForecastLevel();
	}

	@Override
	public String prettyToString() {
		String out = "";
		for (TimeVaryingPair<T, U> pair : pairList) {
			out += String.format("time = %d, replenishment function = %s",
					pair.nextPolicyTime, pair.replenishmentFunction);
		}
		return out;
	}

	@Override
	public Collection<ShipmentDecision> getShipmentDecisions(int currentPeriod,
			T supplierFacility, Collection<Report> reports,
			ImmutableShipmentRepository shipmentRepository, DemandModel demand,
			AbstractShipmentSchedule shipmentSchedule, U leadTime)
			throws Exception {
		return getReplenishmentFunction(currentPeriod)
				.getShipmentDecisions(currentPeriod, supplierFacility,
						reports, shipmentRepository, demand,
						shipmentSchedule, leadTime);
	}

	@Override
	public void resetState() {
		for (TimeVaryingPair<T, U> pair : pairList) {
			pair.replenishmentFunction.resetState();
		}
	}
	
}
