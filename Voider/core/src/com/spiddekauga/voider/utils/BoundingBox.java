package com.spiddekauga.voider.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Bounding box position
 */
public class BoundingBox {
/** Min value */
private float mLeft;
/** Max value */
private float mRight;
/** Max value */
private float mTop;
/** Min value */
private float mBottom;

/**
 * Create a bounding box with values of 0. Same as calling {@link #BoundingBox(float)} with 0.
 */
public BoundingBox() {
	this(0);
}

/**
 * Create bounding box with the specified values. Will set left and bottom to value; right and top
 * to -value. I.e. an invalid bounding box
 * @param value the values to set
 */
public BoundingBox(float value) {
	set(value, -value, -value, value);
}

/**
 * Set the bounding
 * @param left
 * @param top
 * @param right
 * @param bottom
 */
public void set(float left, float top, float right, float bottom) {
	mLeft = left;
	mTop = top;
	mRight = right;
	mBottom = bottom;
}

/**
 * Create a bounding box from an already existing one
 * @param boundingBox the bounding box to copy the values from
 */
public BoundingBox(BoundingBox boundingBox) {
	set(boundingBox);
}

/**
 * Set the bounding box from another bounding box
 * @param boundingBox
 */
public void set(BoundingBox boundingBox) {
	mLeft = boundingBox.mLeft;
	mRight = boundingBox.mRight;
	mTop = boundingBox.mTop;
	mBottom = boundingBox.mBottom;
}

/**
 * Check if this and the specified bounding box overlap
 * @param otherBox
 * @return true if this and the specified bounding box overlap
 */
public boolean overlaps(BoundingBox otherBox) {
	if (mLeft > otherBox.mRight || otherBox.mLeft > mRight) {
		return false;
	} else if (mBottom > otherBox.mTop || otherBox.mBottom > mTop) {
		return false;
	}

	return true;
}

/**
 * Offset all values with
 * @param offset
 */
public void offset(Vector2 offset) {
	mLeft += offset.x;
	mRight += offset.x;
	mTop += offset.y;
	mBottom += offset.y;
}

/**
 * Set the bounding box from a circle
 * @param radius
 */
public void setFromCircle(float radius) {
	mLeft = -radius;
	mRight = radius;
	mTop = radius;
	mBottom = -radius;
}

/**
 * Scale the bounding box
 * @param scale how much to scale the bounding box
 */
public void scale(float scale) {
	// Calculate horizontal center
	float xDiff = (mRight - mLeft) * 0.5f * scale;
	mLeft -= xDiff;
	mRight += xDiff;

	// Calculate vertical center
	float yDiff = (mTop - mBottom) * 0.5f * scale;
	mTop += yDiff;
	mBottom -= yDiff;
}

/**
 * @return width
 */
public float getWidth() {
	return mRight - mLeft;
}

/**
 * @return height
 */
public float getHeight() {
	return mTop - mBottom;
}

/**
 * @return the left
 */
public float getLeft() {
	return mLeft;
}

/**
 * @param left to set
 */
public void setLeft(float left) {
	this.mLeft = left;
}

/**
 * @return the right
 */
public float getRight() {
	return mRight;
}

/**
 * @param right to set
 */
public void setRight(float right) {
	this.mRight = right;
}

/**
 * @return the top
 */
public float getTop() {
	return mTop;
}

/**
 * @param top to set
 */
public void setTop(float top) {
	this.mTop = top;
}

/**
 * @return the bottom
 */
public float getBottom() {
	return mBottom;
}

/**
 * @param bottom to set
 */
public void setBottom(float bottom) {
	this.mBottom = bottom;
}
}
