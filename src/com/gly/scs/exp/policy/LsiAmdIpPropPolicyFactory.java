package com.gly.scs.exp.policy;

import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;

public class LsiAmdIpPropPolicyFactory extends PolicyFamilyLsi {
	public LsiAmdIpPropPolicyFactory(int reportDelay) {
		super(new Allocation.Proportional(),
				new Minuend.InventoryPosition(), 
				reportDelay,
				OrderUpToLevel.Type.DEMAND);
	}	
}
