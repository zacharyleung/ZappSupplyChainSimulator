package com.gly.scs.opt;

import java.io.*;
import java.util.Collection;

import com.gly.random.NormalRandomVariable;

public class PercentileTangentFactoryTest {
	private static double mean = 100.0;
	private static double standardDeviation = 50.0;

	public static void main(String[] args) throws Exception {
		UnmetDemandLowerBoundFactory tf = new UnmetDemandLowerBoundPercentileFactory(11);
		Collection<UnmetDemandLowerBound> tangents = tf.computeTangents(
				new NormalRandomVariable.Builder()
				.withMean(mean)
				.withStandardDeviation(standardDeviation)
				.build());

		try (PrintStream out = new PrintStream(new File("output/TestPercentileTangent/plots.tex"))) {
			for (UnmetDemandLowerBound tangent : tangents) {
				out.printf("  \\addplot{%.4f * x + %.4f};%n", 
						tangent.slope, tangent.intercept);
			}
		}
	}

}
