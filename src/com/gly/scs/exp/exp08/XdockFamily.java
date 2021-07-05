package com.gly.scs.exp.exp08;

import com.gly.scs.domain.NationalFacility;
import com.gly.scs.leadtime.XdockLeadTime;
import com.gly.scs.replen.*;
import com.gly.scs.replen.OrderUpToLevel.Type;
import com.gly.scs.sim.AbstractScenario;
import com.gly.scs.sim.XdockScenario;
import com.gly.scs.zambia.ZambiaSimulationParameters;

public class XdockFamily extends ParametrizedScenarioFamily {
	private final int oneTierReportDelay = 1;
	private final Minuend minuend;
	private final Allocation allocation;
	private final Type type;
	
	public XdockFamily(String name, double[] d, 
			 Type type, Minuend minuend, Allocation allocation) {
		super(name, d);
		this.type = type;
		this.minuend = minuend;
		this.allocation = allocation;
	}

	@Override
	public AbstractScenario getScenario(double parameter) {
		double orderUpToPeriods = parameter;

		AbstractReplenishmentFunction<NationalFacility, XdockLeadTime>  
		retailReplenishmentFunction =
		new OrderUpToReplenishmentFunction<NationalFacility, XdockLeadTime>
		(type.get(orderUpToPeriods), minuend, allocation);

		XdockReplenishmentPolicy retailReplenishmentPolicy =
				new XdockReplenishmentPolicy(oneTierReportDelay,
						retailReplenishmentFunction);

		return new XdockScenario.Builder()
		.withName(name)
		.withReplenishmentPolicy(retailReplenishmentPolicy)
		.withRetailInitialInventoryLevel(
				ZambiaSimulationParameters.retailInitialInventoryLevel)
		.build();
	}

	public static abstract class Type {
		public abstract OrderUpToLevel get(double timesteps);
	}

	public static class Past extends Type {
		@Override
		public OrderUpToLevel get(double timesteps) {
			return new AmiOrderUpToLevel.Builder()
					.withHistoryPeriods(
							ZambiaSimulationParameters.amiHistoryPeriods)
					.withOrderUpToPeriods(timesteps)
					.withType(OrderUpToLevel.Type.CONSUMPTION)
					.build();
		}
	}
	
//	public static class Forecast extends Type {
//		@Override
//		public OrderUpToLevel get(double timesteps) {
//			return new ForecastOrderUpToLevel.Builder()
//					.withForecastLevel(
//							ZambiaSimulationParameters.FORECAST_LEVEL_INDUSTRY)
//					.withForecastPeriods(timesteps)							
//					.build();
//		}
//	}
	

}

