package com.gly.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public abstract class StringUtils {
	/** Return the current time as a string, e.g., "2017-06-13 18:27:07". */
	public static String getTimeAsString() {
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());
	}
	
	public static String prependToEachLine(String input, String addend) {
		Scanner scanner = new Scanner(input);
		StringBuilder sb = new StringBuilder();
		String x = "";
		while (scanner.hasNextLine()) {
			sb.append(String.format("%s%s%s", x, addend, scanner.nextLine()));
			x = System.getProperty("line.separator");
		}
		scanner.close();
		return sb.toString();
	}
	
	/**
	 * Transform
	 * <code>
	 * abc
	 * 123
	 * </code>
	 * to
	 * <code>
	 *    abc
	 *    123
	 * </code>
	 * @param input
	 * @param spaces
	 * @return
	 */
	public static String prependSpacesToEachLine(String input, int numberOfSpaces) {
		String pad = getSpaces(numberOfSpaces);
		return prependToEachLine(input, pad);
	}
	
	public static String getSpaces(int numberOfSpaces) {
		String pad = "";
		for (int i = 0; i < numberOfSpaces; ++i) {
			pad += " ";
		}
		return pad;
	}
}
