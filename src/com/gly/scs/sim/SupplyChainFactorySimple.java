package com.gly.scs.sim;

import java.util.LinkedList;

import com.gly.scs.demand.SingleFacilityDemandFactory;
import com.gly.scs.demand.UniformIntegerSingleFacilityDemandFactory;
import com.gly.scs.domain.Topology;
import com.gly.scs.leadtime.ConstantSingleFacilityLeadTimeFactory;
import com.gly.scs.leadtime.SingleFacilityLeadTimeFactory;
import com.gly.scs.replen.NationalReplenishmentSchedule;
import com.gly.scs.sched.ConstantShipmentSchedule;
import com.gly.scs.sched.RegionalOffsetEntry;
import com.gly.scs.sched.RegionalShipmentSchedule;

/**
 * Create a simple supply chain with a single retail facility "retail",
 * and a single regional facility "regional".
 * There are 50 timesteps in a year.
 * 
 * 
 * @author znhleung
 *
 */
public class SupplyChainFactorySimple extends SupplyChainFactory {

	private final int TIMESTEPS_PER_YEAR = 50;
	/** Send a shipment every 5 timesteps. */
	private final int SHIPMENT_CYCLE = 5;

	public SupplyChainFactorySimple() {

	}

	@Override
	public SupplyChain getSupplyChain(double supplyDemandRatio) {
		SingleFacilityDemandFactory demandFactory = getDemandFactory(5);

		return new SupplyChain.Builder()
		.withTopology(getTopology())
		.withDemandFactory(demandFactory)
		.withRegionalLeadTimeFactory(getRegionalLeadTimeFactory())
		.withRetailLeadTimeFactory(getRetailLeadTimeFactory())
		.withRegionalShipmentSchedule(
				getRegionalShipmentSchedule())
		.withRetailShipmentSchedule(
				new ConstantShipmentSchedule(SHIPMENT_CYCLE))
		.withNationalReplenishmentSchedule(
				getNationalReplenishmentSchedule(demandFactory, supplyDemandRatio))
		.build();
	}

	private Topology getTopology() {
		LinkedList<Topology.Entry> l = new LinkedList<>();
		l.add(new Topology.Entry.Builder()
				.withRetailFacilityId("retail")
				.withRegionalFacilityId("regional")
				.build());
		return new Topology(l);
	}

	private SingleFacilityDemandFactory getDemandFactory(int mean) {
		return new UniformIntegerSingleFacilityDemandFactory(2 * mean);
	}

	private NationalReplenishmentSchedule getNationalReplenishmentSchedule(
			SingleFacilityDemandFactory sfdf, double supplyDemandRatio) {
		// the cycle is the number of timesteps between a replenishment
		int cycle = TIMESTEPS_PER_YEAR; 
		int quantity = (int) (supplyDemandRatio * 
				cycle * 
				sfdf.getMeanDemandPerPeriod());

		return new NationalReplenishmentSchedule.Builder()
				.withCycle(cycle)
				.withHorizon(TIMESTEPS_PER_YEAR)
				.withOffset(0)
				.withQuantity(quantity)
				.build();
	}

	private SingleFacilityLeadTimeFactory getRetailLeadTimeFactory() {
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> list =
				new LinkedList<>();
		list.add(new ConstantSingleFacilityLeadTimeFactory.Entry("retail", 1));
		return new ConstantSingleFacilityLeadTimeFactory(list);
	}

	private SingleFacilityLeadTimeFactory getRegionalLeadTimeFactory() {
		LinkedList<ConstantSingleFacilityLeadTimeFactory.Entry> list =
				new LinkedList<>();
		list.add(new ConstantSingleFacilityLeadTimeFactory.Entry("regional", 1));
		return new ConstantSingleFacilityLeadTimeFactory(list);
	}

	private RegionalShipmentSchedule getRegionalShipmentSchedule() {
		LinkedList<RegionalOffsetEntry> list = new LinkedList<>();
		list.add(new RegionalOffsetEntry.Builder()
						.withOffset(0)
						.withRegionalFacilityId("regional")
						.build());
		return new RegionalShipmentSchedule.Builder()
		.withCycle(SHIPMENT_CYCLE)
		.withRegionalOffsetEntries(list)
		.build();
	}

}
