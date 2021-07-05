package com.gly.scs.leadtime;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTime.Builder;
import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTime.MinimumValue;
import com.gly.scs.sim.RandomParameters;

public class VehicleAccessibilitySingleFacilityLeadTimeFactory 
extends SingleFacilityLeadTimeFactory {
	
	private HashMap<String, Entry> map;
	private MinimumValue minimumValue;
	
	public VehicleAccessibilitySingleFacilityLeadTimeFactory(Collection<Entry> entries,
			MinimumValue minimumValue) {
		map = new HashMap<>();
		for (Entry entry : entries) {
			//System.out.println("VehicleAccessibilitySingleFacilityLeadTimeFactory");
			//System.out.println(entry);
			map.put(entry.facilityId, entry);
		}
		this.minimumValue = minimumValue;
	}

	@Override
	SingleFacilityLeadTime build(String facilityId,
			RandomParameters randomParameters, RandomDataGenerator random) {
		Entry entry = map.get(facilityId);
		//System.out.println("VehicleAccessibilitySingleFacilityLeadTimeFactory.build()");
		//System.out.println("facilityId = " + facilityId);
		//System.out.println(entry);
		
		return new VehicleAccessibilitySingleFacilityLeadTime.Builder()
		.withAccessibility(entry.accessibility)
		.withMean(entry.mean)
		.withStartPeriod(randomParameters.startPeriod)
		.withEndPeriod(randomParameters.endPeriod)
		.withRandomDataGenerator(random)
		.withMinimumValue(minimumValue)
		.build();
	}
	
	public static class Entry {
		public final String facilityId;
		public final double mean;
		public final double[] accessibility;
		private MinimumValue minimumValue;
		
		private Entry(Builder builder) {
			this.mean = builder.mean;
			this.accessibility = builder.accessibility;
			this.facilityId = builder.facilityId;
		}
		
		public static class Builder {
			private String facilityId;
			private double mean;
			private double[] accessibility;
			
			public Builder withMean(double mean) {
				this.mean = mean;
				return this;
			}

			public Builder withAccessibility(double[] accessibility) {
				this.accessibility = Arrays.copyOf(accessibility, accessibility.length);
				return this;
			}

			public Builder withFacilityId(String facilityId) {
				this.facilityId = facilityId;
				return this;
			}
			
			public Entry build() {
				return new Entry(this);
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("VehicleAccessibilitySingleFacilityLeadTimeFactory.Entry {%n"));
			sb.append(String.format("    facility ID = %s%n", facilityId));
			sb.append(String.format("    mean = %.2f%n", mean));
			sb.append(String.format("    accessibility = %s%n", Arrays.toString(accessibility)));
			sb.append("}");
			return sb.toString();
		}
	}

}
