package com.gly.scs.leadtime;

import static org.junit.Assert.*;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import com.gly.random.IntegerRandomVariable.Entry;
import com.gly.scs.leadtime.VehicleAccessibilitySingleFacilityLeadTime.MinimumValue;

public class VehicleAccessibilitySingleFacilityLeadTimeTest {

	private MinimumValue minimumValue = MinimumValue.ONE;

	@Test
	public void testMean() {
		int n = 100000; // sample size
		double[] accessibility = {1};
		double mean = 2;
		RandomDataGenerator random = new RandomDataGenerator();
		random.reSeed(0);

		VehicleAccessibilitySingleFacilityLeadTime leadTime =
				new VehicleAccessibilitySingleFacilityLeadTime.Builder()
		.withStartPeriod(0)
		.withEndPeriod(n)
		.withRandomDataGenerator(random)
		.withMean(mean)
		.withAccessibility(accessibility)
		.withMinimumValue(minimumValue)
		.build();

		SummaryStatistics stats = new SummaryStatistics();
		for (int i = 0; i < n; ++i) {
			try {
				stats.addValue(leadTime.getLeadTime(i));
			} catch (Exception e) {}
		}
		System.out.printf("mean = %.2f%n", stats.getMean());
	}

	@Test
	public void fullAccessibilityZero() {
		double[] accessibility = {1};
		double mean = 2;

		RandomDataGenerator random = new RandomDataGenerator();
		random.reSeed(0);
		for (int t = 0; t < 10; ++t) {
			System.out.printf("random[%d] = %.2f\n", t, random.nextUniform(0, 1));
		}
		random.reSeed(0);

		VehicleAccessibilitySingleFacilityLeadTime leadTime =
				new VehicleAccessibilitySingleFacilityLeadTime.Builder()
		.withStartPeriod(0)
		.withEndPeriod(10)
		.withRandomDataGenerator(random)
		.withMean(mean)
		.withAccessibility(accessibility)
		.withMinimumValue(MinimumValue.ZERO)
		.build();

		assertEquals(5, leadTime.getLeadTime(0));
		assertEquals(4, leadTime.getLeadTime(1));
		assertEquals(3, leadTime.getLeadTime(2));
		assertEquals(2, leadTime.getLeadTime(3));
		assertEquals(1, leadTime.getLeadTime(4));
		assertEquals(0, leadTime.getLeadTime(5));
		assertEquals(2, leadTime.getLeadTime(6));
		assertEquals(1, leadTime.getLeadTime(7));
		assertEquals(0, leadTime.getLeadTime(8));
		assertEquals(0, leadTime.getLeadTime(9));

		List<Entry> entries = leadTime.getLeadTimeRandomVariable(0)
				.getProbabilities();

		for (Entry entry : entries) {
			System.out.println(entry);
		}
	}



	@Test
	public void fullAccessibilityOne() {
		double[] accessibility = {1};
		double mean = 2;

		RandomDataGenerator random = new RandomDataGenerator();
		random.reSeed(0);
		for (int t = 0; t < 10; ++t) {
			System.out.printf("random[%d] = %.2f\n", t, random.nextUniform(0, 1));
		}
		random.reSeed(0);

		VehicleAccessibilitySingleFacilityLeadTime leadTime =
				new VehicleAccessibilitySingleFacilityLeadTime.Builder()
		.withStartPeriod(0)
		.withEndPeriod(10)
		.withRandomDataGenerator(random)
		.withMean(mean)
		.withAccessibility(accessibility)
		.withMinimumValue(MinimumValue.ONE)
		.build();

		assertEquals(2, leadTime.getLeadTime(0));
		assertEquals(1, leadTime.getLeadTime(1));
		assertEquals(3, leadTime.getLeadTime(2));
		assertEquals(2, leadTime.getLeadTime(3));
		assertEquals(1, leadTime.getLeadTime(4));
		assertEquals(3, leadTime.getLeadTime(5));
		assertEquals(2, leadTime.getLeadTime(6));
		assertEquals(1, leadTime.getLeadTime(7));
		assertEquals(1, leadTime.getLeadTime(8));
		try {
			leadTime.getLeadTime(9);
			fail( "My method didn't throw when I expected it to" );
		} catch (NoSuchElementException e) {}

		List<Entry> entries = leadTime.getLeadTimeRandomVariable(0)
				.getProbabilities();

		for (Entry entry : entries) {
			System.out.println(entry);
		}
	}


	@Test
	public void partialAccessibilityZero() {
		double[] accessibility = {1, 0};
		double mean = 2;

		RandomDataGenerator random = new RandomDataGenerator();
		random.reSeed(0);
		for (int t = 0; t < 10; ++t) {
			System.out.printf("random[%d] = %.2f\n", t, random.nextUniform(0, 1));
		}
		random.reSeed(0);

		VehicleAccessibilitySingleFacilityLeadTime leadTime =
				new VehicleAccessibilitySingleFacilityLeadTime.Builder()
		.withStartPeriod(0)
		.withEndPeriod(10)
		.withRandomDataGenerator(random)
		.withMean(mean)
		.withAccessibility(accessibility)
		.withMinimumValue(MinimumValue.ZERO)		
		.build();

		assertEquals(8, leadTime.getLeadTime(0));
		assertEquals(7, leadTime.getLeadTime(1));
		assertEquals(6, leadTime.getLeadTime(2));
		assertEquals(5, leadTime.getLeadTime(3));
		assertEquals(4, leadTime.getLeadTime(4));
		assertEquals(3, leadTime.getLeadTime(5));
		assertEquals(2, leadTime.getLeadTime(6));
		assertEquals(1, leadTime.getLeadTime(7));
		assertEquals(0, leadTime.getLeadTime(8));
		try {
			leadTime.getLeadTime(9);
			fail( "My method didn't throw when I expected it to" );
		} catch (NoSuchElementException e) {}

		List<Entry> entries = leadTime.getLeadTimeRandomVariable(0)
				.getProbabilities();

		for (Entry entry : entries) {
			System.out.println(entry);
		}
	}

}
