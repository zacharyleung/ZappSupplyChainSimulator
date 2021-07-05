package com.gly.scs.opt;

import java.util.SortedSet;
import java.util.TreeSet;

import com.gly.util.SparseArray;

public class ShipmentArrivalPeriods {
	private SparseArray<SortedSet<Integer>> sets = new SparseArray<>();
	
	public void put(int r, int t) {
		try {
			sets.get(r).add(t);
		} catch (Exception e) {
			TreeSet<Integer> set = new TreeSet<>();
			set.add(t);
			sets.put(r, set);
		}
	}
	
	/**
	 * Return a strictly increasing list of shipment arrival periods.
	 * By definition this list does not include duplication shipment
	 * arrival periods.
	 * @param r
	 * @return
	 */
	public SortedSet<Integer> getShipmentArrivalPeriods(int r) {
		return sets.get(r);
	}
}
