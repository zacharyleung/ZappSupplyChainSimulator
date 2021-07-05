package com.gly.util;

public interface PrettyToString {
	public static final int NUMBER_OF_SPACES = 2;
	
	public static final String PAD = StringUtils.getSpaces(NUMBER_OF_SPACES);
	
	public static final String NL = System.getProperty("line.separator");
	
	/**
	 * Pretty print the object with an indentation of the number
	 * of spaces specified by the constant.
	 * @param numberOfSpaces
	 * @return
	 */
	public abstract String prettyToString();
}
