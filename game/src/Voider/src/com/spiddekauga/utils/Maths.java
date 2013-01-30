package com.spiddekauga.utils;

import java.math.BigDecimal;

/**
 * Math utilities
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Maths {
	/**
	 * Rounds a value to the specified precision
	 * @param unrounded the value to round
	 * @param precision amount of presision
	 * @param roundingMode see BigDecimal.ROUND_HALF_UP etc
	 * @return rounded value
	 * @author bluedevil2k on StackExchange.com
	 */
	public static double round(double unrounded, int precision, int roundingMode)
	{
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	/**
	 * Checks whether two values are close to each other
	 * @param valueA compares against valueB
	 * @param valueB compares against valueA
	 * @param delta the maximum range the values can differ to be counted as close
	 * @return true if valueA - valueB is in less than delta and greater than -delta
	 */
	public static boolean approxCompare(float valueA, float valueB, float delta) {
		return approxCompare(valueA - valueB, delta);
	}

	/**
	 * Checks whether the value is within delta range
	 * @param value the value to compare
	 * @param delta if the value is within this range
	 * @return true if value <= delta && value >= -delta
	 */
	public static boolean approxCompare(float value, float delta) {
		return value <= delta && value >= -delta;
	}
}
