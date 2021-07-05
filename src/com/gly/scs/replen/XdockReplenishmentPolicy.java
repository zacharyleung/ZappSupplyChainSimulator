package com.gly.scs.replen;

import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.util.StringUtils;

public class XdockReplenishmentPolicy
extends AbstractReplenishmentPolicy<NationalFacility, XdockLeadTime> {

	public XdockReplenishmentPolicy(int oneTierReportDelay,
			AbstractReplenishmentFunction
			<NationalFacility, XdockLeadTime> function) {
		super(oneTierReportDelay, function, false);
	}

	public XdockReplenishmentPolicy(int oneTierReportDelay,
			AbstractReplenishmentFunction
			<NationalFacility, XdockLeadTime> function,
			boolean shouldAlwaysSubmitReports) {
		super(oneTierReportDelay, function, shouldAlwaysSubmitReports);
	}

	/**
	 * Number of periods of delay for a report to be submitted from the
	 * retail facility to the national facility.
	 * @return
	 */
	@Override
	public int getReportDelay() {
		return 2 * oneTierReportDelay;
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
