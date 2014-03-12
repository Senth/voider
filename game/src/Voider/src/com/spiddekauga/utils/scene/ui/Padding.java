package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Wrapper for padding variables
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class Padding implements Poolable {
	/** Left padding */
	public float left = 0;
	/** right padding */
	public float right = 0;
	/** top padding */
	public float top = 0;
	/** bottom padding */
	public float bottom = 0;

	/**
	 * Sets the padding values from another Padding instance
	 * @param padding the other padding instance
	 */
	public void set(Padding padding) {
		left = padding.left;
		right = padding.right;
		top = padding.top;
		bottom = padding.bottom;
	}

	/**
	 * Sets all padding values to 0
	 */
	@Override
	public void reset() {
		left = 0;
		right = 0;
		top = 0;
		bottom = 0;
	}
}
