package com.gly.scs.exp.exp07;

import java.util.Collection;
import java.util.LinkedList;

import com.gly.scs.domain.Facility;
import com.gly.scs.domain.NationalFacility;
import com.gly.scs.exp.policy.AmiIpFcfsPolicyFactory;
import com.gly.scs.leadtime.OneTierLeadTime;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.opt.UnmetDemandLowerBoundFactory;
import com.gly.scs.opt.UnmetDemandLowerBoundPercentileFactory;
import com.gly.scs.replen.*;
import com.gly.scs.sim.*;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class Policies {

	public static Collection<AbstractScenario> getAllScenarios() {
		return getScenarios(true);
	}

	public static Collection<AbstractScenario> getNonOptimizationScenarios() {
		return getScenarios(false);
	}
	
	/**
	 * Get scenarios to simulate.
	 * @param withOpt if true, include optimization-based policies.
	 * @return
	 */
	private static Collection<AbstractScenario> getScenarios(boolean withOpt) {
		LinkedList<AbstractScenario> scenarios = new LinkedList<>();		
		if (withOpt) {
			scenarios.add(Policies.getOptimizationScenario(
					"opt-forecast-good-delay-0", 0,
					ZambiaSimulationParameters.FORECAST_LEVEL_INDUSTRY));
			scenarios.add(Policies.getOptimizationScenario(
					"opt-forecast-bad-delay-1", 1,
					ZambiaSimulationParameters.FORECAST_LEVEL_MYOPIC));
			scenarios.add(Policies.getClairvoyantScenario());
		}
		scenarios.add(Policies.getIstockScenario());

		int oneTierReportDelay = 0;
		int forecastLevel = 0; 
		AbstractScenario s;
		String name;
		OrderUpToLevel outl;
		
		scenarios.add(AmiIpFcfsPolicyFactory.getPolicy());
		
		scenarios.add(s);
		
		// cross-docking policy with bad forecast
		name = "xdock-forecast-bad-delay-1";
		oneTierReportDelay = 1;
		forecastLevel = ZambiaSimulationParameters.FORECAST_LEVEL_MYOPIC;
		outl = new ForecastOrderUpToLevel.Builder()
				.withForecastLevel(forecastLevel)
				.withForecastPeriods(ZambiaSimulationParameters.amiHistoryPeriods)
				.withOrderUpToPeriods(ZambiaSimulationParameters.xdockOrderUpToPeriods)
				.build();
		s = XdockScenarioFactory.getScenario(name, outl, 
				oneTierReportDelay, new Minuend.InventoryPosition(), 
				new Allocation.Proportional());
		scenarios.add(s);

		// LSI policy
		name = "xdock-lsi";
		oneTierReportDelay = 1;
		outl = new LsiOrderUpToLevel.Builder()
				.withFactor(4)
				.withHistoryTimesteps(ZambiaSimulationParameters.amiHistoryPeriods)
				.withYearTimesteps(ZambiaSimulationParameters.NUMBER_OF_PERIODS_IN_YEAR)
				.build();
		s = XdockScenarioFactory.getScenario(name, outl, 
				oneTierReportDelay, new Minuend.InventoryPosition(), 
				new Allocation.Proportional());
		scenarios.add(s);
		
		return scenarios;
	}

	public static AbstractScenario getClairvoyantScenario() {
		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new ClairvoyantReplenishmentPolicy.Builder()
				.withForecastHorizon(ZambiaSimulationParameters.
						CLAIRVOYANT_REPLENISHMENT_POLICY_FORECAST_HORIZON)
				.withUnmetDemandCost(ZambiaSimulationParameters.
						CLAIRVOYANT_REPLENISHMENT_POLICY_UNMET_DEMAND_COST)
				.build();

		return new XdockScenario.Builder()
				.withName("clair")
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}

	public static AbstractScenario getIstockScenario() {
		int oneTierReportDelay = 1;

		// regional replenishment policy
		AmiOrderUpToLevel regionalOrderUpToLevel = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(ZambiaSimulationParameters.amiHistoryPeriods)
				.withOrderUpToPeriods(ZambiaSimulationParameters.istockRegionalOrderUpToPeriods)
				.build();

		AbstractReplenishmentFunction<Facility, OneTierLeadTime> 
		regionalReplenishmentFunction =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(regionalOrderUpToLevel,
				new Minuend.InventoryPosition(), 
				new Allocation.Proportional());

		OneTierReplenishmentPolicy regionalReplenishmentPolicy =
				new OneTierReplenishmentPolicy(oneTierReportDelay,
						regionalReplenishmentFunction);

		// retail replenishment policy
		AmiOrderUpToLevel retailOrderUpToLevel = new AmiOrderUpToLevel.Builder()
				.withHistoryPeriods(ZambiaSimulationParameters.amiHistoryPeriods)
				.withOrderUpToPeriods(ZambiaSimulationParameters.istockRetailOrderUpToPeriods)
				.build();

		AbstractReplenishmentFunction<Facility, OneTierLeadTime> 
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<Facility, OneTierLeadTime>
		(retailOrderUpToLevel,
				new Minuend.InventoryPosition(), 
				new Allocation.Proportional());

		OneTierReplenishmentPolicy retailReplenishmentPolicy =
				new OneTierReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunction);

		return new IstockScenario.Builder()
				.withName("istock")
				.withRegionalReplenishmentPolicy(regionalReplenishmentPolicy)
				.withRetailReplenishmentPolicy(retailReplenishmentPolicy)
				.withRegionalInitialInventoryLevel(ZambiaSimulationParameters.regionalInitialInventoryLevel)
				.withRetailInitialInventoryLevel(ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}

	public static AbstractScenario getOptimizationScenario(String name, 
			int reportDelay, int forecastLevel) {
	}

	public static AbstractScenario getXdockForecastScenario(String name, 
			int reportDelay, int forecastLevel) {

		System.out.println("ZambiaPerformance.getXdockForecastScenario(");
		System.out.printf("  name = %s%n", name);
		System.out.printf("  reportDelay = %d%n", reportDelay);
		System.out.printf("  forecastLevel = %d%n", forecastLevel);
		System.out.println(")");

		ForecastOrderUpToLevel orderUpToLevel = 
				new ForecastOrderUpToLevel.Builder()
				.withForecastLevel(forecastLevel)
				.withForecastPeriods(ZambiaSimulationParameters.amiHistoryPeriods)
				.withOrderUpToPeriods(ZambiaSimulationParameters.xdockOrderUpToPeriods)
				.build();

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(orderUpToLevel,
				new Minuend.InventoryPosition(), 
				new Allocation.Proportional());

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(reportDelay,
						retailReplenishmentFunction);

		return new XdockScenario.Builder()
				.withName(name)
				.withReplenishmentPolicy(retailReplenishmentPolicy)
				.withRetailInitialInventoryLevel(
						ZambiaSimulationParameters.retailInitialInventoryLevel)
				.build();
	}

}
