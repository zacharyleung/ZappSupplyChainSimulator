package com.gly.scs.exp.exp06;

public class Params20170315 extends Params {
	@Override
	public double[] getInitialScale(int packSize) {
		switch(packSize) {
		case 6:
			return new double[]{0.9562};
		case 12:
			return new double[]{1.8319};
		case 18:
			return new double[]{1.8213};
		case 24:
			return new double[]{1.1741};
		default:
			throw new IllegalArgumentException();
		}
	}
}



