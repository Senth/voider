package com.spiddekauga.voider.editor.brushes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.utils.Pools;

/**
 * A simple resource that draws a rectangle
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class RectangleBrush extends Resource implements IResourceEditorRender, Disposable {
	/**
	 * Creates the rectangle with a start point. The end point
	 * will be set to the start point.
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
		mColor.set(color);
		mStartPoint.set(startPoint);
		mEndPoint.set(endPoint);
	}

	@Override
	public void dispose() {
		Pools.vector2.freeAll(mStartPoint, mEndPoint);
		Pools.color.free(mColor);
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		shapeRenderer.setColor(mColor);

		// Draw rectangle
		if (!mStartPoint.equals(mEndPoint)) {
			shapeRenderer.push(ShapeType.Line);

			shapeRenderer.rect(mStartPoint, mEndPoint);

			shapeRenderer.pop();
		}
		// Draw point as start and end is the same
		else {
			shapeRenderer.push(ShapeType.Point);

			shapeRenderer.point(mStartPoint.x, mStartPoint.y, 0);

			shapeRenderer.pop();
		}
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

	/** Color of the brush */
	private Color mColor = Pools.color.obtain();
	/** Start point of the rectangle */
	private Vector2 mStartPoint = Pools.vector2.obtain();
	/** End point of the rectangle */
	private Vector2 mEndPoint = Pools.vector2.obtain();
}
