package com.gly.scs.leadtime;

import java.util.NoSuchElementException;

import com.gly.random.IntegerRandomVariable;

public abstract class LeadTime {
	public boolean hasLeadTime(String facilityId, int t) {
		try {
			getLeadTime(facilityId, t);
			return true;
		} catch (NoSuchElementException e) {
			return false;			
		}
	}

	/**
	 * Return the probability that a given facility during a given
	 * period is accessible for shipments to be delivered.
	 * @param facilityId
	 * @param t
	 * @return
	 */
	public abstract double getAccessibility(String facilityId, int t);
	
	/**
	 * Return the lead time for a shipment to the facility made during
	 * period t.
	 * @param facilityId
	 * @param t
	 * @return 
	 * @throws NoSuchElementException If the shipment arrival period
	 * exceeds the simulation horizon. 
	 */
	public abstract int getLeadTime(String facilityId, int t)
			throws NoSuchElementException;
	
	/**
	 * Return the secondary lead time part of the lead time if applicable.
	 * @param facilityId
	 * @param t
	 * @return
	 * @throws NoSuchElementException
	 */
	public abstract int getSecondaryLeadTime(String facilityId, int t)
			throws NoSuchElementException;
	
	public abstract IntegerRandomVariable getLeadTimeRandomVariable(
			String facilityId, int t);
}
