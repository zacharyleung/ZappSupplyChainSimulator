package com.gly.scs.exp.param;

import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Allocation.Proportional;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.Minuend.InventoryLevel;
import com.gly.scs.replen.OrderUpToLevel.Type;

public final class LsiParameters {
	public static final String NAME = "lsi";
	public static final int REPORT_DELAY = 0;
	public static final Type TYPE = Type.DEMAND;
	public static final int HISTORY_TIMESTEPS = 12;
	public static final int ORDER_UP_TO_TIMESTEPS = 16;
	public static final Minuend MINUEND = 
			new Minuend.InventoryLevel();
	public static final Allocation ALLOCATION = 
			new Allocation.Proportional();
}
