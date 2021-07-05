package com.gly.scs.exp.policy;

import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;

public class LsiAmdIPropPolicyFactory extends PolicyFamilyLsi {
	
	public LsiAmdIPropPolicyFactory(int reportDelay) {
		super(new Allocation.Proportional(),
				new Minuend.InventoryLevel(), 
				reportDelay,
				OrderUpToLevel.Type.DEMAND);
	}
	
}
