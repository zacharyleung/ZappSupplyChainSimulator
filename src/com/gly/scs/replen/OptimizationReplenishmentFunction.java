package com.gly.scs.replen;

import java.util.*;

import com.gly.random.*;
import com.gly.scs.data.*;
import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.opt.ShipmentLeadTimeType;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.HoldingCostType;
import com.gly.scs.opt.ShipmentOptimizationModel;
import com.gly.scs.opt.ShipmentOptimizationModel.Inputs;
import com.gly.scs.opt.ShipmentOptimizationModel.Options;
import com.gly.scs.opt.ShipmentOptimizationModel.Options.Builder;
import com.gly.scs.opt.UnmetDemandLowerBoundType;
import com.gly.scs.sched.*;
import com.gly.util.*;

public class OptimizationReplenishmentFunction
extends AbstractReplenishmentFunction<NationalFacility, XdockLeadTime> {

	private final int forecastHorizon;
	private final double unmetDemandCost;
	private HashMap<String, Report> latestReports;
	private final boolean shouldPrint;
	private final double shipmentLeadTimePercentile;
	private final UnmetDemandLowerBoundFactory udlbFactory;
	private final int forecastLevel;
	private final HoldingCostType holdingCostType;
	private final ShipmentLeadTimeType sltType;
	private final UnmetDemandLowerBoundType udlbType;
	
	public OptimizationReplenishmentFunction(Builder builder)
			throws IllegalArgumentException {
		this.forecastHorizon = builder.forecastHorizon;
		this.forecastLevel = builder.forecastLevel;
		this.unmetDemandCost = builder.unmetDemandCost;
		this.shipmentLeadTimePercentile = builder.shipmentLeadTimePercentile;
		this.shouldPrint = builder.shouldPrint;
		this.udlbFactory = builder.udlbFactory;
		this.holdingCostType = builder.holdingCostType;
		this.sltType = builder.sltType;
		this.udlbType = builder.udlbType;

		if (holdingCostType == null) {
			throw new IllegalArgumentException("holdingCostType not defined!");
		}
		if (sltType == null) {
			throw new IllegalArgumentException("sltType not defined!");
		}
	}

	public static class Builder {
		private int forecastHorizon;
		private int forecastLevel;
		private double unmetDemandCost;
		private double shipmentLeadTimePercentile = -1;
		private boolean shouldPrint = false;
		private UnmetDemandLowerBoundFactory udlbFactory;
		private HoldingCostType holdingCostType;
		private ShipmentLeadTimeType sltType;
		private UnmetDemandLowerBoundType udlbType;
		
		public Builder withShouldPrint(boolean shouldPrint) {
			this.shouldPrint = shouldPrint;
			return this;
		}

		public Builder withHoldingCostType(HoldingCostType holdingCostType) {
			this.holdingCostType = holdingCostType;
			return this;
		}
		
		public Builder withForecastHorizon(int forecastHorizon) {
			this.forecastHorizon = forecastHorizon;
			return this;
		}

		public Builder withForecastLevel(int forecastLevel) {
			this.forecastLevel = forecastLevel;
			return this;
		}

		public Builder withUnmetDemandCost(double unmetDemandCost) {
			this.unmetDemandCost = unmetDemandCost;
			return this;
		}

		public Builder withShipmentLeadTimePercentile(double shipmentLeadTimePercentile) {
			this.shipmentLeadTimePercentile = shipmentLeadTimePercentile;
			return this;
		}

		public Builder withShipmentLeadTimeType(ShipmentLeadTimeType sltType) {
			this.sltType = sltType;
			return this;
		}
		
		public Builder withUnmetDemandLowerBoundType(UnmetDemandLowerBoundType udlbType) {
			this.udlbType = udlbType;
			return this;
		}
		
		public Builder withTangentFactory(UnmetDemandLowerBoundFactory udlbFactory) {
			this.udlbFactory = udlbFactory;
			return this;
		}

		public OptimizationReplenishmentFunction build() {
			return new OptimizationReplenishmentFunction(this);
		}
	}

	@Override
	public int getReportHistoryPeriods() {
		return 0;
	}

	/**
	 * Update the latestReports field
	 * @param reports
	 * @return true if at least one new report has been received
	 */
	private boolean updateLatestReports(Collection<Report> newReports) {
		//System.out.println("OptimizationReplenishmentPolicy.updateLatestReports()");
		boolean hasNewReports = false;
		for (Report report : newReports) {
			//System.out.println(report);
			latestReports.remove(report.getFacility().getId());
			latestReports.put(report.getFacility().getId(), report);
			hasNewReports = true;
		}
		return hasNewReports;
	}

	private Collection<Report> getLatestReports() {
		return latestReports.values();
	}

	@Override
	public Collection<ShipmentDecision> getShipmentDecisions(int currentPeriod,
			NationalFacility nationalFacility, Collection<Report> reportCollection,
			ImmutableShipmentRepository shipmentRepository, DemandModel demand,
			AbstractShipmentSchedule shipmentSchedule, XdockLeadTime leadTime)
					throws Exception {

		// update the latest reports with the currently received reports
		boolean hasNewReport = updateLatestReports(reportCollection);
		// if no new report has been received, then no shipments will be made
		if (!hasNewReport) {
			return new LinkedList<ShipmentDecision>();
		}
		// otherwise, continue 

		// convert the reports into an array
		Collection<Report> newReportCollection = getLatestReports();
		int R = newReportCollection.size();
		Report[] reports = newReportCollection.toArray(new Report[R]);

		Inputs inputs = SomInputAdapter.getInputs(currentPeriod,
				nationalFacility, reports, shipmentRepository,
				demand, forecastLevel, shipmentSchedule,
				leadTime, forecastHorizon);
		Options options = new Options.Builder()
		.withForecastHorizon(forecastHorizon)
		.withHoldingCostType(holdingCostType)
		.withShipmentLeadTimePercentile(shipmentLeadTimePercentile)
		.withUnmetDemandCost(unmetDemandCost)
		.withUdlbFactory(udlbFactory)
		.withUdlbType(udlbType)
		.withShipmentLeadTimeType(sltType)
		.build();

		Collection<com.gly.scs.opt.ShipmentDecision> shipmentDecisions
		= ShipmentOptimizationModel.getShipmentDecisions(inputs, options);
		return SomOutputAdapter.convert(reports, shipmentDecisions);
	}

	@Override
	public int getReportForecastPeriods() {
		return forecastHorizon;
	}

	@Override
	public void resetState() {
		latestReports = new HashMap<>();
	}

	@Override
	public String prettyToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "{" + NL);
		sb.append(PAD + "forecast horizon = " + forecastHorizon + NL);
		sb.append(PAD + "unmet demand cost = " + unmetDemandCost + NL);
		sb.append(PAD + "forecast level = " + forecastLevel + NL);
		sb.append(PAD + "shipment lead time percentile = " + shipmentLeadTimePercentile + NL);
		sb.append("}");
		return sb.toString();
	}

}
