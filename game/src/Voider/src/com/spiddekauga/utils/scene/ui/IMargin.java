package com.spiddekauga.utils.scene.ui;

/**
 * Some element that has margins
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <ReturnType> the type that has a margin. Useful when wanting to chain set events
 */
public interface IMargin<ReturnType> {
	/**
	 * Sets the margin
	 * @param top margin at the top
	 * @param right margin to the right
	 * @param bottom margin at the bottom
	 * @param left margin to the left
	 * @return this for chaining
	 */
	ReturnType setMargin(float top, float right, float bottom, float left);

	/**
	 * Set the margin
	 * @param margin margin to the left, right, top, and bottom
	 * @return this for chaining
	 */
	ReturnType setMargin(float margin);

	/**
	 * Set the margin from another margin
	 * @param margin another margin
	 * @return this for chaining
	 */
	ReturnType setMargin(Padding margin);

	/**
	 * Sets the left margin
	 * @param marginLeft margin to the left
	 * @return this for chaining
	 */
	ReturnType setMarginLeft(float marginLeft);

	/**
	 * Sets right top margin
	 * @param marginRight margin to the right
	 * @return this for chaining
	 */
	ReturnType setMarginRight(float marginRight);

	/**
	 * Sets the top margin
	 * @param marginTop margin to the top
	 * @return this for chaining
	 */
	ReturnType setMarginTop(float marginTop);

	/**
	 * Sets the bottom margin
	 * @param marginBottom margin to the bottom
	 * @return this for chaining
	 */
	ReturnType setMarginBottom(float marginBottom);

	/**
	 * @return all margins
	 */
	Padding getMargin();

	/**
	 * @return top margin
	 */
	float getMarginTop();

	/**
	 * @return right margin
	 */
	float getMarginRight();

	/**
	 * @return bottom margin
	 */
	float getMarginBottom();

	/**
	 * @return left margin
	 */
	float getMarginLeft();
}
