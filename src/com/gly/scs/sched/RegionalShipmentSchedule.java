package com.gly.scs.sched;

import java.util.*;

import com.gly.util.MathUtils;

public class RegionalShipmentSchedule extends AbstractShipmentSchedule {

	public final int cycle;
	private HashMap<String, Integer> map = new HashMap<>();
	
	public RegionalShipmentSchedule(Builder builder) {
		this.cycle = builder.cycle;
		for (RegionalOffsetEntry entry : builder.regionalOffsetEntries) {
			map.put(entry.regionalFacilityId, entry.offset);
		}
		
		// check that there are no duplicate regional facility IDs
		if (map.size() < builder.regionalOffsetEntries.size()) {
			throw new IllegalArgumentException("duplicate regional facility ID!");
		}
	}
	
	public boolean isShipmentPeriod(String regionalFacilityId, int period) {
		int offset = map.get(regionalFacilityId);
		return MathUtils.positiveModulo(period, cycle) == 
				MathUtils.positiveModulo(offset, cycle);
	}
	
	public static class Builder {
		private int cycle;
		private List<RegionalOffsetEntry> regionalOffsetEntries;
		
		public Builder withCycle(int cycle) {
			this.cycle = cycle;
			return this;
		}
		
		public Builder withRegionalOffsetEntries(
				Collection<RegionalOffsetEntry> regionalOffsetEntries) {
			this.regionalOffsetEntries = new ArrayList<>(regionalOffsetEntries);
			return this;
		}
		
		public RegionalShipmentSchedule build() {
			return new RegionalShipmentSchedule(this);
		}
	}
	
}
