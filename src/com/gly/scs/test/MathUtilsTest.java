package com.gly.scs.test;

import java.util.Arrays;

import com.gly.util.MathUtils;

public class MathUtilsTest {

	public static void main(String[] args) {
		applyConvexCombinationToMean();
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		applyConvexCombinationToOne();
	}
	
	public static void applyConvexCombinationToMean() {
		double[] a = {10, 20, 30, 40, 50};
		System.out.println("a = " + Arrays.toString(a));
		System.out.println();
		double[] b;

		b = MathUtils.applyConvexCombinationToMean(a, 0);
		System.out.println("convex combination(a, 0.0) = " + Arrays.toString(b));

		b = MathUtils.applyConvexCombinationToMean(a, 0.5);
		System.out.println("convex combination(a, 0.5) = " + Arrays.toString(b));

		b = MathUtils.applyConvexCombinationToMean(a, 1);
		System.out.println("convex combination(a, 1.0) = " + Arrays.toString(b));
	}

	public static void applyConvexCombinationToOne() {
		double[] a = {0.2, 0.4, 0.6, 0.8, 1};
		System.out.println("a = " + Arrays.toString(a));
		System.out.println();
		double[] b;

		b = MathUtils.applyConvexCombinationToOne(a, 0);
		System.out.println("convex combination(a, 0.0) = " + Arrays.toString(b));

		b = MathUtils.applyConvexCombinationToOne(a, 0.5);
		System.out.println("convex combination(a, 0.5) = " + Arrays.toString(b));

		b = MathUtils.applyConvexCombinationToOne(a, 1);
		System.out.println("convex combination(a, 1.0) = " + Arrays.toString(b));
	}

	
}
