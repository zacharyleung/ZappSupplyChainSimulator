package com.gly.scs.opt;

import java.util.LinkedList;

import com.gly.random.*;
import com.gly.scs.opt.*;
import com.gly.scs.opt.ShipmentOptimizationModel.Inputs;
import com.gly.scs.opt.ShipmentOptimizationModel.Options;
import com.gly.util.SparseArray;

public class ShipmentOptimizationModelTest {

	private static int forecastHorizon = 16;
	private static double shipmentLeadTimePercentile = 0.99;
	private static double unmetDemandCost = 10;
	
	public static void main(String[] args) throws Exception {
		Inputs inputs = new Inputs.Builder()
		.withRetailStates(getRetailStates())
		.withSupplierState(getSupplierState())
		.build();

		Options options = new Options.Builder()
		.withForecastHorizon(16)
		.withHoldingCostType(HoldingCostType.CONSTANT)
		.withShipmentLeadTimePercentile(shipmentLeadTimePercentile)
		.withUnmetDemandCost(unmetDemandCost)
		.build();

		ShipmentOptimizationModel.getShipmentDecisions(inputs, options);
	}

	/**
	 * Retail facility sends report in period -2.
	 * Initial inventory level is 10 units.
	 * A shipment of 30 units sent in period -3 will arrive in period 3.
	 * The supplier can send a shipment in period 4 and period 8
	 * that will arrive after a lead time of 3 periods. 
	 * @return
	 */
	private static RetailState[] getRetailStates() {
		int reportPeriod = -2;
		int T2 = reportPeriod + forecastHorizon;
		
		SparseArray<Double> accessibility = new SparseArray<>();
		for (int i = reportPeriod; i < T2; ++i) {
			accessibility.put(i, 1.0);
		}
		LinkedList<IncomingShipment> incomingShipments = new LinkedList<>();
		incomingShipments.add(new IncomingShipment.Builder()
		.withPeriodArrival(3)
		.withPeriodSent(-3)
		.withLeadTimeRandomVariable(new ConstantIntegerRandomVariable(6))
		.withQuantity(30)
		.withRealizedLeadTime(6)
		.build());

		LinkedList<ShipmentOpportunity> shipmentOpportunities
		= new LinkedList<>();
		shipmentOpportunities.add(new ShipmentOpportunity.Builder()
		.withPeriod(8)
		.withLeadTimeRandomVariable(new ConstantIntegerRandomVariable(3))
		.withRealizedLeadTime(3)
		.build());
		shipmentOpportunities.add(new ShipmentOpportunity.Builder()
		.withPeriod(4)
		.withLeadTimeRandomVariable(new ConstantIntegerRandomVariable(3))
		.withRealizedLeadTime(3)
		.build());

		SparseArray<AbstractRandomVariable> demandForecast = 
				new SparseArray<>();
		for (int u = reportPeriod; u < T2; ++u) {
			demandForecast.put(u, new NormalRandomVariable.Builder()
					.withMean(10)
					.withStandardDeviation(5)
					.build());
		}

		RetailState[] reports = new RetailState[1];
		reports[0] = new RetailState.Builder()
		.withPeriod(reportPeriod)
		.withAccessibility(accessibility)
		.withInventoryLevel(10)
		.withIncomingShipments(incomingShipments)
		.withShipmentOpportunities(shipmentOpportunities)
		.withDemandForecast(demandForecast)
		.build();

		return reports;
	}
	
	private static SupplierState getSupplierState() {
		LinkedList<IncomingShipment> incomingShipments = new LinkedList<>();
		incomingShipments.add(new IncomingShipment.Builder()
		.withPeriodArrival(4)
		.withPeriodSent(4)
		.withLeadTimeRandomVariable(new ConstantIntegerRandomVariable(0))
		.withQuantity(50)
		.withRealizedLeadTime(0)
		.build());
		incomingShipments.add(new IncomingShipment.Builder()
		.withPeriodArrival(8)
		.withPeriodSent(8)
		.withLeadTimeRandomVariable(new ConstantIntegerRandomVariable(0))
		.withQuantity(50)
		.withRealizedLeadTime(0)
		.build());

		return new SupplierState.Builder()
		.withIncomingShipments(incomingShipments)
		.withPeriod(0)
		.withInventoryLevel(20)
		.build();
	}
}
