package com.gly.scs.replen;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;

public class OrderUpToLevelConstant extends OrderUpToLevel {

	private final int value;
	
	public OrderUpToLevelConstant(int i) {
		this.value = i;
	}
	
	@Override
	public int getReportHistoryPeriods() {
		return 0;
	}

	@Override
	public int getReportForecastPeriods() {
		return 0;
	}

	@Override
	public String prettyToString() {
		return String.format("OrderUpToLevelConstant{%d}", value);
	}

	@Override
	public int getOrderUpToLevel(int currentTimestep, Report report, 
			AbstractShipmentSchedule shipmentSchedule,
			LeadTime leadTime, DemandModel demand) {
		return value;
	}

}
