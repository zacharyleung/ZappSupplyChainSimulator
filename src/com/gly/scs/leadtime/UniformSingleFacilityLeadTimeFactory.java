package com.gly.scs.leadtime;

import java.util.*;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;

public class UniformSingleFacilityLeadTimeFactory extends SingleFacilityLeadTimeFactory {

	private HashMap<String, Entry> map;
	
	public UniformSingleFacilityLeadTimeFactory(Collection<Entry> entries) {
		map = new HashMap<>();
		for (Entry entry : entries) {
			map.put(entry.facilityId, entry);
		}
	}
	
	@Override
	SingleFacilityLeadTime build(String facilityId, RandomParameters randomParameters,
			RandomDataGenerator random) {
		Entry entry = map.get(facilityId);
		return new UniformSingleFacilityLeadTime.Builder()
		.withMin(entry.min)
		.withMax(entry.max)
		.withStartPeriod(randomParameters.startPeriod)
		.withEndPeriod(randomParameters.endPeriod)
		.withRandomDataGenerator(random)
		.build();
	}

	public static class Entry {
		public final String facilityId;
		public final int min;
		public final int max;
		
		private Entry(Builder builder) {
			this.min = builder.min;
			this.max = builder.max;
			this.facilityId = builder.facilityId;
		}
		
		public static class Builder {
			private String facilityId;
			private int min;
			private int max;
			
			public Builder withMax(int max) {
				this.max = max;
				return this;
			}

			public Builder withMin(int min) {
				this.min = min;
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
	}
}
