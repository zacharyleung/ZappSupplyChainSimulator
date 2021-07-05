package com.gly.util;

/**
 * 
 * @author zacharyleung
 *
 * @param <E>
 */
public class NegativeArray <E> {
	
	private int firstIndex;
	private int lastIndex;
	private Object[] array;
	
	/**
	 * Create an array with indices [firstIndex, lastIndex).
	 * @param firstIndex
	 * @param lastIndex
	 */
	public NegativeArray(int firstIndex, int lastIndex) {
		this(firstIndex, lastIndex, null);		
	}
	
	public NegativeArray(int firstIndex, int lastIndex, E initial) {
		this.firstIndex = firstIndex;
		this.lastIndex = lastIndex;
		
		boolean isValid = lastIndex > firstIndex; 
		if (!isValid) {
			throw new IllegalArgumentException(
					"lastIndex > firstIndex violated!");
		}
		
		// create the array
		array = new Object[size()];
		for (int i = 0; i < array.length; ++i)
			array[i] = initial;
	}
	
	public void set(int index, E e) {
		checkIndexValidity(index);
		array[getIndex(index)] = e;
	}
	
	public E get(int index) {
		checkIndexValidity(index);
		@SuppressWarnings("unchecked")
		E e = (E) array[getIndex(index)];
		return e;
	}
	
	public int size() {
		return lastIndex - firstIndex;
	}
	
	private int getIndex(int index) {
		return index - firstIndex;
	}
	
	private void checkIndexValidity(int index) {
		boolean isValid = (index >= firstIndex) & 
				(index < lastIndex);
		if (!isValid) {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
}