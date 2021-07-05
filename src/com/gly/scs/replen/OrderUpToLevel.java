package com.gly.scs.replen;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;
import com.gly.util.PrettyToString;

public abstract class OrderUpToLevel implements ReportProcessor, PrettyToString {
	public static enum Type {
		/**
		 * How much was consumed at a retail facility, or issued at a
		 * regional facility.
		 */
		CONSUMPTION,
		/**
		 * How much was demanded at a retail facility, or ordered at a
		 * regional facility.
		 */
		DEMAND
	};
	
	public abstract int getOrderUpToLevel(
			int currentTimestep, Report report,
			AbstractShipmentSchedule shipmentSchedule,
			LeadTime leadTime, DemandModel demand);
}
