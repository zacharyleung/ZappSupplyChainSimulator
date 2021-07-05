package com.gly.scs.opt;

import java.util.LinkedList;
import java.util.List;

import com.gly.random.AbstractRandomVariable;
import com.gly.random.LognormalRandomVariable;
import com.gly.scs.opt.UnmetDemandLowerBound.Builder;
import com.gly.util.MathUtils;
import com.gly.util.MathUtils.Line;
import com.gly.util.MathUtils.Point2D;

public class UnmetDemandLowerBoundPercentileFactory extends UnmetDemandLowerBoundFactory {

	private final int numberOfTangents;
	
	public UnmetDemandLowerBoundPercentileFactory(int numberOfTangents) {
		this.numberOfTangents = numberOfTangents;
		
		if (numberOfTangents < 2) {
			throw new IllegalArgumentException("numberOfTangents < 2!");
		}
	}
	
	@Override
	public List<UnmetDemandLowerBound> subComputeTangents(AbstractRandomVariable randomVariable) {
		double n = numberOfTangents;
		
		double mean = randomVariable.getMean();
		double std = randomVariable.getStdDev();
		LognormalRandomVariable lognormal =
				new LognormalRandomVariable.Builder()
		.withMean(mean)
		.withStandardDeviation(std)
		.build();
		
		LinkedList<UnmetDemandLowerBound> tangents = new LinkedList<>();
		
		for (int i = 0; i < n; ++i) {
			double p1 = i / (n + 1);
			double x1 = lognormal.inverseCumulativeProbability(p1);
			double y1 = unmetDemand(x1, randomVariable);
			Point2D q1 = new Point2D(x1, y1);
			
			double p2 = (i + 1) / (n + 1);
			double x2 = lognormal.inverseCumulativeProbability(p2);
			double y2 = unmetDemand(x2, randomVariable);
			Point2D q2 = new Point2D(x2, y2);
			
			Line line = MathUtils.getLine(q1, q2);
			tangents.addLast(new UnmetDemandLowerBound.Builder()
			.withIntercept(line.intercept)
			.withSlope(line.slope)
			.build());
		}

		return tangents;
	}

}
