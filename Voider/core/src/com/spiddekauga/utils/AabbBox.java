package com.spiddekauga.utils;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * An AABB box for collision detection
 */
public class AabbBox {
/** Minimum position of the AABB box (lower left corner) */
private Vector2 mMinPos = new Vector2();
/** Maximum position of the AABB box (upper right corner) */
private Vector2 mMaxPos = new Vector2();

/**
 * Sets the AABB box
 * @param minPos minimum position (lower left corner)
 * @param maxPos maximum position (upper right corner)
 */
public void setFromBox(Vector2 minPos, Vector2 maxPos) {
	mMinPos.set(minPos);
	mMaxPos.set(maxPos);
}

/**
 * Sets the AABB box from a circle
 * @param center position of the circle
 * @param radius radius of the circle
 */
public void setFromCircle(Vector2 center, float radius) {
	mMinPos.set(center.x - radius, center.y - radius);
	mMaxPos.set(center.x + radius, center.y + radius);
}

/**
 * Sets the AABB box from a box, but uses a center position and with/height to determine
 * minimum/maxiumum positions
 * @param position position of the box
 * @param halfWidth half width of the box
 * @param halfHeight half height of the box
 */
public void setFromBox(Vector2 position, float halfWidth, float halfHeight) {
	mMinPos.set(position.x - halfWidth, position.y - halfHeight);
	mMinPos.set(position.x + halfWidth, position.y + halfHeight);
}

/**
 * @copydoc setFromPolygon(Vector[])
 */
@SuppressWarnings("javadoc")
public void setFromPolygon(List<Vector2> vertices) {
	setFromPolygon(vertices.toArray(new Vector2[0]));
}

/**
 * Sets the AABB box from a polygon. Traverses through all vertices to get the lowest and highest
 * positions
 * @param vertices all vertices in the polygon
 */
public void setFromPolygon(Vector2[] vertices) {
	mMinPos.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	mMinPos.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

	for (Vector2 vertex : vertices) {
		if (vertex.x < mMinPos.x) {
			mMinPos.x = vertex.x;
		}
		if (vertex.y < mMinPos.y) {
			mMinPos.y = vertex.y;
		}
		if (vertex.x > mMaxPos.x) {
			mMaxPos.x = vertex.x;
		}
		if (vertex.y > mMaxPos.y) {
			mMaxPos.y = vertex.y;
		}
	}
}

/**
 * Sets the AABB box from a line
 * @param pointA one point of the line
 * @param pointB the other point of the line
 */
public void setFromLine(Vector2 pointA, Vector2 pointB) {

}

/**
 * @return minimum position of the AABB box
 */
public Vector2 getMinPos() {
	return mMinPos;
}

/**
 * @return maximum positino of the AABB box
 */
public Vector2 getMaxPos() {
	return mMaxPos;
}
}
