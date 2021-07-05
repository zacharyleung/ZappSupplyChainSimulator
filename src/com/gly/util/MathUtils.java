package com.gly.util;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.primitives.Doubles;

public final class MathUtils {
	private MathUtils() {}
	
	public static double[] copyOf(double[] a) {
		return Arrays.copyOf(a, a.length);
	}
	
	/**
	 * Take a modulo b and return a positive number.
	 * The default Java modulo sets -3 % 4 = -3.  This modulo
	 * function returns 1. 
	 * @param a
	 * @param b
	 * @return a positive number in 0,...,b-1
	 */
	public static int positiveModulo(int a, int b)
			throws IllegalArgumentException {
		if (b < 0) {
			throw new IllegalArgumentException("b = " + b + " is negative.");
		}
		return (a % b + b) % b;
	}
	
	/**
	 * Get n equally spaced samples from the interval [min,max]
	 * with the smallest sample = min and the largest sample = max.
	 * @param min
	 * @param max
	 * @param n
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static double[] getSamples(double min, double max, int n)
			throws IllegalArgumentException {
		if (min > max) {
			throw new IllegalArgumentException("min > max!");
		}
		if (n < 1) {
			throw new IllegalArgumentException("n < 1!");
		}
		if (n == 1) {
			return new double[]{(min + max) / 2};
		}
		
		double[] a = new double[n];
		double inc = (max - min) / (n - 1);
		for (int i = 0; i < n; ++i) {
			a[i] = min + i * inc;
		}
		return a;
	}
	
	/**
	 * Generate a regular sequence: from, from + by, from + 2 * by, ...,
	 * up to the sequence value less than or equal to to. 
	 */
	public static double[] seq(double from, double to, double by) {
		LinkedList<Double> l = new LinkedList<>();
		for (double d = from; d <= to; d += by) {
			l.add(d);			
		}
		return Doubles.toArray(l);
	}
	
	public static double getMax(double[] array) {
		SummaryStatistics summary = new SummaryStatistics();
		for (int i = 0; i < array.length; ++i) {
			summary.addValue(array[i]);
		}
		return summary.getMax();
	}

	public static double getMin(double[] array) {
		SummaryStatistics summary = new SummaryStatistics();
		for (int i = 0; i < array.length; ++i) {
			summary.addValue(array[i]);
		}
		return summary.getMin();
	}

	/**
	 * Compute the mean of an array.
	 * @param array
	 * @return
	 */
	public static double getMean(double[] array) {
		SummaryStatistics summary = new SummaryStatistics();
		for (int i = 0; i < array.length; ++i) {
			summary.addValue(array[i]);
		}
		return summary.getMean();
	}
	
	public static double getSum(double[] array) {
		SummaryStatistics summary = new SummaryStatistics();
		for (int i = 0; i < array.length; ++i) {
			summary.addValue(array[i]);
		}
		return summary.getSum();
	}
	
	public static double doubleDivision(long a, long b) {
		return a * 1.0 / b;
	}
	
	/**
	 * Return the line y = ax + b connecting the points p and q.
	 * @param p
	 * @param q
	 * @return
	 */
	public static Line getLine(Point2D p, Point2D q) {
		double slope = (q.y - p.y) / (q.x - p.x);
		double intercept = p.y - slope * p.x;
		return new Line(slope, intercept);
	}
	
	public static class Point2D {
		public final double x;
		public final double y;
		
		public Point2D(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public static class Line {
		public final double slope;
		public final double intercept;
		
		private Line(double slope, double intercept) {
			this.slope = slope;
			this.intercept = intercept;
		}
	}
	
	public static int[] reverse(int[] a) {
		int N = a.length;
		int[] b = new int[N];
		for (int i = 0; i < N; ++i) {
			b[i] = a[N - 1 - i];
		}
		return b;
	}
	
	/** Return a sample, selected uniformly at random from an array. */ 
	public static double sample(RandomDataGenerator r, double[] d) {
		if (d.length == 1) {
			return d[0];
		}

		// nextInt(int lower, int upper)
		// Generates a uniformly distributed random integer between lower and
		// upper (endpoints included).
		int i = r.nextInt(0, d.length - 1);
		return d[i];
	}
	

	/**
	 * The convexParameter should be between a number between 0 and 1.
	 *   - The value of 0 means that every array value a[i] = mean.
	 *   - The value of 1 means that every array value a[i] is unchanged.
	 */
	public static double[] applyConvexCombinationToMean(
			double[] a, double convexParameter) {
		// Create a copy of the array
		double[] copy = a.clone();
		
		// Calculate the mean demand
		double mean = getMean(copy);
		
		// Apply the effect of the demand parameter
		double p = convexParameter;
		for (int i = 0; i < copy.length; ++i) {
			copy[i] = p * copy[i] + (1 - p) * mean;
		}

		return copy;
	}

	/**
	 * The convexParameter should be between a number between 0 and 1.
	 *   - The value of 0 means that every array value a[i] = 1.
	 *   - The value of 1 means that every array value a[i] is unchanged.
	 */
	public static double[] applyConvexCombinationToOne(
			double[] a, double convexParameter) {
		// Create a copy of the array
		double[] copy = a.clone();
		
		// Apply the effect of the demand parameter
		double p = convexParameter;
		for (int i = 0; i < copy.length; ++i) {
			copy[i] = p * copy[i] + (1 - p) * 1;
		}

		return copy;
	}
	
}
