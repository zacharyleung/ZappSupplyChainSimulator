package com.gly.scs.sched;

public class RegionalOffsetEntry {
	public final String regionalFacilityId;
	public final int offset;
	
	private RegionalOffsetEntry(Builder builder) {
		this.regionalFacilityId = builder.regionalFacilityId;
		this.offset = builder.offset;
	}
	
	public static class Builder {
		private int offset;
		private String regionalFacilityId = null;
			
		public Builder withOffset(int offset) {
			this.offset = offset;
			return this;
		}
		
		public Builder withRegionalFacilityId(String regionalFacilityId) {
			this.regionalFacilityId = regionalFacilityId;
			return this;
		}
		
		public RegionalOffsetEntry build() {
			return new RegionalOffsetEntry(this);
		}
	}
}
