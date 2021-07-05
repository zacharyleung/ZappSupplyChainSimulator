package com.gly.scs.replen;

import java.util.*;

import com.gly.scs.domain.Report;

public abstract class SomOutputAdapter {
	public static Collection<ShipmentDecision> convert(Report[] reports,
			Collection<com.gly.scs.opt.ShipmentDecision> input) {
		LinkedList<ShipmentDecision> output = new LinkedList<>();
		for (com.gly.scs.opt.ShipmentDecision in : input) {
			int r = in.retailId;
			output.add(new ShipmentDecision.Builder()
			.withFacility(reports[r].getFacility())
			.withPeriod(in.period)
			.withQuantity(in.quantity)
			.build());
		}
		return output;
	}
}
