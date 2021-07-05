package com.gly.scs.domain;

public class NationalFacility extends Facility {
	private NationalFacility(Builder builder) {
		super(builder.id, builder.startPeriod, builder.endPeriod);
	}
	
	public static class Builder {
		private String id;
		private int startPeriod;
		private int endPeriod;
		
		public Builder withId(String id) {
			this.id = id;
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
		
		public NationalFacility build() {
			return new NationalFacility(this);
		}
	}
}
