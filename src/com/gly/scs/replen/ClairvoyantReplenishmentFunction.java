package com.gly.scs.replen;

import gurobi.*;

import java.util.*;

import com.gly.random.*;
import com.gly.scs.data.*;
import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.ShipmentLeadTimeType;
import com.gly.scs.opt.ShipmentOptimizationModel;
import com.gly.scs.opt.UnmetDemandLowerBoundType;
import com.gly.scs.opt.ShipmentOptimizationModel.Inputs;
import com.gly.scs.opt.ShipmentOptimizationModel.Options;
import com.gly.scs.sched.*;
import com.gly.util.*;

/**
 * The clairvoyant replenishment policy requires the retail facilities
 * to submit reports in every period.  Therefore, this policy is
 * stateless.
 * 
 * @author zacharyleung
 *
 */
class ClairvoyantReplenishmentFunction
extends AbstractReplenishmentFunction<NationalFacility, XdockLeadTime> {
	private final int forecastHorizon;
	private final double unmetDemandCost;

	private ClairvoyantReplenishmentFunction(Builder builder) {
		this.forecastHorizon = builder.forecastHorizon;
		this.unmetDemandCost = builder.unmetDemandCost;
	}

	public static class Builder {
		private int forecastHorizon;
		private double unmetDemandCost;

		public Builder withForecastHorizon(int forecastHorizon) {
			this.forecastHorizon = forecastHorizon;
			return this;
		}

		public Builder withUnmetDemandCost(double unmetDemandCost) {
			this.unmetDemandCost = unmetDemandCost;
			return this;
		}

		public ClairvoyantReplenishmentFunction build() {
			return new ClairvoyantReplenishmentFunction(this);
		}
	}

	@Override
	public Collection<ShipmentDecision> getShipmentDecisions(
			int currentPeriod, NationalFacility nationalFacility,
			Collection<Report> reportCollection,
			ImmutableShipmentRepository shipmentRepository,
			DemandModel demand, AbstractShipmentSchedule shipmentSchedule,
			XdockLeadTime leadTime) throws Exception {

		// plan shipments from [T1, T2]
		// for retail facility r, we have information
		// for [T0[r], T0[r] + forecastHorizon)
		// T2 is the last date for which we have information

		int t = currentPeriod;	
		int R = reportCollection.size();
		Report[] reports = reportCollection.toArray(new Report[R]);

		Inputs inputs = SomInputAdapter.getInputs(currentPeriod,
				nationalFacility, reports, shipmentRepository,
				demand, shipmentSchedule,
				leadTime, forecastHorizon);
		Options options = new Options.Builder()
		.withForecastHorizon(forecastHorizon)
		.withHoldingCostType(HoldingCostType.FACILITY_ACCESSIBILITY)
		.withUnmetDemandCost(unmetDemandCost)
		.withUdlbType(UnmetDemandLowerBoundType.ACTUAL)
		.withShipmentLeadTimeType(ShipmentLeadTimeType.ACTUAL)
		.build();

		Collection<com.gly.scs.opt.ShipmentDecision> shipmentDecisions
		= ShipmentOptimizationModel.getShipmentDecisions(inputs, options);
		return SomOutputAdapter.convert(reports, shipmentDecisions);
	}

	@Override
	public int getReportHistoryPeriods() {
		return 0;
	}

	@Override
	public int getReportForecastPeriods() {
		return forecastHorizon;
	}

	@Override
	public int getForecastLevel() {
		return 0;
	}

	@Override
	public void resetState() {
		// nothing to do
	}

	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "{" + NL);
		sb.append(PAD + "forecast horizon = " + forecastHorizon + NL);
		sb.append(PAD + "unmet demand cost = " + unmetDemandCost + NL);
		sb.append("}");
		return sb.toString();
	}
}
