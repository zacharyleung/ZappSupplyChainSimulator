package com.gly.util;

public class Negative2DArray <E> {

	private int i1;
	private int j1;
	private Object[][] array;
	
	/**
	 * Create a 2D array with indices [i1, i2) x [j1, j2).
	 * @param i1
	 * @param i2
	 * @param j1
	 * @param j2
	 */
	public Negative2DArray(int i1, int i2, int j1, int j2) {
		this(i1, i2, j1, j2, null);		
	}

	public Negative2DArray(int i1, int i2, int j1, int j2, E initial) {
		this.i1 = i1;
		this.j1 = j1;
		
		array = new Object[i2 - i1][j2 - j1];
	}

	public void set(int i, int j, E e) {
		array[i - i1][j - j1] = e;
	}
	
	public E get(int i, int j) {
		@SuppressWarnings("unchecked")
		E e = (E) array[i - i1][j - j1];
		return e;
	}
	
}
