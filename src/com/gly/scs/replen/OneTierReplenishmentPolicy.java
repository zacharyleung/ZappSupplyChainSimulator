package com.gly.scs.replen;

import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.util.StringUtils;

/**
 * This class implements an intermediate stocking replenishment policy.
 * 
 * This class implements an intermediate stocking replenishment policy,
 * i.e. a replenishment policy for deciding shipments from the national
 * facility to regional facilities, or for deciding shipments from a
 * regional facility to retail facilities.
 * 
 * @author zacharyleung
 *
 */
public class OneTierReplenishmentPolicy extends
		AbstractReplenishmentPolicy<Facility, OneTierLeadTime> {
	
	public OneTierReplenishmentPolicy(int oneTierReportDelay,
			AbstractReplenishmentFunction
			<Facility, OneTierLeadTime> function) {
		super(oneTierReportDelay, function, false);
	}

	public OneTierReplenishmentPolicy(int oneTierReportDelay,
			AbstractReplenishmentFunction
			<Facility, OneTierLeadTime> function,
			boolean shouldAlwaysSubmitReports) {
		super(oneTierReportDelay, function, shouldAlwaysSubmitReports);
	}

	/**
	 * Number of periods of delay for a report to be submitted from the
	 * customer facility to the supplier facility.
	 * @return
	 */
	@Override
	public int getReportDelay() {
		return oneTierReportDelay;
	}

	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "{" + NL);
		sb.append(StringUtils.prependToEachLine(
				"replenishment function = " + function.prettyToString(),
				PAD));
		sb.append(NL);
		sb.append(PAD + "report delay = " + oneTierReportDelay + NL);
		sb.append("}");
		return sb.toString();
	}

}
