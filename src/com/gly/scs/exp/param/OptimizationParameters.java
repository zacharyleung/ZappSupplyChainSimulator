package com.gly.scs.exp.param;

import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.ShipmentLeadTimeType;
import com.gly.scs.opt.UnmetDemandLowerBoundType;

public class OptimizationParameters {
	public static final String NAME = "optimization";
	public static final int FORECAST_LEVEL = ExperimentParameters.FORECAST_LEVEL_GOOD;
	public static final int REPORT_DELAY = 0;
	public final static double UNMET_DEMAND_COST = 16;

	/** The number of tangents must be at least 2. */
	//public static final int NUMBER_OF_TANGETS = 7;  // Value in previous experiments
	public static final int NUMBER_OF_TANGETS = 10;

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
