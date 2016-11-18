package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Wrapper for padding variables
 */
public class Padding implements Poolable {
public float left = 0;
public float right = 0;
public float top = 0;
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
