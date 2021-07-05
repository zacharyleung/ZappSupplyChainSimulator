package com.gly.util;

import java.util.HashMap;

public class Sparse2DArray<E> {
	private HashMap<Key, E> map = new HashMap<>();
	
	public E get(int i, int j) throws ArrayIndexOutOfBoundsException {
		E object = map.get(new Key(i, j));
		if (object == null) {
			throw new ArrayIndexOutOfBoundsException(
					String.format("[%d][%d]", i, j));
		} else {
			return object;
		}
	}
	
	public void put(int i, int j, E object) throws IllegalArgumentException {
		if (object == null) {
			throw new IllegalArgumentException("Cannot put null object in SparseArray");
		}
		map.put(new Key(i, j), object);
	}
	
	private class Key {
		private final int i;
		private final int j;

		Key(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}

			if (this.getClass() != other.getClass()) {
				return false;
			}

			return ((Key) other).i == i &
					((Key) other).j == j;
		}

		@Override
		public int hashCode() {
			return 397 * i + j;
		}
	}

}
