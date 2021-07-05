package com.gly.scs.demand;

public class GetDemandForecastParameters {
	public final String retailFacilityId;
	public final int currentTimestep;
	public final int futureTimestep;
	public final int forecastLevel;
	
	private GetDemandForecastParameters(Builder builder) {
		this.retailFacilityId = builder.retailFacilityId;
		this.currentTimestep = builder.currentTimestep;
		this.futureTimestep = builder.futureTimestep;
		this.forecastLevel = builder.forecastLevel;
	}
	
	public static class Builder {
		private String retailFacilityId;
		private int currentTimestep;
		private int futureTimestep;
		private int forecastLevel;
		
		public Builder withRetailFacility(String retailFacilityId) {
			this.retailFacilityId = retailFacilityId;
			return this;
		}
		
		public Builder withCurrentTimestep(int currentTimestep) {
			this.currentTimestep = currentTimestep;
			return this;
		}
		
		public Builder withFutureTimestep(int futureTimestep) {
			this.futureTimestep = futureTimestep;
			return this;
		}
		
		public Builder withForecastLevel(int forecastLevel) {
			this.forecastLevel = forecastLevel;
			return this;
		}
		
		public GetDemandForecastParameters build() {
			return new GetDemandForecastParameters(this);
		}
	}
}
