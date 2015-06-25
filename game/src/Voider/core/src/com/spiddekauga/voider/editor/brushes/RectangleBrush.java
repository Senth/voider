package com.spiddekauga.voider.editor.brushes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx;

/**
 * A simple resource that draws a rectangle
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RectangleBrush extends Brush {
	/**
	 * Creates the rectangle with a start point. The end point will be set to the start
	 * point.
	 * @param color the color of the brush
	 * @param startPoint where the rectangle starts
	 */
	public RectangleBrush(Color color, Vector2 startPoint) {
		this(color, startPoint, startPoint);
	}

	/**
	 * Creates the rectangle with a start and end point
	 * @param color the color of the brush
	 * @param startPoint where the rectangle starts
	 * @param endPoint where the rectangle ends
	 */
	public RectangleBrush(Color color, Vector2 startPoint, Vector2 endPoint) {
		super(color);
		mStartPoint.set(startPoint);
		mEndPoint.set(endPoint);
	}

	@Override
	protected boolean preRender() {
		return !mStartPoint.equals(mEndPoint);
	}

	@Override
	protected void render(ShapeRendererEx shapeRenderer) {
		shapeRenderer.rect(mStartPoint, mEndPoint);
	}

	/**
	 * Sets the start position of the rectangle
	 * @param startPosition the start position of the rectangle
	 */
	public void setStartPosition(Vector2 startPosition) {
		mStartPoint.set(startPosition);
	}

	/**
	 * Sets the end position of the rectangle
	 * @param endPosition end position of the rectangle
	 */
	public void setEndPosition(Vector2 endPosition) {
		mEndPoint.set(endPosition);
	}

	/** Start point of the rectangle */
	private Vector2 mStartPoint = new Vector2();
	/** End point of the rectangle */
	private Vector2 mEndPoint = new Vector2();
}
