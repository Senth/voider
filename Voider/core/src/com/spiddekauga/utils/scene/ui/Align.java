package com.spiddekauga.utils.scene.ui;

/**
 * Alignment for UI
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Align {
	/**
	 * Default constructor, sets alignment to left and middle
	 */
	Align() {
		this(Horizontal.LEFT, Vertical.MIDDLE);
	}

	/**
	 * Creates an alignment with the specified horizontal and vertical alignment
	 * @param horizontal the horizontal alignment
	 * @param vertical the vertical alignment
	 */
	Align(Horizontal horizontal, Vertical vertical) {
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	/**
	 * Sets the alignment from another alignment
	 * @param align the other alignment to copy values from
	 */
	void set(Align align) {
		horizontal = align.horizontal;
		vertical = align.vertical;
	}

	/** Horizontal Alignment */
	public Horizontal horizontal;
	/** Vertical alignment */
	public Vertical vertical;

	/**
	 * Enumeration for horizontal alignment
	 */
	public enum Horizontal {
		/** LEFT on the x-axis */
		LEFT,
		/** Center on the x-axis */
		CENTER,
		/** RIGHT on the x-axis */
		RIGHT
	}

	/**
	 * Enumeration for vertical alignment
	 */
	public enum Vertical {
		/** Top on the y-axis */
		TOP,
		/** Middle on the y-axis */
		MIDDLE,
		/** Bottom on the y-axis */
		BOTTOM
	}
}
