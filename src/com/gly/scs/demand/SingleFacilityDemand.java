package com.gly.scs.demand;

import com.gly.random.AbstractRandomVariable;

/**
 * Abstract Single Facility Demand.
 * @author zacharyleung
 *
 */
public abstract class SingleFacilityDemand {
	abstract int getDemand(int t);
	
	abstract double getMeanDemand(int t);
	
	abstract AbstractRandomVariable getDemandForecast(
			GetDemandForecastParameters params);
}
