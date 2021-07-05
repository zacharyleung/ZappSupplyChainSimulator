package com.gly.scs.zambia;

import java.io.File;
import java.util.*;

import com.gly.scs.demand.*;
import com.gly.scs.domain.*;
import com.gly.scs.leadtime.*;
import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTime.MinimumValue;
import com.gly.scs.replen.*;
import com.gly.scs.sched.*;
import com.gly.scs.sim.*;
import com.gly.util.MathUtils;

/**
 * Build a SupplyChain object by reading input files in a directory.
 * 
 * @author zacleung
 *
 */
public class SupplyChainFactoryFolder extends SupplyChainFactory {

	public final int shipmentCycle = 4;
	public final int forecastHorizon = 48;
	public final int nationalReplenishmentCycle = 12;
	/** 
	 * The mean total demand per timestep, i.e., the total of the mean
	 * demand per timestep summed over every retail facility. 
	 */
	public final double meanDemandPerPeriod;
	/**
	 * Minimum value for the class
	 * VehicleAccessibilitySingleFacilityLeadTime.
	 */
	public final MinimumValue minimumValue;

	private final SupplyChain.Builder supplyChainBuilder;
	private final String inputFolder;
	private final Topology topology;
	private final ReplenishmentFileParser rfp;
	private final AccessibilityParser accessParser;

	public SupplyChainFactoryFolder(
			String inputFolder, MinimumValue minimumValue)
					throws Exception {
		System.out.printf("SupplyChainFactoryFolder(%s, %s)",
				inputFolder, minimumValue);		

		this.inputFolder = inputFolder;
		this.minimumValue = minimumValue;
		File dir = new File(inputFolder);

		// create the topology object
		topology = TopologyParser.parse(
				new File(dir, "facility-names.csv"));
		//System.out.println(topology);

		SingleFacilityDemandFactory demandFactory = 
				getDemandFactory(MmfeDemandParser.DEFAULT_DEMAND_PARAMETER);
		meanDemandPerPeriod = demandFactory.getMeanDemandPerPeriod();

		accessParser = new AccessibilityParser(
				new File(dir, "facility-accessibility.csv"));

		rfp = new ReplenishmentFileParser(
				new File(dir, "replenishment.csv")); 

		// create the regional shipment schedule object
		Collection<RegionalOffsetEntry> regionalOffsetEntries =
				rfp.getRegionalOffsetEntries();
		RegionalShipmentSchedule regionalShipmentSchedule =
				new RegionalShipmentSchedule.Builder()
				.withCycle(shipmentCycle)
				.withRegionalOffsetEntries(regionalOffsetEntries)
				.build();
		//System.out.println(regionalShipmentSchedule);

		// lead time factory for primary lead time
		SingleFacilityLeadTimeFactory primarySfltFactory =
				rfp.getPrimarySfltFactory();

		// Create the retail shipment schedule object.
		// Note that the regional facility attempts to make shipments
		// to retail facilities on the first period of every month.
		AbstractShipmentSchedule retailShipmentSchedule =
				new ConstantShipmentSchedule(shipmentCycle);

		supplyChainBuilder = new SupplyChain.Builder()
				.withTopology(topology)
				.withRegionalLeadTimeFactory(primarySfltFactory)
				.withRegionalShipmentSchedule(regionalShipmentSchedule)
				.withRetailShipmentSchedule(retailShipmentSchedule);
	}

	@Override
	public SupplyChain getSupplyChain(double supplyDemandRatio) {
		SingleFacilityDemandFactory demandFactory = 
				getDemandFactory(MmfeDemandParser.DEFAULT_DEMAND_PARAMETER);

		SingleFacilityLeadTimeFactory secondarySfltFactory = 
				getSecondaryLeadTimeFactory(DEFAULT_ACCESS_PARAMETER);

		return getSupplyChain(
				supplyDemandRatio,
				demandFactory,
				secondarySfltFactory);
	}

	@Override
	public SupplyChain getSupplyChainWithDemandParameter(
			double supplyDemandRatio, double demandParameter) {
		SingleFacilityDemandFactory demandFactory = 
				getDemandFactory(demandParameter);

		SingleFacilityLeadTimeFactory secondarySfltFactory = 
				getSecondaryLeadTimeFactory(DEFAULT_ACCESS_PARAMETER);

		return getSupplyChain(
				supplyDemandRatio,
				demandFactory,
				secondarySfltFactory);
	}

	@Override
	public SupplyChain getSupplyChainWithAccessParameter(
			double supplyDemandRatio, double accessParameter) {
		SingleFacilityDemandFactory demandFactory = 
				getDemandFactory(MmfeDemandParser.DEFAULT_DEMAND_PARAMETER);

		SingleFacilityLeadTimeFactory secondarySfltFactory = 
				getSecondaryLeadTimeFactory(accessParameter);

		return getSupplyChain(
				supplyDemandRatio,
				demandFactory,
				secondarySfltFactory);
	}

	@Override
	public SupplyChain getSupplyChainWithDemandAndAccessParameter(
			double supplyDemandRatio, double parameter) {
		SingleFacilityDemandFactory demandFactory = 
				getDemandFactory(parameter);

		SingleFacilityLeadTimeFactory secondarySfltFactory = 
				getSecondaryLeadTimeFactory(parameter);

		return getSupplyChain(
				supplyDemandRatio,
				demandFactory,
				secondarySfltFactory);
	}
	
	private SupplyChain getSupplyChain(
			double supplyDemandRatio, 
			SingleFacilityDemandFactory demandFactory,
			SingleFacilityLeadTimeFactory secondarySfltFactory) {
		NationalReplenishmentSchedule nationalReplenishmentSchedule =
				getNationalReplenishmentSchedule(
						supplyDemandRatio, demandFactory);
		return supplyChainBuilder
				.withDemandFactory(demandFactory)
				.withRetailLeadTimeFactory(secondarySfltFactory)
				.withNationalReplenishmentSchedule(nationalReplenishmentSchedule)
				.build();
	}
	
	private NationalReplenishmentSchedule getNationalReplenishmentSchedule(
			double supplyDemandRatio,
			SingleFacilityDemandFactory demandFactory) {
		// Every NRC timesteps, the national facility will receive
		// supplyDemandRatio * meanDemandPerTimestep amount of inventory. 

		// Calculate the mean demand per timestep, summed across all facilities
		int quantity = (int) (supplyDemandRatio * nationalReplenishmentCycle
				* meanDemandPerPeriod);

		// national replenishment policy
		NationalReplenishmentSchedule nationalReplenishmentSchedule =
				new NationalReplenishmentSchedule.Builder()
				.withCycle(nationalReplenishmentCycle)
				.withHorizon(forecastHorizon)
				.withOffset(0)
				.withQuantity(quantity)
				.build();

		return nationalReplenishmentSchedule;
	}

	private SingleFacilityDemandFactory getDemandFactory(double demandParameter) {
		try {
			File dir = new File(inputFolder);
			File file = new File(dir, "facility-timestep-demand-mean.csv");
			SingleFacilityDemandFactory demandFactory = 
					MmfeDemandParser.parse(file, demandParameter);
			return demandFactory;
		} catch (Exception e) {
			return null;
		}
	}

	private final double DEFAULT_ACCESS_PARAMETER = 1; 
	private SingleFacilityLeadTimeFactory getSecondaryLeadTimeFactory(
			double accessParameter) {
		// lead time factory for secondary lead time
		LinkedList<VehicleAccessibilitySingleFacilityLeadTimeFactory.Entry>
		secondaryLeadTimeEntries =	new LinkedList<>();
		for (String retail : topology.getRetailIds()) {
			String regionalFacilityId = topology.getRegional(retail);
			double mean = rfp.getMeanSecondaryLeadTime(regionalFacilityId);
			double[] accessibility = accessParser.getAccessibility(retail);

			// Apply the effect of the accessibility parameter
			accessibility = MathUtils.applyConvexCombinationToOne(
					accessibility, accessParameter);
			
			//System.out.println("SupplyChainFactoryFolder");
			//System.out.println(Arrays.toString(accessibility));

			secondaryLeadTimeEntries.add(
					new VehicleAccessibilitySingleFacilityLeadTimeFactory.Entry.Builder()
					.withFacilityId(retail)
					.withMean(mean)
					.withAccessibility(accessibility)
					.build());
		}
		return new VehicleAccessibilitySingleFacilityLeadTimeFactory(
				secondaryLeadTimeEntries, minimumValue);
	}


	public String getInputFolder() {
		return inputFolder;
	}

}
