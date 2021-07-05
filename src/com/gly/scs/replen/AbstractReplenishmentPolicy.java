package com.gly.scs.replen;

import java.util.*;

import com.gly.scs.data.*;
import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.sched.*;
import com.gly.util.PrettyToString;

/**
 * A replenishment policy has a field report delay and computes the
 * shipment decisions based on the reports.
 * @author zacleung
 *
 * @param <T>
 * @param <U>
 */
public abstract class AbstractReplenishmentPolicy 
<T extends Facility, U extends LeadTime> 
implements ReportProcessor, PrettyToString {

	protected AbstractReplenishmentFunction<T,U> function;

	/**
	 * Number of periods of delay between a report being created by a
	 * downstream facility and received by the upstream facility,
	 * e.g. from retail facility to regional facility.  
	 */
	protected final int oneTierReportDelay;

	private final boolean shouldAlwaysSubmitReports;
	
	/** Reset the state of a stateful replenishment policy. */
	public void resetState() {
		function.resetState();
	}
	
	protected AbstractReplenishmentPolicy(int oneTierReportDelay,
			AbstractReplenishmentFunction<T,U> function,
			boolean shouldAlwaysSubmitReports) {
		this.oneTierReportDelay = oneTierReportDelay;
		this.function = function;
		this.shouldAlwaysSubmitReports = shouldAlwaysSubmitReports;
	}

	public abstract int getReportDelay();

	/**
	 * Based on the reports that the national facility has received
	 * in period t, and the current state of the national facility,
	 * the demand model, lead time model and shipment schedule,
	 * compute shipment decisions for the current period and possibly
	 * future periods as well.
	 * @param currentPeriod
	 * @param inventoryLevel
	 * @param reports
	 * @param shipmentRepository
	 * @param demand
	 * @param leadTime
	 * @return
	 */
	public Collection<ShipmentDecision> getShipmentDecisions(
			int currentPeriod,
			T supplierFacility,
			Collection<Report> reports,
			ImmutableShipmentRepository shipmentRepository,
			DemandModel demand,
			AbstractShipmentSchedule shipmentSchedule,
			U leadTime)
					throws Exception {
		return function.getShipmentDecisions(currentPeriod, supplierFacility,
				reports, shipmentRepository, demand,
				shipmentSchedule, leadTime);
	}

	/**
	 * Return the number of periods of history to include in a report.
	 * @return
	 */
	public int getReportHistoryPeriods() {
		return function.getReportHistoryPeriods();
	}
	
	/**
	 * Return the number of periods of demand forecast to include in
	 * a report.
	 * @return
	 */
	public int getReportForecastPeriods() {
		return function.getReportForecastPeriods();
	}
	
	public int getForecastLevel() {
		return function.getForecastLevel();
	}
	
	/**
	 * Return true if retail facilities should submit a report in
	 * every period.
	 * @return
	 */
	public boolean shouldAlwaysSubmitReports() {
		return shouldAlwaysSubmitReports;
	}
	
}
