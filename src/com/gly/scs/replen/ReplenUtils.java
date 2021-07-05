package com.gly.scs.replen;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.gly.scs.demand.DemandModel;
import com.gly.scs.domain.Facility;
import com.gly.scs.domain.Report;
import com.gly.scs.leadtime.LeadTime;

public class ReplenUtils {
	public static double meanPast(Report report, OrderUpToLevel.Type type,
			int timesteps) {
		// mean demand per period
		double sum = 0;
		for (int i = 0; i < timesteps; ++i) {
			if (type == OrderUpToLevel.Type.DEMAND) {
				sum += report.getPastDemand(i);
			} else {
				sum += report.getPastConsumption(i);
			}
		}
		return sum / timesteps;
	}

	public static double meanPastType(Report report,
			OrderUpToLevel.Type type,
			int startTimestep, int endTimestep) {
		if (startTimestep >= endTimestep) {
			System.out.printf("startTimestep = %d\n", startTimestep);
			System.out.printf("endTimestep = %d\n", endTimestep);
			throw new IllegalArgumentException("startTimestep >= endTimestep");
		}
		//System.out.printf("ReplenUtils.meanPastDemand(%d,%d)\n", 
		//		startTimestep, endTimestep);
		SummaryStatistics stats = new SummaryStatistics();
		for (int t = startTimestep; t < endTimestep; ++t) {
			if (type == OrderUpToLevel.Type.DEMAND) {
				stats.addValue(report.getPastDemandAtTimestep(t));
			} else { // type == OrderUpToLevel.Type.CONSUMPTION
				stats.addValue(report.getConsumptionAtTimestep(t));
			}
		}
		return stats.getMean();
	}

	/** Compute mean seasonality index */
	public static double meanSeasonalityIndex(DemandModel demand, 
			Facility customer, int t, int u) {
		SummaryStatistics stats = new SummaryStatistics();
		String customerId = customer.getId();
		for (int i = t; i < u; ++i) {
			stats.addValue(demand.getMeanDemand(customerId, i));
		}
		return stats.getMean();
	}

	/**
	 * The median shipment arrival timestep for a shipment sent in
	 * timestep t.
	 */
	public static int getMedianSat(LeadTime leadTime, 
			Facility customer, int t) {
		String customerId = customer.getId();
		return t + leadTime.getLeadTimeRandomVariable(customerId, t)
		.inverseCumulativeProbability(0.5);
	}
}
