package com.gly.scs.replen;

import com.gly.scs.domain.Facility;
import com.gly.scs.leadtime.LeadTime;

public class TimeVaryingPair <T extends Facility, U extends LeadTime> {
	public AbstractReplenishmentFunction<T, U> replenishmentFunction;
	public int nextPolicyTime;

	public TimeVaryingPair(AbstractReplenishmentFunction<T, U> replenishmentFunction, int nextPolicyTime) {
		this.replenishmentFunction = replenishmentFunction;
		this.nextPolicyTime = nextPolicyTime;
	}
}
