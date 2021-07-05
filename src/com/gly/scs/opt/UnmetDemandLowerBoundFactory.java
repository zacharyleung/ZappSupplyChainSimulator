package com.gly.scs.opt;

import java.util.*;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;

import com.gly.random.AbstractRandomVariable;

public abstract class UnmetDemandLowerBoundFactory {

	private static NormalDistribution normalDistribution =
			new NormalDistribution();
		
	public Collection<UnmetDemandLowerBound> computeTangents(
			AbstractRandomVariable randomVariable) {
		List<UnmetDemandLowerBound> tangents;
		// if the coefficient of variation is small, then the demand
		// is basically deterministic
		if (randomVariable.getCoefficientOfVariation() < 0.05) {
			tangents = new LinkedList<>();
			tangents.add(new UnmetDemandLowerBound.Builder()
			.withSlope(-1)
			.withIntercept(randomVariable.getMean())
			.build());
		} else {
			tangents = subComputeTangents(randomVariable);
		}
		
		//System.out.println("AbstractTangentFactory.computeTangents()");
		//for (Tangent tangent : tangents) {
		//	System.out.println(tangent);
		//}
		
		return tangents;
	}
	
	public abstract List<UnmetDemandLowerBound> subComputeTangents(
			AbstractRandomVariable randomVariable);
	
	/**
	 * Compute unmet demand assuming that the demand random variable is
	 * lognormally distributed with that mean and standard deviation.
	 * @return
	 */
	protected double unmetDemand(double inventory, AbstractRandomVariable randomVariable) {
		double mean = randomVariable.getMean();
		double std = randomVariable.getStdDev();
		
		double sigma = Math.sqrt(Math.log(std * std / (mean * mean) + 1));
		double mu = Math.log(mean) - sigma * sigma / 2;
		
		double sigma2 = sigma * sigma;
		
		double lni = Math.log(inventory);
		
		return Math.exp(mu + sigma2 / 2) * 
				normalDistribution.cumulativeProbability((mu + sigma2 - lni) / sigma) -
				inventory / 2 * (1 - Erf.erf((lni - mu) / (Math.sqrt(2) * sigma)));
	}


}
