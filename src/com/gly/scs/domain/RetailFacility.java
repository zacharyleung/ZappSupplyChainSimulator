package com.gly.scs.domain;


public class RetailFacility extends Facility {
	private RetailFacility(Builder builder) {
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
		
		public RetailFacility build() {
			return new RetailFacility(this);
		}
	}
}
