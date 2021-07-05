package com.gly.scs.exp.param;

import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTime.MinimumValue;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.ShipmentLeadTimeType;
import com.gly.scs.opt.UnmetDemandLowerBoundType;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.replen.OrderUpToLevel.Type;

public class ExperimentParameters {

	public static final int RETAIL_INITIAL_INVENTORY_PERIODS = 24;
	public static final int NUMBER_OF_WARMUP_YEARS = 0;
	public static final int NUMBER_OF_DATA_COLLECTION_YEARS = 5;
	public static final int NUMBER_OF_TIMESTEPS_IN_MONTH = 4;
	
	/** Bad forecast level */
	public final static int FORECAST_LEVEL_BAD = 0;
	/** Good forecast level */
	public final static int FORECAST_LEVEL_GOOD = 1;
	
	public static int getNumberOfTimestepsInYear() {
		return 12 * NUMBER_OF_TIMESTEPS_IN_MONTH;
	}

	public static int getNumberOfTimestepsInMonth() {
		return NUMBER_OF_TIMESTEPS_IN_MONTH;
	}
	
	public static int getNumberOfWarmupYears() {
		return NUMBER_OF_WARMUP_YEARS;
	}

	public static int getNumberOfWarmupTimesteps() {
		return getNumberOfWarmupYears() * getNumberOfTimestepsInYear();
	}
	
	public static int getNumberOfDataCollectionYears() {
		return NUMBER_OF_DATA_COLLECTION_YEARS;
	}

	public static int getNumberOfDataCollectionTimesteps() {
		return getNumberOfDataCollectionYears() * getNumberOfTimestepsInYear();
	}

	public static MinimumValue getMinimumLeadTimeValue() {
		return MinimumValue.ZERO;
	}
	
	public static double getRetailInitialInventoryPeriods() {
		return RETAIL_INITIAL_INVENTORY_PERIODS;
	}
	
	public static OrderUpToLevel getRetailInitialInventoryLevel() 	 {
		return new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(getNumberOfTimestepsInYear())
				.withOrderUpToPeriods(RETAIL_INITIAL_INVENTORY_PERIODS)
				.withType(Type.DEMAND)
				.build();
	}
	
	/** I think this class is old code that is never used. */
	public static class Optimization {
		public static final String NAME = "optimization";
		public static final int NUMBER_OF_TANGETS = 7;
		public static final int FORECAST_LEVEL = FORECAST_LEVEL_GOOD;
		public static final int REPORT_DELAY = 0;		
		public final static double UNMET_DEMAND_COST = 16;

		public final static ShipmentLeadTimeType LEAD_TIME_TYPE =
				ShipmentLeadTimeType.CONSERVATIVE;
		public final static double LEAD_TIME_PERCENTILE = 0.99;
		public final static HoldingCostType HOLDING_COST_TYPE =
				HoldingCostType.FACILITY_ACCESSIBILITY;
				//HoldingCostType.CONSTANT;
		public final static UnmetDemandLowerBoundType
		UNMET_DEMAND_LOWER_BOUND_TYPE =
		UnmetDemandLowerBoundType.MULTI_PERIOD;
	}
}
