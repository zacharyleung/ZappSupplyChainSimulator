package com.gly.scs.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Topology {
	
	/**
	 * Map of retail facility ID to regional facility ID;
	 */
	private final HashMap<String, String> map = new HashMap<>();
	
	public Topology(List<Entry> entries) {
		for (Entry entry : entries) {
			// check if the map already contains this retail facility ID
			// if so, throw an IllegalArgumentException
			if (map.containsKey(entry.retailFacilityId)) {
				throw new IllegalArgumentException(
						String.format("Duplicate refail facility ID entry: %s",
								entry.retailFacilityId));
			}
			map.put(entry.retailFacilityId, entry.regionalFacilityId);
		}
	}
	
	public Set<String> getRetailIds() {
		return map.keySet();
	}
	
	/**
	 * Return the facility IDs of the retail facilities in the specified
	 * region.
	 * 
	 * @param regionalFacilityId
	 * @return
	 */
	public Set<String> getRetailIds(String regionalFacilityId) {
		List<String> ids = new LinkedList<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue().equals(regionalFacilityId)) {
				ids.add(entry.getKey());
			}
		}
		return new HashSet<>(ids);
	}
	
	public Set<String> getRegionalIds() {
		return new HashSet<>(map.values());
	}
	
	/**
	 * Return the facility ID of the regional facility corresponding to the
	 * retail facility.
	 * @param retailFacilityId
	 * @return Facility ID of the regional facility corresponding to the
	 * retail facility.
	 */
	public String getRegional(String retailFacilityId) {
		return map.get(retailFacilityId);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String nl = System.getProperty("line.separator");
		sb.append("Topology{" + nl);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append("  retail = " + entry.getKey() +
					", regional = " + entry.getValue() + nl);
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static class Entry {
		public final String retailFacilityId;
		public final String regionalFacilityId;
		
		private Entry(Builder builder) {
			this.retailFacilityId = builder.retailFacilityId;
			this.regionalFacilityId = builder.regionalFacilityId;
			
			if (retailFacilityId == null) {
				throw new IllegalArgumentException("retailFacilityId = null!");
			}
			if (regionalFacilityId == null) {
				throw new IllegalArgumentException("regionalFacilityId = null!");
			} 
		}
		
		public static class Builder {
			private String retailFacilityId = null;
			private String regionalFacilityId = null;
				
			public Builder withRetailFacilityId(String retailFacilityId) {
				this.retailFacilityId = retailFacilityId;
				return this;
			}
			
			public Builder withRegionalFacilityId(String regionalFacilityId) {
				this.regionalFacilityId = regionalFacilityId;
				return this;
			}
			
			public Entry build() {
				return new Entry(this);
			}
		}
	}
}
