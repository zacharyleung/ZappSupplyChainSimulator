package com.gly.scs.demand;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.random.AbstractRandomVariable;
import com.gly.random.LognormalRandomVariable;
import com.gly.util.MathUtils;

public class MmfeLevelsSingleFacilityDemand extends CyclicSingleFacilityDemand {

	/** standardDeviation[m] = standard deviation of epsilon_{t,u}. */
	private double[] standardDeviation;
	/**
	 * varianceProportion[k] = 
	 *     variance(epsilon_{t,u}^k) / variance(epsilon_{t,u}^k)
	 * In words, variance(epsilon_{t,u}^k) is responsible for this proportion
	 * of the variance of epsilon_{t,u}^k.
	 * Constraint: sum_{k=0}^{K-1} forecastLevel[k] = 1.
	 */
	private double[] varianceProportion;
	
	/** Number of periods of memory. */
	private final int M;
	/** Number of forecast levels. */
	private final int K;
	/** sum_{m=0}^{M-1} standardDeviation[m]^2 */
	private final double totalVariance;

	/**
	 * Map (t,u,k) to epsilon_{t,u}^k.
	 */
	private HashMap<Key, Double> map = new HashMap<>();
	

	private MmfeLevelsSingleFacilityDemand(Builder builder) {
		super(builder.demandMean);

		int startPeriod = builder.startPeriod;
		int endPeriod = builder.endPeriod;
		RandomDataGenerator random = builder.random;
		this.standardDeviation = builder.standardDeviation;
		this.varianceProportion = builder.proportion;
		
		M = standardDeviation.length;
		K = varianceProportion.length;
		
		double sum = 0;
		for (int m = 0; m < M; ++m) {
			sum += Math.pow(standardDeviation[m], 2);
		}
		totalVariance = sum;
		
		for (int t = startPeriod; t < endPeriod; ++t) {
			for (int s = t - M + 1; s <= t; ++s) {
				for (int k = 0; k < K; ++k) {
					double sigma = getStandardDeviation(t - s, k);
					double mu = - sigma * sigma / 2;
					double r = random.nextGaussian(mu, sigma);
					Key key = new Key(s, t, k);
					//System.out.printf("m = %d, k = %d, mu = %.2f, sigma = %.2f\n",
					//		t - s, k, mu, sigma);
					//System.out.printf("Putting key = %s, value = %.2f\n", key, r);
					map.put(key, r);
				}
			}
		}
		
		// debug
//		System.out.println("MmfeLevelsSingleFacilityDemand.constructor()");
//		sum = 0;
//		for (int m = 0; m < M; ++m) {
//			for (int k = 0; k < K; ++k) {
//				double sd = getStandardDeviation(m, k);
//				System.out.printf("% 8.2f", sd);
//				sum += sd * sd;
//			}
//			System.out.println();
//		}
//		System.out.printf("Total variance = %.3f%n", sum);
	}
	
	public static class Builder {
		private int startPeriod;
		private int endPeriod;
		private RandomDataGenerator random;
		private double[] proportion;
		private double[] demandMean;
		private double[] standardDeviation;

		Builder withStartPeriod(int startPeriod) {
			this.startPeriod = startPeriod;
			return this;
		}

		Builder withEndPeriod(int endPeriod) {
			this.endPeriod = endPeriod;
			return this;
		}

		Builder withRandomDataGenerator(RandomDataGenerator random) {
			this.random = random;
			return this;
		}

		Builder withForecastVarianceProportion(double[] proportion) {
			this.proportion = Arrays.copyOf(proportion, proportion.length);
			return this;
		}
		
		Builder withDemandMean(double[] mean) {
			this.demandMean = Arrays.copyOf(mean, mean.length);
			return this;
		}
		
		Builder withStandardDeviation(double[] std) {
			this.standardDeviation = Arrays.copyOf(std, std.length);
			return this;
		}
		
		MmfeLevelsSingleFacilityDemand build() {
			return new MmfeLevelsSingleFacilityDemand(this);
		}
	}
	
	@Override
	int getDemand(int t) {
		double sum = 0;
		for (int s = t - M + 1; s <= t; ++s) {
			for (int k = 0; k < K; ++k) {
				Key key = new Key(s, t, k);
				//System.out.println("Getting key = " + key);
				sum += map.get(key);
			}
		}
		return (int) Math.round(getMeanDemand(t) * Math.exp(sum));
	}

	private double getStandardDeviation(int m, int k) {
		double variance = Math.pow(standardDeviation[m], 2);
		return Math.sqrt(variance * varianceProportion[k]);
	}
	

	
	private static class Key {
		private final int t;
		private final int u;
		private final int k;
		
		private Key(int t, int u, int k) {
			this.t = t;
			this.u = u;
			this.k = k;
		}
		
		@Override
		public String toString() {
			return String.format("Key{t = %d, u = %d, k = %d}", t, u, k);
		}
		
		@Override
		public int hashCode() {
			int x = t;
			x = 397 * x + u;
			x = 397 * x + k;
			return x;
		}
		
		@Override
		public boolean equals(Object other)
		{
		   if (other == null) {
		      return false;
		   }

		   if (this.getClass() != other.getClass()) {
		      return false;
		   }

		   return (this.t == ((Key)other).t) &
				   (this.u == ((Key)other).u) &
				   (this.k == ((Key)other).k);
		}
	}

	/**
	 * The forecast level L is a number in the set {0, ..., M}.  At the
	 * end of period t, the random variables (epsilon_{t,k}^k)_{k=0}^{L-1}
	 * are realized while the random variables (epsilon_{t,k}^k)_{k=L}^{M-1}
	 * are not.
	 */
	@Override
	AbstractRandomVariable getDemandForecast(GetDemandForecastParameters params) {
		int t = params.currentTimestep;
		int u = params.futureTimestep;
		int forecastLevel = params.forecastLevel;
		
		// check that the forecast level is valid
		if (forecastLevel < 0 || forecastLevel > M) {
			throw new IllegalArgumentException(
					"Invalid forecast level = " + forecastLevel);
		}		
		
		double sum = 0;
		double unexplainedVariance = totalVariance;
		for (int s = u - M + 1; s < t; ++s) {
			for (int k = 0; k < forecastLevel; ++k) {
				Key key = new Key(s, t, k);
				//System.out.println("Getting key = " + key);
				sum += map.get(key);
				unexplainedVariance -= 
						Math.pow(getStandardDeviation(t - s, k), 2);
			}
		}
		double mean = getMeanDemand(u) * Math.exp(sum);

		// demand forecast = mean * ln N(mu,sigma^2)
		double sigma = Math.sqrt(unexplainedVariance);
		double mu = -sigma * sigma / 2;
		// variance of lognormal random variable ln N(mu,sigma^2)
		// = $[\exp(\sigma^2) - 1] \exp(2\mu + \sigma^2)$
		double std = Math.sqrt(
				(Math.exp(sigma*sigma) - 1) * Math.exp(2 * mu + sigma*sigma));
		
		return new LognormalRandomVariable.Builder()
		.withMean(mean)
		.withStandardDeviation(mean * std)
		.build();
	}


}
