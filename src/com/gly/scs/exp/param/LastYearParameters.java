package com.gly.scs.exp.param;

import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Allocation.Proportional;
import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.Minuend.InventoryPosition;
import com.gly.scs.replen.OrderUpToLevel.Type;

public final class LastYearParameters {

	public static final String NAME = "last-year";
	public static final int REPORT_DELAY = 0;
	public static final Type TYPE = Type.DEMAND;
	public static final int HISTORY_TIMESTEPS = 12;
	public static final int ORDER_UP_TO_TIMESTEPS = 16;
	public static final Minuend MINUEND = 
	new Minuend.InventoryPosition();
	public static final Allocation ALLOCATION = 
	new Allocation.Proportional();

}
