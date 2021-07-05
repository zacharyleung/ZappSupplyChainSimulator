package com.gly.scs.sim;

public class SimulationParameters {
	/**
	 * Number of periods of random data (demands and lead times) to
	 * generate before the start of the simulation.
	 */
	private final int beforePeriods;
	/**
	 * Number of periods of random data (demands and lead times) to
	 * generate after the start of the simulation.
	 */
	private final int afterPeriods;
	/** Start period for the simulation. */
	private final int startPeriod;
	/** Number of periods to simulate as warm-up periods. */
	private final int warmUpPeriods;
	/** Number of periods to simulate for data collection. */
	private final int simulationPeriods;
	private final long randomSeed;
	private final boolean shouldPrint;

	private SimulationParameters(Builder builder) {
		this.startPeriod = builder.startPeriod;
		this.simulationPeriods = builder.simulationPeriods;
		this.beforePeriods = builder.beforePeriods;
		this.afterPeriods = builder.afterPeriods;
		this.warmUpPeriods = builder.warmUpPeriods;
		this.randomSeed = builder.randomSeed;
		this.shouldPrint = builder.shouldPrint;

		if (beforePeriods < 0) {
			throw new IllegalArgumentException(
					String.format("beforePeriods = %d < 0!", beforePeriods));
		}
		if (afterPeriods < 0) {
			throw new IllegalArgumentException(
					String.format("afterPeriods = %d < 0!", afterPeriods));
		}

	}

	public static class Builder {
		private boolean shouldPrint = false;
		private int beforePeriods = 0;
		private int afterPeriods = 0;
		private int warmUpPeriods = 0;
		private int startPeriod;
		private int simulationPeriods;
		private long randomSeed;

		public Builder withStartPeriod(int startPeriod) {
			this.startPeriod = startPeriod;
			return this;
		}

		public Builder withSimulationPeriods(int simulationPeriods) {
			this.simulationPeriods = simulationPeriods;
			return this;
		}

		public Builder withBeforePeriods(int beforePeriods) {
			this.beforePeriods = beforePeriods;
			return this;
		}

		public Builder withAfterPeriods(int afterPeriods) {
			this.afterPeriods = afterPeriods;
			return this;
		}

		public Builder withWarmUpPeriods(int warmUpPeriods) {
			this.warmUpPeriods = warmUpPeriods;
			return this;
		}

		public Builder withRandomSeed(long randomSeed) {
			this.randomSeed = randomSeed;
			return this;
		}

		public Builder withShouldPrint(boolean shouldPrint) {
			this.shouldPrint = shouldPrint;
			return this;
		}


		public SimulationParameters build() {
			return new SimulationParameters(this);
		}
	}

	public int getSimulationStartPeriod() {
		return startPeriod;
	}

	public int getSimulationEndPeriod() {
		return startPeriod + warmUpPeriods + simulationPeriods;
	}

	/**
	 * Return the first period for which to create random data.
	 * @return
	 */
	public int getRandomStartPeriod() {
		return startPeriod - beforePeriods;
	}

	/**
	 * Return the last period for which to create random data.
	 * @return
	 */
	public int getRandomEndPeriod() {
		return getSimulationEndPeriod() + afterPeriods;
	}
	
	public long getRandomSeed() {
		return randomSeed;
	}
	
	public int getDataCollectionStartPeriod() {
		return startPeriod + warmUpPeriods;
	}
	
	public int getDataCollectionEndPeriod() {
		return getSimulationEndPeriod();
	}

}