package com.gly.scs.demand;

import java.util.*;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.sim.RandomParameters;
import com.gly.util.MathUtils;

public class MmfeLevelsSingleFacilityDemandFactory extends SingleFacilityDemandFactory {

	private final double[] standardDeviation;
	private final double[] varianceProportion;
	private final HashMap<String, double[]> map;
	
	private MmfeLevelsSingleFacilityDemandFactory(Builder builder) {
		this.standardDeviation = builder.standardDeviation;
		this.varianceProportion = builder.varianceProportion;
		this.map = builder.map;
	}

	public static class Builder {
		private double[] standardDeviation;
		private double[] varianceProportion;
		private int forecastLevel;	
		private HashMap<String, double[]> map;

		public Builder withEntries(Collection<Entry> entries) {
			map = new HashMap<>();
			for (Entry entry : entries) {
				map.put(entry.facilityId, entry.mean);
			}
			return this;
		}
		
		public Builder withStandardDeviation(double[] standardDeviation) {
			this.standardDeviation = standardDeviation;
			return this;
		}

		public Builder withVarianceProportion(double[] varianceProportion) {
			this.varianceProportion = varianceProportion;
			return this;
		}

		public Builder withForecastLevel(int level) {
			this.forecastLevel = level;
			return this;
		}

		public MmfeLevelsSingleFacilityDemandFactory build() {
			return new MmfeLevelsSingleFacilityDemandFactory(this);
		}

	}

	public static class Entry {
		public final String facilityId;
		public final double[] mean;

		public Entry(String facilityId, double[] mean) {
			this.facilityId = facilityId;
			this.mean = Arrays.copyOf(mean, mean.length);
		}
	}

	@Override
	public SingleFacilityDemand build(String facilityId,
			RandomParameters randomParameters,
			RandomDataGenerator random) {
		double[] mean = map.get(facilityId);
		//System.out.println("MmfeLevelsSingleFacilityDemandFactory.build()");
		//System.out.println("facilityId = " + facilityId);
		//System.out.println(entry);

		return new MmfeLevelsSingleFacilityDemand.Builder()
		.withDemandMean(mean)
		.withStandardDeviation(standardDeviation)
		.withForecastVarianceProportion(varianceProportion)
		.withStartPeriod(randomParameters.startPeriod)
		.withEndPeriod(randomParameters.endPeriod)
		.withRandomDataGenerator(random)
		.build();
	}

	@Override
	public double getMeanDemandPerPeriod() {
		double sum = 0;
		for (Map.Entry<String, double[]> entry : map.entrySet()) 	{
		    double[] demandMean = entry.getValue();
		    sum += MathUtils.getMean(demandMean);
		}
		return sum;
	}

}
