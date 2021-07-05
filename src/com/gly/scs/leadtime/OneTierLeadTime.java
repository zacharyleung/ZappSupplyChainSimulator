package com.gly.scs.leadtime;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.random.IntegerRandomVariable;
import com.gly.scs.sim.RandomParameters;

/**
 * This class represents the one tier lead time, i.e. the lead time from
 * the national facility to regional facilities, or the lead time from
 * the regional facilities to the retail facilities.
 * @author zacharyleung
 *
 */
public class OneTierLeadTime extends LeadTime {

	protected final long randomSeed;
	protected final int startPeriod;
	protected final int endPeriod;
	protected HashMap<String, SingleFacilityLeadTime> map = new HashMap<>();

	private OneTierLeadTime(Builder builder) {
		SingleFacilityLeadTimeFactory sfltFactory = builder.sfltFactory;
		this.randomSeed = builder.randomSeed;
		this.startPeriod = builder.startPeriod;
		this.endPeriod = builder.endPeriod;
		Collection<String> facilityIds = builder.facilityIds; 

		RandomParameters randomParameters = new RandomParameters.Builder()
				.withRandomSeed(randomSeed)
				.withStartPeriod(startPeriod)
				.withEndPeriod(endPeriod)
				.build();
		
		RandomDataGenerator random = new RandomDataGenerator();
		random.reSeed(randomSeed);
		//System.out.println("OneTierLeadTime()");
		//System.out.println(Arrays.toString(facilityIds.toArray()));
		for (String facilityId : facilityIds) {
			//System.out.printf("facilityId = %s%n", facilityId);
			map.put(facilityId, 
					sfltFactory.build(facilityId, randomParameters, random));
		}
	}

	public static class Builder {
		private long randomSeed;
		private int startPeriod;
		private int endPeriod;
		private SingleFacilityLeadTimeFactory sfltFactory;
		private Collection<String> facilityIds;

		public Builder withRandomSeed(long randomSeed) {
			this.randomSeed = randomSeed;
			return this;
		}
		
		public Builder withStartPeriod(int startPeriod) {
			this.startPeriod = startPeriod;
			return this;
		}
		
		public Builder withEndPeriod(int endPeriod) {
			this.endPeriod = endPeriod;
			return this;
		}

		public Builder withSfltFactory(SingleFacilityLeadTimeFactory sfltFactory) {
			this.sfltFactory = sfltFactory;
			return this;
		}

		public Builder withFacilityIds(Collection<String> facilityIds) {
			this.facilityIds = facilityIds;
			return this;
		}

		public OneTierLeadTime build() {
			return new OneTierLeadTime(this);
		}
	}

	/**
	 * Return the lead time of the shipment sent to the facility to at
	 * period t.  
	 * @param to Facility ID
	 * @param t
	 */
	@Override
	public int getLeadTime(String facilityId, int t) {
		return map.get(facilityId).getLeadTime(t);
	}

	public IntegerRandomVariable getLeadTimeRandomVariable(
			String facilityId, int t) {
		return map.get(facilityId).getLeadTimeRandomVariable(t);
	}

	@Override
	public double getAccessibility(String facilityId, int t) {
		return map.get(facilityId).getAccessibility(t);
	}

	@Override
	public int getSecondaryLeadTime(String facilityId, int t)
			throws NoSuchElementException {
		// if this is a retail facility, just return the secondary
		// lead time
		// if this is a regional facility, this method shouldn't be
		// called on this object!
		return getLeadTime(facilityId, t);
	}

}
