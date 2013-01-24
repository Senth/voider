package com.spiddekauga.utils.scene.ui;

/**
 * Alignment for UI
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Align {
	/** Centered in x-axis
	 * @See #middle for centered on y-axis */
	static public final int CENTER = 1 << 0;
	/** Aligned to the top (y-axis) */
	static public final int TOP = 1 << 1;
	/** Aligned to the bottom (y-axis) */
	static public final int BOTTOM = 1 << 2;
	/** Aligned to the left (x-axis) */
	static public final int LEFT = 1 << 3;
	/** Aligned to the right (x-axis) */
	static public final int RIGHT = 1 << 4;
	/** Aligned in the middle (y-axis)
	 * @see #CENTER for centered on x-axis */
	static public final int MIDDLE = 1 << 5;
}
