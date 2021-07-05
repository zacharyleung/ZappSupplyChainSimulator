package com.gly.scs.leadtime;

import java.util.*;

import com.gly.random.*;
import com.gly.random.IntegerRandomVariable.Entry;
import com.gly.scs.domain.Topology;
import com.gly.scs.sim.*;

/**
 * This class represents the cross-docking lead time, i.e. the lead time
 * from the national facility to retail facilities.
 * @author zacharyleung
 *
 */
public class XdockLeadTime extends LeadTime {

	private final LeadTime primaryLeadTime;
	private final LeadTime secondaryLeadTime;
	private final Topology topology;

	private XdockLeadTime(Builder builder) {
		this.topology = builder.topology;

		RandomParameters randomParameters = builder.randomParameters;

		//System.out.println("XdockLeadTime");
		//System.out.println(Arrays.toString(topology.getRegionalIds().toArray()));
		this.primaryLeadTime = new OneTierLeadTime.Builder()
				.withRandomSeed(randomParameters.regionalLeadTimeRandomSeed)
				.withStartPeriod(randomParameters.startPeriod)
				.withEndPeriod(randomParameters.endPeriod)
				.withFacilityIds(topology.getRegionalIds())
				.withSfltFactory(builder.primarySfltFactory)
				.build();

		this.secondaryLeadTime = new OneTierLeadTime.Builder()
				.withRandomSeed(randomParameters.retailLeadTimeRandomSeed)
				.withStartPeriod(randomParameters.startPeriod)
				.withEndPeriod(randomParameters.endPeriod)
				.withFacilityIds(topology.getRetailIds())
				.withSfltFactory(builder.secondarySfltFactory)
				.build();
		
		System.out.printf("Creating XdockLeadTime(regional seed = %d, retail seed = %d)%n",
				randomParameters.regionalLeadTimeRandomSeed,
				randomParameters.retailLeadTimeRandomSeed);		
	}

	public static class Builder {
		private RandomParameters randomParameters;
		private SingleFacilityLeadTimeFactory primarySfltFactory;
		private SingleFacilityLeadTimeFactory secondarySfltFactory;
		private Topology topology;

		public Builder withRandomParameters(RandomParameters randomParameters) {
			this.randomParameters = randomParameters;
			return this;
		}

		public Builder withPrimarySfltFactory(
				SingleFacilityLeadTimeFactory primarySfltFactory) {
			this.primarySfltFactory = primarySfltFactory;
			return this;
		}

		public Builder withSecondarySfltFactory(
				SingleFacilityLeadTimeFactory secondarySfltFactory) {
			this.secondarySfltFactory = secondarySfltFactory;
			return this;
		}

		public Builder withTopology(Topology topology) {
			this.topology = topology;
			return this;
		}

		public XdockLeadTime build() {
			return new XdockLeadTime(this);
		}

	}

	public int getLeadTime(String retailFacilityId, int period) 
			throws NoSuchElementException {
		int t = period;
		String regionalFacilityId = topology.getRegional(retailFacilityId);
		int l = primaryLeadTime.getLeadTime(regionalFacilityId, t);
		// the period when the shipment arrives at the regional facility
		int u = t + l;
		int m = secondaryLeadTime.getLeadTime(retailFacilityId, u);
		// the period when the shipment arrives at the retail facility
		int v = u + m;
		// the period when the shipment arrives at the retail facility
		//System.out.println("retailFacilityId = " + retailFacilityId);
		//System.out.println("period = " + period);
		//System.out.println("period received by regional = " + u);
		//System.out.println("period received by retail = " + v);
		return v - t;
	}

	@Override
	public int getSecondaryLeadTime(String retailFacilityId, int period) {
		int t = period;
		String regionalFacilityId = topology.getRegional(retailFacilityId);
		int l = primaryLeadTime.getLeadTime(regionalFacilityId, t);
		// the period when the shipment arrives at the regional facility
		int u = t + l;
		int m = secondaryLeadTime.getLeadTime(retailFacilityId, u);
		return m;
	}

	public IntegerRandomVariable getLeadTimeRandomVariable(
			String retailFacilityId, int period) {

		int t = period;
		String regionalFacilityId = topology.getRegional(retailFacilityId);
		int l = primaryLeadTime.getLeadTime(regionalFacilityId, t);
		// the period when the shipment arrives at the regional facility
		int u = t + l;

		// slt: secondary lead time
		IntegerRandomVariable slt =
				secondaryLeadTime.getLeadTimeRandomVariable(retailFacilityId, u);

		List<Entry> entries = slt.getProbabilities();
		LinkedList<Entry> newEntries = new LinkedList<>();
		for (Entry entry : entries) {
			newEntries.addLast(new Entry.Builder()
					.withValue(l + entry.value)
					.withProbability(entry.probability)
					.build());
		}

		// tlt: total lead time
		EmpiricalIntegerRandomVariable tlt =
				new EmpiricalIntegerRandomVariable(newEntries);

		return tlt;
	}

	@Override
	public double getAccessibility(String facilityId, int t) {
		return secondaryLeadTime.getAccessibility(facilityId, t);
	}

}
