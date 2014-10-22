package com.spiddekauga.utils;

import java.math.BigDecimal;

/**
 * Math utilities
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
	public static double round(double unrounded, int precision, int roundingMode) {
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	/**
	 * Checks whether two values are close to each other
	 * @param valueA compares against valueB
	 * @param valueB compares against valueA
	 * @param delta the maximum range the values can differ to be counted as close
	 * @return true if valueA - valueB is in less or equal to delta and greater or equal
	 *         to -delta
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

	/**
	 * Compare if two floats are close to equal
	 * @param left compares against right
	 * @param right compares against left
	 * @return true if left -right is in the range of [{@value #FLOAT_EQUALS_DELTA},
	 *         {@value #FLOAT_EQUALS_DELTA}].
	 */
	public static boolean floatEquals(float left, float right) {
		float diff = left - right;
		return diff <= FLOAT_EQUALS_DELTA && diff >= -FLOAT_EQUALS_DELTA;
	}

	/**
	 * Calculate order of magnitude for the value
	 * @param value
	 * @return magnitude values
	 */
	public static MagnitudeWrapper calculateMagnitude(float value) {
		MagnitudeWrapper magnitude = new MagnitudeWrapper();

		float positive = value < 0 ? -value : value;

		// Int
		int intValue = (int) positive;
		if (intValue != 0) {
			magnitude.intMag = (int) (Math.log10(intValue) + 1.0);
		} else {
			magnitude.intMag = 1;
		}

		// Decimal
		float decValue = positive - intValue;
		if (decValue != 0) {
			String decString = String.valueOf(value);
			int decPosition = decString.indexOf(".");
			if (decPosition == -1) {
				decPosition = decString.indexOf(",");
			}
			decString = decString.substring(decPosition + 1);
			magnitude.decMag = decString.length();
		}

		return magnitude;
	}


	/**
	 * Wrapper class for magnitude values
	 */
	public static class MagnitudeWrapper {
		/**
		 * @return integer-part magnitude
		 */
		public int getInt() {
			return intMag;
		}

		/**
		 * @return decimal-part magnitude
		 */
		public int getDec() {
			return decMag;
		}

		/**
		 * @return total magnitude
		 */
		public int get() {
			return intMag + decMag;
		}

		/** Integer magnitude */
		private int intMag = 0;
		/** Decimal magnitude */
		private int decMag = 0;
	}

	/** Float compare value */
	public static float FLOAT_EQUALS_DELTA = 0.001f;
}
