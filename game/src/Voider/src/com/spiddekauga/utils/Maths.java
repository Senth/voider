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
}
