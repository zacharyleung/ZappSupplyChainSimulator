package com.gly.scs.domain;

import com.gly.util.NegativeArray;

public abstract class Facility {

	private String id;
	protected int inventory;

	private NegativeArray<Integer> demandArray;
	private NegativeArray<Integer> consumptionArray;
	/** Inventory level at the beginning of the period */
	private NegativeArray<Integer> inventoryBeginArray;
	/** Shipment quantity that was received in that period */
	private NegativeArray<Integer> shipmentArray;


	public Facility(String id, int startPeriod, int endPeriod) {
		this.id = id;
		inventory = 0; // initial inventory level
		Integer initial = new Integer(0);
		demandArray = new NegativeArray<Integer>(startPeriod, endPeriod, initial);
		consumptionArray = new NegativeArray<Integer>(startPeriod, endPeriod, initial);
		inventoryBeginArray = new NegativeArray<Integer>(startPeriod, endPeriod, initial);
		shipmentArray = new NegativeArray<Integer>(startPeriod, endPeriod, initial);
	}



	//-----------------------------------------------------------------------//
	// Inventory methods                                                     //
	//-----------------------------------------------------------------------//

	/**
	 * Decrements inventory by the specified quantity.
	 * @param change
	 * @throws IllegalArgumentException If quantity exceeds inventory.
	 */
	public void decrementInventory(Increment change)
			throws IllegalArgumentException {
		inventory -= change.quantity;
		// increment the consumption array
		int c = consumptionArray.get(change.period);
		c += change.quantity;
		consumptionArray.set(change.period, c);
		
		// check that the current inventory level is non-negative 
		if (inventory < 0) {
			String msg = String.format(
					"Decrement quantity = %d exceeds inventory = %d!",
					change.quantity, inventory); 
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Increase the current inventory level.
	 * @param change
	 */
	public void incrementInventory(Increment change) {
		// increment the inventory level
		inventory += change.quantity;
		
		// increment the shipments received record
		int s = shipmentArray.get(change.period);
		s += change.quantity;
		shipmentArray.set(change.period, s);
	}
	
	public void incrementDemand(Increment increment) {
		// increment the demand record
		int s = demandArray.get(increment.period);
		s += increment.quantity;
		demandArray.set(increment.period, s);
	}
	
	public void incrementConsumption(Increment increment) throws Exception {
		throw new Exception("Use decrementInventory() instead!");
	}
	

	public void logStartOfPeriodInventory(int t) {
		inventoryBeginArray.set(t, inventory);
	}

	public static class Increment {
		private final int period;
		private final int quantity;
		
		private Increment(Builder builder) {
			this.period = builder.period;
			this.quantity = builder.quantity;
		}
		
		public static class Builder {
			private int period;
			private int quantity;

			public Builder withPeriod(int period) {
				this.period = period;
				return this;
			}
			
			public Builder withQuantity(int quantity) {
				this.quantity = quantity;
				return this;
			}
			
			public Increment build() {
				return new Increment(this);
			}
		}
	}
	
	


	//-----------------------------------------------------------------------//
	// Set methods                                                           //
	//-----------------------------------------------------------------------//

	public void setDemand(int t, int demand) {
		demandArray.set(t, demand);
	}

	public void setConsumption(int t, int consumption) {
		consumptionArray.set(t, consumption);
	}

	public void demandArrives(int t, int demand) {
		int consumption = Math.min(inventory, demand);
		int unmetDemand = demand - consumption;

		inventory -= consumption;
		demandArray.set(t, demand);
		consumptionArray.set(t, consumption);
	}

	public void setInventory(int inv) {
		if (inv < 0) {
			throw new IllegalArgumentException("inventory < 0!");
		}
		this.inventory = inv;
	}




	//-----------------------------------------------------------------------//
	// Get methods                                                           //
	//-----------------------------------------------------------------------//

	public int[] getPastDemand(int currentPeriod, int historyPeriods) {
		int[] a = new int[historyPeriods];
		for (int i = 0; i < historyPeriods; ++i) {
			a[i] = demandArray.get(currentPeriod - 1 - i);
		}
		return a;
	}

	public int[] getPastConsumption(int currentPeriod, int historyPeriods) {
		int[] a = new int[historyPeriods];
		for (int i = 0; i < historyPeriods; ++i) {
			a[i] = consumptionArray.get(currentPeriod - 1 - i);
		}
		return a;
	}

	public String getId() {
		return id;
	}

	public int getShipment(int t) {
		return shipmentArray.get(t);
	}

	public int getConsumption(int t) {
		return consumptionArray.get(t);
	}

	public int getDemand(int t) {
		return demandArray.get(t);
	}

	public int getInventory(int t) {
		return inventoryBeginArray.get(t);
	}

	public int getUnmetDemand(int t) {
		return demandArray.get(t) - consumptionArray.get(t);
	}

	public int getBeginningOfPeriodInventory(int t) {
		return inventoryBeginArray.get(t);
	}

	public int getCurrentInventoryLevel() {
		return inventory;
	}

	@Override
	public String toString() {
		return toString(2);
	}

	public String toString(int indent) {
		String nl = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		String pad = "";
		for (int i = 0; i < indent; ++i) {
			pad += " ";
		}
		sb.append("Facility{" + nl);
		sb.append(pad + "id = " + id + nl);
		sb.append(pad + "inventory = " + inventory + nl);
		sb.append("}");
		return sb.toString();
	}
}
