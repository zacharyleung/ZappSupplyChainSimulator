package com.gly.scs.exp.policy;

import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;

public class LsiAmdIpFcfsPolicyFactory extends PolicyFamilyLsi {
	
	public LsiAmdIpFcfsPolicyFactory(int reportDelay) {
		super(new Allocation.RandomFcfs(),
				new Minuend.InventoryPosition(), 
				reportDelay,
				OrderUpToLevel.Type.DEMAND);
	}
	
}
