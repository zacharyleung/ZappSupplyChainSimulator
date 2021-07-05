package com.gly.scs.exp.policy;

import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel;

public class LsiAmdIFcfsPolicyFactory extends PolicyFamilyLsi {
	
	public LsiAmdIFcfsPolicyFactory(int reportDelay) {
		super(new Allocation.RandomFcfs(),
				new Minuend.InventoryLevel(), 
				reportDelay,
				OrderUpToLevel.Type.DEMAND);
	}
	
}
