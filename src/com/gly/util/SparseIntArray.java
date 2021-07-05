package com.gly.util;

public class SparseIntArray extends SparseArray<Integer> {
	@Override
	public Integer get(int index) throws ArrayIndexOutOfBoundsException {
		try {
			return super.get(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
}
