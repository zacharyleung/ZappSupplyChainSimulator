package com.gly.scs.zambia;

import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.ShipmentLeadTimeType;
import com.gly.scs.opt.UnmetDemandLowerBoundType;
import com.gly.scs.replen.AmiOrderUpToLevel;
import com.gly.scs.replen.OrderUpToLevel;
import com.gly.scs.replen.OrderUpToLevel.Type;

/**
 * Time parameters of the simulation experiments.
 *  
 * @author zacleung
 *
 */
public class ZambiaSimulationParameters {
	public final static int NUMBER_OF_PERIODS_IN_YEAR = 48;
	
	public final static int WARM_UP_YEARS = 3;
	
	public final static int DATA_COLLECTION_YEARS = 1;
	
	
	public final static int beforePeriods =
			2 * NUMBER_OF_PERIODS_IN_YEAR;
	public final static int startPeriod = 0;
	public final static int warmUpPeriods = 
			WARM_UP_YEARS * NUMBER_OF_PERIODS_IN_YEAR;
	public final static int simulationPeriods = 
			DATA_COLLECTION_YEARS * NUMBER_OF_PERIODS_IN_YEAR;
	public final static int forecastHorizon = 48;

	// replenishment policy
	public final static int amiHistoryPeriods = 12;

	// initial inventory levels
	public final static int regionalInitialInventoryPeriods = 12;
	public final static int retailInitialInventoryPeriods = 8;
	public final static OrderUpToLevel regionalInitialInventoryLevel = 
			new AmiOrderUpToLevel.Builder()
	.withHistoryPeriods(amiHistoryPeriods)
	.withOrderUpToPeriods(regionalInitialInventoryPeriods)
	.withType(Type.DEMAND)
	.build();
	public final static OrderUpToLevel retailInitialInventoryLevel = 
			new AmiOrderUpToLevel.Builder()
	.withHistoryPeriods(amiHistoryPeriods)
	.withOrderUpToPeriods(retailInitialInventoryPeriods)
	.withType(Type.DEMAND)
	.build();

	// order-up-to periods
	public final static int istockRegionalOrderUpToPeriods = 12;
	public final static int istockRetailOrderUpToPeriods = 8;
	public final static int xdockOrderUpToPeriods = 16;

	// forecast levels
	public final static int FORECAST_LEVEL_MYOPIC = 0;
	public final static int FORECAST_LEVEL_INDUSTRY = 1;

	// optimization replenishment policy parameters
	public final static int 
	OPTIMIZATION_REPLENISHMENT_POLICY_FORECAST_HORIZON = forecastHorizon;
	public final static double
	OPTIMIZATION_REPLENISHMENT_POLICY_UNMET_DEMAND_COST = 16;
	public final static double
	OPTIMIZATION_REPLENISHMENT_POLICY_SHIPMENT_LEAD_TIME_PERCENTILE = 0.99;
	public final static ShipmentLeadTimeType 
	OPTIMIZATION_REPLENISHMENT_POLICY_LEAD_TIME_TYPE =
			ShipmentLeadTimeType.CONSERVATIVE;
	public final static HoldingCostType 
	OPTIMIZATION_REPLENISHMENT_POLICY_HOLDING_COST =
			HoldingCostType.FACILITY_ACCESSIBILITY;
	public final static UnmetDemandLowerBoundType
	UNMET_DEMAND_LOWER_BOUND_TYPE =
			UnmetDemandLowerBoundType.MULTI_PERIOD;
	
	public static class Optimization {
		public final static int NUMBER_OF_TANGENTS = 7;
	}
	
	// clairvoyant replenishment policy parameters
	public final static int 
	CLAIRVOYANT_REPLENISHMENT_POLICY_FORECAST_HORIZON = 
	forecastHorizon;
	public final static double
	CLAIRVOYANT_REPLENISHMENT_POLICY_UNMET_DEMAND_COST = 48;

}
