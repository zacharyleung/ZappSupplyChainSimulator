package com.gly.scs.leadtime;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;

public class ConstantSingleFacilityLeadTimeFactory extends SingleFacilityLeadTimeFactory {

	private HashMap<String, Integer> map = new HashMap<>();
	
	public ConstantSingleFacilityLeadTimeFactory(Collection<Entry> entries) {
		for (Entry entry : entries) {
			map.put(entry.facilityId, entry.value);
		}
	}
	
	public static class Entry {
		private final String facilityId;
		private final int value;
		
		public Entry(String facilityId, int value) {
			this.facilityId = facilityId;
			this.value = value;
		}
	}
	
	@Override
	SingleFacilityLeadTime build(String facilityId, RandomParameters randomParameters,
			RandomDataGenerator random) {
		//System.out.println("ConstantSingleFacilityLeadTimeFactory");
		//System.out.printf("facilityId = %s%n", facilityId);
		int value = map.get(facilityId);
		return new ConstantSingleFacilityLeadTime(value);
	}

}
