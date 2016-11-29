package com.spiddekauga.utils.scene.ui;

/**
 * Alignment for UI
 */
public class Align {
/** Horizontal Alignment */
public Horizontal horizontal;
/** Vertical alignment */
public Vertical vertical;

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
 * Convert this alignment to libgdx's internal alignment
 * @return libgdx's internal alignment
 */
public static int toLibgdxAlign(Horizontal horizontal, Vertical vertical) {
	int align = 0;

	if (horizontal != null) {
		switch (horizontal) {
		case LEFT:
			align = com.badlogic.gdx.utils.Align.left;
			break;
		case CENTER:
			align = com.badlogic.gdx.utils.Align.center;
			break;
		case RIGHT:
			align = com.badlogic.gdx.utils.Align.right;
			break;
		}
	}

	if (vertical != null) {
		switch (vertical) {
		case TOP:
			align |= com.badlogic.gdx.utils.Align.top;
			break;
		case MIDDLE:
			// N/A
			break;
		case BOTTOM:
			align |= com.badlogic.gdx.utils.Align.bottom;
			break;
		}
	}

	return align;
}

/**
 * Sets the alignment from another alignment
 * @param align the other alignment to copy values from
 */
void set(Align align) {
	horizontal = align.horizontal;
	vertical = align.vertical;
}

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
