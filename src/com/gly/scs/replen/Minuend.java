package com.gly.scs.replen;

import com.gly.scs.domain.Report;
import com.gly.scs.domain.Shipment;

public abstract class Minuend {
	public abstract int getMinuend(Report report);
	
	public static class InventoryPosition extends Minuend {
		@Override
		public int getMinuend(Report report) {
			int ip = report.getInventory();
			for (Shipment shipment : report.getShipments()) {
				ip += shipment.quantity;
			}
			return ip;
		}
	}

	public static class InventoryLevel extends Minuend {
		@Override
		public int getMinuend(Report report) {
			return report.getInventory();
		}
	}
	
	public static class Zero extends Minuend {
		@Override
		public int getMinuend(Report report) {
			return 0;
		}
	}
}

