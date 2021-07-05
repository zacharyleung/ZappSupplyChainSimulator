package com.gly.scs.sched;

public class ConstantShipmentSchedule extends AbstractShipmentSchedule {

	private int cycle;
	
	public ConstantShipmentSchedule(int cycle) {
		this.cycle = cycle;
	}
	
	@Override
	public boolean isShipmentPeriod(String facilityId, int t) {
		return t % cycle == 0;
	}

}
