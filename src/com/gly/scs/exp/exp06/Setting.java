package com.gly.scs.exp.exp06;

import com.gly.scs.replen.Minuend;
import com.gly.scs.replen.OrderUpToLevel.Type;
import com.gly.scs.sim.AbstractScenario;
import com.gly.util.MathUtils;

public abstract class Setting {
	public final int oneTierReportDelay; 
	public final int packSize;
	public final String outputDirectory;
	public final Minuend minuend;
	public final Type type;

	/** Scale */
	public final double initialOutFactor = 1.0;
	public final Type initialType;
	public final Minuend initialMinuend;
	public final int initialShipmentTimestep;
	/**
	 * A multiplicative scaling factor for the order-up-to level of
	 * the initial shipment.
	 */
	protected final double[] initialScale;
	
	public String getOutputDirectory() {
		return String.format("%s/%s-%02d", 
				outputDirectory, getName(), packSize);
	}
	
	protected Setting(Builder b) {
		this.oneTierReportDelay = b.oneTierReportDelay;
		this.packSize = b.packSize;
		this.outputDirectory = b.outputDirectory;
		this.minuend = b.minuend;
		this.type = b.type;
		this.initialType = b.initialType;
		this.initialShipmentTimestep = b.initialShipmentTimestep;
		this.initialMinuend = b.initialMinuend;
		this.initialScale = b.initialScale;
		
		// Check that the scale is not null and has at least one element
		if (initialScale == null) {
			throw new IllegalArgumentException("initial scale is null");
		}
		if (initialScale.length == 0) {
			throw new IllegalArgumentException("initial scale has length 0");
		}
	}
	
	public static class Builder {
		private int oneTierReportDelay; 
		private int packSize;
		private int initialShipmentTimestep;
		private String outputDirectory;
		private Minuend minuend;
		private Type type;
		private Type initialType;
		private Minuend initialMinuend;
		private double[] initialScale;
		
		public Builder withInitialType(Type t) {
			this.initialType = t;
			return this;
		}
		
		public Builder withOneTierReportDelay(int i) {
			this.oneTierReportDelay = i;
			return this;
		}

		public Builder withPackSize(int i) {
			this.packSize = i;
			return this;
		}

		public Builder withInitialShipmentTimestep(int i) {
			this.initialShipmentTimestep = i;
			return this;
		}
		
		public Builder withMinuend(Minuend m) {
			this.minuend = m;
			return this;
		}
		
		public Builder withInitialMinuend(Minuend m) {
			this.initialMinuend = m;
			return this;
		}
		
		public Builder withType(Type t) {
			this.type = t;
			return this;
		}
		
		public Builder withOutputDirectory(String s) {
			this.outputDirectory = s;
			return this;
		}
		
		public Builder withInitialScale(double[] s) {
			this.initialScale = MathUtils.copyOf(s);
			return this;
		}
	}
	
	public abstract String getName();
	
	public abstract AbstractScenario getScenario(long randomSeed);
}
