package com.gly.scs.sched;

import com.gly.scs.domain.Topology;

public class XdockShipmentSchedule extends AbstractShipmentSchedule {
	
	private final Topology topology;
	private final RegionalShipmentSchedule regionalShipmentSchedule;
	
	private XdockShipmentSchedule(Builder builder) {
		this.regionalShipmentSchedule = builder.regionalShipmentSchedule;
		this.topology = builder.retailToRegional;
	}
	
	@Override
	public boolean isShipmentPeriod(String retailFacilityId, int period) {
		String regionalFacilityId = 
				topology.getRegional(retailFacilityId);
		if (regionalFacilityId == null) {
			throw new IllegalArgumentException(
					String.format("retailFacilityId = %s not recognized!",
							retailFacilityId));
		}
		return regionalShipmentSchedule.isShipmentPeriod(
				regionalFacilityId, period);
	}
	
	public static class Builder {
		private Topology retailToRegional;
		private RegionalShipmentSchedule regionalShipmentSchedule;
		
		public Builder withRegionalShipmentSchedule(
				RegionalShipmentSchedule regionalShipmentSchedule) {
			this.regionalShipmentSchedule = regionalShipmentSchedule;
			return this;
		}	
		
		public Builder withTopology(
				Topology retailToRegional) {
			this.retailToRegional = retailToRegional;
			return this;
		}
		
		public XdockShipmentSchedule build() {
			return new XdockShipmentSchedule(this);
		}
	}
	
}
