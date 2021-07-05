package com.gly.scs.demand;

import java.util.*;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.random.AbstractRandomVariable;
import com.gly.scs.sim.RandomParameters;

public class DemandModel {
	
	protected final long randomSeed;
	protected final int startPeriod;
	protected final int endPeriod;
	protected HashMap<String, SingleFacilityDemand> map = new HashMap<>();
	protected RandomDataGenerator random;
	
	private DemandModel(Builder builder) {
		RandomParameters randomParameters = builder.randomParameters;
		this.randomSeed = randomParameters.demandRandomSeed;
		this.startPeriod = randomParameters.startPeriod;
		this.endPeriod = randomParameters.endPeriod;
		SingleFacilityDemandFactory sfdFactory = builder.sfdFactory; 
		
		RandomDataGenerator random = new RandomDataGenerator();
		random.reSeed(randomSeed);
//		System.out.printf("DemandModel%n");
		for (String facilityId : builder.facilityIds) {
//			System.out.println(facilityId);
			map.put(facilityId, 
					sfdFactory.build(facilityId, randomParameters, random));
		}

		System.out.printf("Creating DemandModel(random seed = %d)%n", randomSeed);		
	}
	
	public static class Builder {
		private RandomParameters randomParameters;
		private SingleFacilityDemandFactory sfdFactory;
		private Collection<String> facilityIds;
		
		public Builder withRandomParameters(RandomParameters randomParameters) {
			this.randomParameters = randomParameters;
			return this;
		}
		
		public Builder withSfdFactory(SingleFacilityDemandFactory sfdFactory) {
			this.sfdFactory = sfdFactory;
			return this;
		}
		
		public Builder withFacilityIds(Collection<String> facilityIds) {
			this.facilityIds = facilityIds;
			return this;
		}

		public DemandModel build() {
			return new DemandModel(this);
		}		
	}
	
	/**
	 * Return the demand at the retail facility at period t.
	 * @param retailFacilityId
	 * @param t
	 * @return
	 */
	public int getDemand(String retailFacilityId, int t) {
//		System.out.printf("DemandModel.getDemand(%s, %d)\n",
//				retailFacilityId, t); 
		return map.get(retailFacilityId).getDemand(t);
	}
	
	public double getMeanDemand(String retailFacilityId, int t) {
		return map.get(retailFacilityId).getMeanDemand(t);
	}
	
	/**
	 * Return a random variable representing the forecast of demand for
	 * the facility at the beginning of period t for the demand during
	 * period s.
	 * It is assumed that t <= s.
	 * @param facilityId
	 * @param t
	 * @param s
	 * @return
	 */
	public AbstractRandomVariable getDemandForecast(
			GetDemandForecastParameters params) {
		return map.get(params.retailFacilityId).getDemandForecast(params);
	}
}
