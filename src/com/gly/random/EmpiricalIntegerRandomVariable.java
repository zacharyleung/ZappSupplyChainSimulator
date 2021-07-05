package com.gly.random;

import java.util.*;

public class EmpiricalIntegerRandomVariable extends IntegerRandomVariable {

	public static final double EPSILON = 0.0001;
	/**
	 * Map values to probabilities.
	 */
	private TreeMap<Integer, Double> map = new TreeMap<>();

	public EmpiricalIntegerRandomVariable(Collection<Entry> entries) {
		// remaining probability
		double rp = 1.0;
		for (Entry entry : entries) {
			map.put(entry.value, entry.probability);
			rp -= entry.probability;
		}

		if (entries.size() != map.size()) {
			throw new IllegalArgumentException("entries contains duplicate entries!");
		}
		if (rp > EPSILON) {
			throw new IllegalArgumentException(
					String.format("Too much remaining probability = %.2e!", rp));
		}
		if (rp < -EPSILON) {
			throw new IllegalArgumentException("Sum of probabilities exceeds 1!");			
		}
	}

	@Override
	public int inverseCumulativeProbability(double p) {
		// check that 0 <= p <= 1
		if (p < 0 || p > 1) {
			throw new IllegalArgumentException("Illegal p = " + p);
		}

		for (Map.Entry<Integer, Double> entry : map.entrySet()) {
			p -= entry.getValue();
			if (p < 0) {
				return entry.getKey();
			}
		}
		return map.lastKey();
	}

	@Override
	public double probability(int x) {
		if (map.containsKey(x)) {
			return map.get(x);
		} else {
			return 0;
		}
	}

	@Override
	public List<Entry> getProbabilities() {
		LinkedList<Entry> entries = new LinkedList<>();
		for (Map.Entry<Integer, Double> entry : map.entrySet()) {
			entries.add(new Entry.Builder()
			.withValue(entry.getKey())
			.withProbability(entry.getValue())
			.build());
		}
		return entries;
	}

	@Override
	public int getMin() {
		return map.firstKey();
	}

	@Override
	public String toString() {
		return String.format("EmpiricalIntegerRandomVariable{0: %.2f, 1: %.2f, 2: %.2f, 3: %.2f}",
				map.get(0), map.get(1), map.get(2), map.get(3));
	}

}
