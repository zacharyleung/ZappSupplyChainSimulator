package com.gly.util;

import java.util.HashMap;

/**
 * SparseArray maps integers to non-null Objects. 
 * Unlike a normal array of Objects, there can be gaps in the indices.
 * Internally, SparseArray uses a HashMap to map Integers to Objects.
 * Additionally, SparseArray performs error checking:
 * <ul>
 * <li>
 * If you try to get an object from a non-existent array index,
 * then SparseArray will throw an exception.
 * </li>
 * <li>
 * If you try to put a null object into the SparseArray,
 * then SparseArray will throw an exception.
 * </li>
 * </ul>
 * 
 * Adapted from http://developer.android.com/reference/android/util/SparseArray.html
 * 
 * @author zacharyleung
 *
 */
public class SparseArray<E> {
	private HashMap<Integer, E> map = new HashMap<>();
	
	public E get(int index) throws ArrayIndexOutOfBoundsException {
		E object = map.get(index);
		if (object == null) {
			throw new ArrayIndexOutOfBoundsException(index);
		} else {
			return object;
		}
	}
	
	public void put(int index, E object) throws IllegalArgumentException {
		if (object == null) {
			throw new IllegalArgumentException("Cannot put null object in SparseArray");
		}
		map.put(index, object);
	}
}
