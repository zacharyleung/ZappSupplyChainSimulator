package com.gly.random;

import java.util.LinkedList;
import java.util.List;

public class ConstantIntegerRandomVariable extends IntegerRandomVariable {

	private final int value;
	
	public ConstantIntegerRandomVariable(int value) {
		this.value = value;
		
		if (value < 0) {
			throw new IllegalArgumentException(
					String.format("value = %d < 0!", value));
		}
	}
	
	@Override
	public double probability(int x) {
		if (x == value) {
			return 1;
		} else {
			return 0;			
		}
	}

	@Override
	public int inverseCumulativeProbability(double p) {
		return value;
	}

	@Override
	public List<Entry> getProbabilities() {
		List<Entry> entries = new LinkedList<Entry>();
		entries.add(new Entry.Builder()
		.withValue(value)
		.withProbability(1.0)
		.build());
		return entries;
	}

	@Override
	public int getMin() {
		return value;
	}

}
