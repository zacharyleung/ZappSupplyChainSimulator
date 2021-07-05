package com.gly.scs.exp.exp07;

public class ScenarioParameters {
	public final int forecastLevel;
	public final int oneTierReportDelay;
	
	private ScenarioParameters(Builder b) {
		this.forecastLevel = b.forecastLevel;
		this.oneTierReportDelay = b.oneTierReportDelay;
	}
	
	public static class Builder {
		private int forecastLevel;
		private int oneTierReportDelay;
		
		public Builder withForecastLevel(int i) {
			this.forecastLevel = i;
			return this;
		}

		public Builder withOneTierReportDelay(int i) {
			this.oneTierReportDelay = i;
			return this;
		}
		
		public ScenarioParameters build() {
			return new ScenarioParameters(this);
		}
	}
}
