package com.gly.scs.demand;

import static org.junit.Assert.*;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.Test;

import com.gly.random.AbstractRandomVariable;

public class MmfeSingleFacilityDemandTest {

	@Test
	public void test() {
		RandomDataGenerator random = new RandomDataGenerator();
		random.reSeed(0);

		// see what values the random data generator will output
		double[] r = new double[16];
		for (int i = 0; i < 16; ++i) {
			r[i] = random.nextGaussian(0, 1);
		}

		// reset the random data generator
		random.reSeed(0);

		MmfeLevelsSingleFacilityDemand demand = new MmfeLevelsSingleFacilityDemand.Builder()
		.withDemandMean(new double[]{100, 200})
		.withStandardDeviation(new double[]{0.5, 0.1})
		.withForecastVarianceProportion(new double[]{0.36, 0.64})
		.withRandomDataGenerator(random)
		.withStartPeriod(0)
		.withEndPeriod(4)
		.withForecastLevel(1)
		.build();

		double d;
		double sum;
		double x;
		double var;
		double std;
		double mu;
		double sigma;
		AbstractRandomVariable rv;


		// check that the values of demand are what we expect
		// based on peeking at the random variates that are
		// output from the random data generator


		x = convert(r[0], 0.06);
		System.out.printf("Adding %.2f\n", x);
		sum = x;
		sum += convert(r[1], 0.08);
		sum += convert(r[2], 0.3);
		sum += convert(r[3], 0.4);
		d = 100 * Math.exp(sum);
		assertEquals((int) Math.round(d), demand.getDemand(0));

		rv = demand.getDemandForecast(-1, 0);
		d = 100;
		assertEquals(d, rv.getMean(), 0.001);

		// variance of lognormal random variable ln N(mu,sigma^2)
		// = $[\exp(\sigma^2) - 1] \exp(2\mu + \sigma^2)$

		rv = demand.getDemandForecast(0, 0);
		d = 100 * Math.exp(convert(r[0], 0.06));
		assertEquals(d, rv.getMean(), 0.001);
		var = Math.pow(0.08, 2);
		var += Math.pow(0.3, 2);
		var += Math.pow(0.4, 2);
		sigma = Math.sqrt(var);
		mu = -sigma * sigma / 2;
		std = d * Math.sqrt(
				(Math.exp(sigma*sigma) - 1) * Math.exp(2 * mu + sigma*sigma));
		assertEquals(std, rv.getStdDev(), 0.001);

		x = convert(r[4], 0.06);
		System.out.printf("Adding %.2f\n", x);
		sum = x;
		sum += convert(r[5], 0.08);
		sum += convert(r[6], 0.3);
		sum += convert(r[7], 0.4);
		d = 200 * Math.exp(sum);
		assertEquals((int) Math.round(d), demand.getDemand(1));

		x = convert(r[8], 0.06);
		System.out.printf("Adding %.2f\n", x);
		sum = x;
		sum += convert(r[9], 0.08);
		sum += convert(r[10], 0.3);
		sum += convert(r[11], 0.4);
		d = 100 * Math.exp(sum);
		assertEquals((int) Math.round(d), demand.getDemand(2));

		x = convert(r[12], 0.06);
		System.out.printf("Adding %.2f\n", x);
		sum = x;
		sum += convert(r[13], 0.08);
		sum += convert(r[14], 0.3);
		sum += convert(r[15], 0.4);
		d = 200 * Math.exp(sum);
		assertEquals((int) Math.round(d), demand.getDemand(3));


	}

	/**
	 * If we generate demand using the MmfeSingleFacilityDemand object,
	 * does it follow the lognormal distribution with the expected
	 * mean and variance?
	 */
	@Test
	public void testGenerate() {
		RandomDataGenerator random = new RandomDataGenerator();
		Mean mean = new Mean();
		Variance var = new Variance();

		// generate 10000 replications
		for (int i = 0; i < 10000; ++i) {
			random.reSeed(i);

			MmfeLevelsSingleFacilityDemand demand = new MmfeLevelsSingleFacilityDemand.Builder()
			.withDemandMean(new double[]{100})
			.withStandardDeviation(new double[]{0.5, 0.1})
			.withForecastVarianceProportion(new double[]{0.36, 0.64})
			.withRandomDataGenerator(random)
			.withStartPeriod(0)
			.withEndPeriod(4)
			.withForecastLevel(1)
			.build();

			int d = demand.getDemand(0);
			if (i < 10) {
				System.out.printf("i = %d, d = %d\n", i, d);
			}

			mean.increment(d);
			var.increment(d);
		}

		System.out.printf("mean = %.2f\n", mean.getResult());
		System.out.printf("theoretical mean = 100\n");
		System.out.printf("variance = %.2f\n", var.getResult());
		System.out.printf("theoretical variance = 2969\n");
		// variance of normal i.e. sigma^2 is 0.26
		// variance of lognormal is 10000 * exp(sigma^2) - 1
		// = 10000 * (exp(0.26) - 1) = 2969

	}


	/**
	 * If we generate demand using the MmfeSingleFacilityDemand object,
	 * does it follow the lognormal distribution with the expected
	 * mean and variance compared to the forecast?
	 */
	@Test
	public void testGenerateForecast() {
		RandomDataGenerator random = new RandomDataGenerator();
		Mean mean = new Mean();
		Variance var = new Variance();

		// generate 10000 replications
		for (int i = 0; i < 100000; ++i) {
			random.reSeed(i);

			MmfeLevelsSingleFacilityDemand demand = new MmfeLevelsSingleFacilityDemand.Builder()
			.withDemandMean(new double[]{100})
			.withStandardDeviation(new double[]{0.5, 0.5})
			.withForecastVarianceProportion(new double[]{0.36, 0.64})
			.withRandomDataGenerator(random)
			.withStartPeriod(0)
			.withEndPeriod(4)
			.withForecastLevel(2)
			.build();

			double f = demand.getDemandForecast(0, 0).getMean();
			int d = demand.getDemand(0);
			if (i < 10) {
				System.out.printf("i = %d, f = %.1f, d = %d\n", i, f, d);
			}

			mean.increment(d / f);
			var.increment(d / f);
		}

		System.out.println("Statistics for actual demand / forecast mean:");
		System.out.printf("mean = %.4f\n", mean.getResult());
		System.out.printf("theoretical mean = 1\n");
		System.out.printf("variance = %.4f\n", var.getResult());
		System.out.printf("theoretical variance = 0.5068\n");
		// for forecast level 1:
		// variance of normal i.e. sigma^2 is 0.25 + 0.16 = 0.41
		// variance of lognormal is exp(sigma^2) - 1
		// = exp(0.41) - 1 = 0.5068

		// for forecast level 2:
		// variance of normal i.e. sigma^2 is 0.25
		// variance of lognormal is exp(sigma^2) - 1
		// = exp(0.25) - 1 = 0.2840
	}


	private double convert(double r, double sigma) {
		return r * sigma - sigma * sigma / 2;
	}

}
