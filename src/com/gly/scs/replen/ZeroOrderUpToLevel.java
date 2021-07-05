package com.gly.scs.replen;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;

public class ZeroOrderUpToLevel extends OrderUpToLevel {

	@Override
	public int getReportHistoryPeriods() {
		return 0;
	}

	@Override
	public int getReportForecastPeriods() {
		return 0;
	}

	@Override
	public int getForecastLevel() {
		return 0;
	}

	@Override
	public String prettyToString() {
		return "ZeroOrderUpToLevel";
	}

	@Override
	public int getOrderUpToLevel(int currentTimestep, Report report, 
			AbstractShipmentSchedule shipmentSchedule, LeadTime leadTime,
			DemandModel demand) {
		return 0;
	}

}
