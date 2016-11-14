package com.spiddekauga.utils.scene.ui;

/**
 * An element that has padding
 * @param <ReturnType> the type that has a padding. Useful when wanting to chain set events
 */
public interface IPadding<ReturnType> {
/**
 * Sets the padding
 * @param top padding at the top
 * @param right padding to the right
 * @param bottom padding at the bottom
 * @param left padding to the left
 * @return this for chaining
 */
ReturnType setPad(float top, float right, float bottom, float left);

/**
 * Set the padding
 * @param padding padding to the left, right, top, and bottom
 * @return this for chaining
 */
ReturnType setPad(float padding);

/**
 * Set the padding from another padding
 * @param padding another padding
 * @return this for chaining
 */
ReturnType setPad(Padding padding);

/**
 * Sets the left padding
 * @param paddingLeft padding to the left
 * @return this for chaining
 */
ReturnType setPadLeft(float paddingLeft);

/**
 * Sets right top padding
 * @param paddingRight padding to the right
 * @return this for chaining
 */
ReturnType setPadRight(float paddingRight);

/**
 * Sets the top padding
 * @param paddingTop padding to the top
 * @return this for chaining
 */
ReturnType setPadTop(float paddingTop);

/**
 * Sets the bottom padding
 * @param paddingBottom padding to the bottom
 * @return this for chaining
 */
ReturnType setPadBottom(float paddingBottom);

/**
 * @return all paddings
 */
Padding getPad();

/**
 * @return top padding
 */
float getPadTop();

/**
 * @return right padding
 */
float getPadRight();

/**
 * @return bottom padding
 */
float getPadBottom();

/**
 * @return left padding
 */
float getPadLeft();

/**
 * @return get padding Y (i.e. padding top + bottom)
 */
float getPadY();

/**
 * @return get padding X (i.e. padding left + right)
 */
float getPadX();
}
