package com.gly.util;

/**
 * Given a set of increasing points x[k], k = 0, 1, ..., K - 1,
 * and function values y[k], k = 0, 1, ..., K - 1,
 * perform the following linear interpolation.
 * For x such that x[k] < x < x[k+1], let y be the linear interpolation
 * between y[k] and y[k+1].
 * For x such that x > x[K-1], y = y[K].
 * @author znhleung
 *
 */
public class LinearInterpolation {
	private double[] xval;
	private double[] yval;
	
	public LinearInterpolation(double[] x, double[] y) {
		this.xval = x;
		this.yval = y;
		for (int i = 0; i < x.length - 1; ++i) {
			if (x[i] >= x[i + 1]) {
				throw new IllegalArgumentException("not increasing");
			}
		}
		if (x.length != y.length) {
			throw new IllegalArgumentException("lengths do not match");
		}
	}
	
	public double getValue(double x) {
		int n = xval.length;
		for (int i = 0; i < n - 1; ++i) {
			if (xval[i] <= x && x <= xval[i + 1]) {
				double alpha = (x - xval[i]) / (xval[i + 1] - xval[i]);
				return alpha * yval[i + 1] + (1 - alpha) * yval[i];
			}
		}
		//if (x > xval[n - 1]) {
		return yval[n - 1];
	}
}
