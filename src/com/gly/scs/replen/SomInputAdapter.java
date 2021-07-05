package com.gly.scs.replen;

import java.util.Collection;
import java.util.LinkedList;

import com.gly.random.*;
import com.gly.scs.data.ImmutableShipmentRepository;
import com.gly.scs.demand.DemandModel;
import com.gly.scs.demand.GetDemandForecastParameters;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.opt.ShipmentOptimizationModel.Inputs;
import com.gly.scs.opt.*;
import com.gly.scs.sched.AbstractShipmentSchedule;
import com.gly.util.SparseArray;

public abstract class SomInputAdapter {
	public static Inputs getInputs(int currentPeriod,
			NationalFacility nationalFacility,
			Report[] reports,
			ImmutableShipmentRepository shipmentRepository,
			DemandModel demand,
			int forecastLevel,
			AbstractShipmentSchedule shipmentSchedule,
			XdockLeadTime leadTime,
			int forecastHorizon) {

		LinkedList<IncomingShipment> supplierIncomingShipments
		= new LinkedList<>();
		for (Shipment shipment: 
			shipmentRepository.getPipeline(nationalFacility)) {
			int realizedLeadTime = 
					shipment.periodReceived - shipment.periodSent;
			supplierIncomingShipments.add(new IncomingShipment.Builder()
			.withPeriodSent(shipment.periodSent)
			.withPeriodArrival(shipment.periodReceived)
			.withQuantity(shipment.quantity)
			.withRealizedLeadTime(realizedLeadTime)
			.withLeadTimeRandomVariable(
					new ConstantIntegerRandomVariable(realizedLeadTime))
					.build());
		}

		SupplierState supplierState = new SupplierState.Builder()
		.withPeriod(currentPeriod)
		.withInventoryLevel(nationalFacility.getCurrentInventoryLevel())
		.withIncomingShipments(supplierIncomingShipments)
		.build();

		LinkedList<RetailState> retailStates = new LinkedList<>();
		for (Report report : reports) {
			Facility facility = report.getFacility();
			String facilityId = report.getFacilityId(); 
			int T0 = report.getPeriodSent();
			int T1 = T0 + forecastHorizon;

			SparseArray<Double> accessibility = new SparseArray<>();
			for (int t = T0; t < T1; ++t) {
				accessibility.put(t, leadTime.getAccessibility(facilityId, t));
			}

			SparseArray<AbstractRandomVariable> demandForecast
			= new SparseArray<>();
			SparseArray<Integer> realizedDemand = new SparseArray<>();
			for (int t = T0; t < T1; ++t) {
				GetDemandForecastParameters p = 
						new GetDemandForecastParameters.Builder()
						.withCurrentTimestep(T0)
						.withForecastLevel(forecastLevel)
						.withFutureTimestep(t)
						.withRetailFacility(facilityId)
						.build();
				demandForecast.put(t, demand.getDemandForecast(p));
				realizedDemand.put(t, demand.getDemand(facilityId, t));
			}

			Collection<IncomingShipment> incomingShipments
			= convertIncomingShipments(shipmentRepository.getPipeline(facility),
					leadTime);

			LinkedList<ShipmentOpportunity> shipmentOpportunities
			= new LinkedList<>();
			for (int t = T0; t < T1; ++t) {
				if (shipmentSchedule.isShipmentPeriod(facilityId, t)) {
					IntegerRandomVariable leadTimeRandomVariable
					= leadTime.getLeadTimeRandomVariable(facilityId, t);
					if (leadTime.hasLeadTime(facilityId, t)) {
						int realizedLeadTime = leadTime.getLeadTime(facilityId, t);
						shipmentOpportunities.add(new ShipmentOpportunity.Builder()
						.withLeadTimeRandomVariable(leadTimeRandomVariable)
						.withPeriod(t)
						.withRealizedLeadTime(realizedLeadTime)
						.build());
					}
				}
			}

			retailStates.addLast(new RetailState.Builder()
			.withAccessibility(accessibility)
			.withDemandForecast(demandForecast)
			.withRealizedDemand(realizedDemand)
			.withIncomingShipments(incomingShipments)
			.withInventoryLevel(report.getInventory())
			.withPeriod(T0)
			.withShipmentOpportunities(shipmentOpportunities)
			.build());
		}

		return new Inputs.Builder()
		.withSupplierState(supplierState)
		.withRetailStates(retailStates.toArray(new RetailState[reports.length]))
		.build();
	}

	private static Collection<IncomingShipment> convertIncomingShipments(
			Collection<Shipment> shipments,
			XdockLeadTime leadTime) {
		LinkedList<IncomingShipment> incomingShipments
		= new LinkedList<>();
		for (Shipment shipment : shipments) {
			String facilityId = shipment.to.getId();
			int periodSent = shipment.periodSent;
			IntegerRandomVariable leadTimeRandomVariable
			= leadTime.getLeadTimeRandomVariable(facilityId, periodSent);
			int realizedLeadTime = 
					shipment.periodReceived - shipment.periodSent;

			incomingShipments.add(new IncomingShipment.Builder()
			.withPeriodSent(shipment.periodSent)
			.withPeriodArrival(shipment.periodReceived)
			.withQuantity(shipment.quantity)
			.withRealizedLeadTime(realizedLeadTime)
			.withLeadTimeRandomVariable(leadTimeRandomVariable)
			.build());
		}
		return incomingShipments;
	}
}
