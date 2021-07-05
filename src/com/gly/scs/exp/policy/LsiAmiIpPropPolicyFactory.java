package com.gly.scs.exp.policy;

import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;

public class LsiAmiIpPropPolicyFactory extends PolicyFamilyLsi {
	public LsiAmiIpPropPolicyFactory(int reportDelay) {
		super(new Allocation.Proportional(),
				new Minuend.InventoryPosition(), 
				reportDelay,
				OrderUpToLevel.Type.CONSUMPTION);
	}	
}
