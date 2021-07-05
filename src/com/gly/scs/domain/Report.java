package com.gly.scs.domain;

import java.util.*;

import com.gly.random.AbstractRandomVariable;
import com.gly.util.SparseArray;

public class Report {
	/** The period when the report is written and sent. */
	private final int periodSent;
	/** The inventory level at the beginning of the period. */
	private final int inventory;
	/** pastDemand[k] = demand during period periodSent - 1 - k. */
	private int[] pastDemand;
	/** pastDemand[k] = consumption during period periodSent - 1 - k. */    
	private int[] pastConsumption;

	/** futureDemand[k] = demand during period periodSent + k. */
	private int[] futureDemand;
	
	/** demandForecast.get(t) = demand forecast during period t. */
	private SparseArray<AbstractRandomVariable> demandForecast;
	
	/**
	 * futureAccessibility[k] = accessibility during period
	 * periodSent + k.
	 */
	private double[] futureAccessibility;
	
	private List<Shipment> shipments;
	
	private Facility from;

	public Report(Builder builder) {
		this.periodSent = builder.periodSent;
		this.inventory = builder.inventory;
		this.pastDemand = builder.pastDemand;
		this.pastConsumption = builder.pastConsumption;
		this.demandForecast = builder.demandForecast;
		this.futureDemand = builder.futureDemand;
		this.futureAccessibility = builder.futureAccessibility;
		this.from = builder.from;
		this.shipments = builder.shipments;
		
//		System.out.println("Report(Builder b)");
//		System.out.println(this);
	}

	public static class Builder {
		private int periodSent;
		private int inventory;
		private int[] pastDemand;
		private int[] pastConsumption;
		private Facility from;
		private List<Shipment> shipments;
		private int[] futureDemand;
		private double[] futureAccessibility;
		private SparseArray<AbstractRandomVariable> demandForecast;
	
		public Builder withPeriodCreated(int period) {
			this.periodSent = period;
			return this;
		}
	
		public Builder withInventory(int inventory) {
			this.inventory = inventory;
			return this;
		}
	
		public Builder withPastDemand(int[] pastDemand) {
			this.pastDemand = Arrays.copyOf(pastDemand, pastDemand.length);
			return this;
		}
	
		public Builder withPastConsumption(int[] pastConsumption) {
			this.pastConsumption = Arrays.copyOf(
					pastConsumption, pastConsumption.length);
			return this;
		}
		
		public Builder withFutureDemand(int[] futureDemand) {
			int[] a = futureDemand;
			this.futureDemand = Arrays.copyOf(a, a.length);
			return this;
		}
		
		public Builder withFutureAccessibility(double[] futureAccessibility) {
			double[] a = futureAccessibility;
			this.futureAccessibility = Arrays.copyOf(a, a.length);
			return this;
		}
		
		public Builder withDemandForecast(SparseArray<AbstractRandomVariable> demandForecast) {
			this.demandForecast = demandForecast;
			return this;
		}
	
		public Builder withFrom(Facility from) {
			this.from = from;
			return this;
		}
	
		public Builder withShipments(Collection<Shipment> shipments) {
			this.shipments = new ArrayList<>(shipments);
			return this;
		}
		
		public Report build() {
			return new Report(this);
		}
	}

	/**
	 * The report includes a demand forecast and accessibility information
	 * @return
	 */
	public int getEndPeriod() {
		return periodSent + futureDemand.length;
	}

	public int getPeriodSent() {
		return periodSent;
	}

	public Facility getFacility() {
		return from;
	}

	/**
	 * Get demand during the period t - 1 - i, where t is the period
	 * when the report was created.
	 */
	public int getPastDemand(int i) {
		return pastDemand[i];
	}
	
	/**
	 * Get demand during the period t - 1 - i, where t is the period
	 * when the report was created.
	 */
	public int getPastConsumption(int i) {
		return pastConsumption[i];
	}
	
	public int getPastDemandAtTimestep(int t) {
		return pastDemand[timestepToIndex(t)];
	}

	/**
	 * Get demand during the period t + i, where t is the period
	 * when the report was created.
	 */
	public int getFutureDemand(int i) {
		return futureDemand[i];
	}

	public int getInventory() {
		return inventory;
	}

	/**
	 * Get consumption during the period t - 1 - i, where t is the period
	 * when the report was created.
	 */
	public int getConsumption(int i) {
		return pastConsumption[i];
	}

	public int getConsumptionAtTimestep(int t) {
		return pastConsumption[timestepToIndex(t)];
	}
	
	public int timestepToIndex(int t) {
		return periodSent - 1 - t; 
	}

	public List<Shipment> getShipments() {
		return new LinkedList<>(shipments);
	}
	
	public AbstractRandomVariable getDemandForecast(int period) {
		return demandForecast.get(period);
	}
	
	public double[] getAccessibility() {
		return futureAccessibility;
	}
	
	public double getAccessibility(int period) {
		return futureAccessibility[period - periodSent];
	}
	
	public String getFacilityId() { return from.getId(); }
	
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
		sb.append("Report{" + nl);
		sb.append(pad + "facility = " + from.getId() + nl);
		sb.append(pad + "timestep created = " + periodSent + nl);
		sb.append(pad + "past demand timesteps = " + pastDemand.length + nl);
		sb.append(pad + "inventory = " + inventory + nl);
		sb.append(pad + "demand = " + Arrays.toString(pastDemand) + nl);
		sb.append(pad + "consumption = " + Arrays.toString(pastConsumption) + nl);
		
		//sb.append(pad + "forecast = ");
		//for (int i = 0; i < demandForecast.length; ++i) {
		//	sb.append(demandForecast[i] + " ");
		//}
		//sb.append(nl);
		
		sb.append("}");
		return sb.toString();
	}
}