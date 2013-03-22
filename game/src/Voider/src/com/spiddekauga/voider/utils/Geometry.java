package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Various geometry help functions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Geometry {
	/**
	 * Checks if two lines intersects, but skips the vertex points of the lines (i.e. if
	 * a point is the same, it simply skips the calculation. Note that the lines might still
	 * intersect, e.g. LINE1: (0,0) - (5,0), LINE2: (0,0) - (2,0).
	 * @param line1a first vertex point for line 1
	 * @param line1b second vertex point for line 1
	 * @param line2a first vertex point for line 2
	 * @param line2b second vertex point for line 2
	 * @return true if the lines intersect within the lines' range.
	 * 
	 * @see #linesIntersect(Vector2,Vector2,Vector2,Vector2)
	 * @see #getLineLineIntersection(Vector2, Vector2, Vector2, Vector2) to get the line intersection
	 */
	public static boolean linesIntersectNoCorners(Vector2 line1a, Vector2 line1b, Vector2 line2a, Vector2 line2b) {
		if (line1a.equals(line2a) || line1a.equals(line2b) ||
				line1b.equals(line2a) || line1b.equals(line2b))
		{
			return false;
		}

		return linesIntersect(line1a, line1b, line2a, line2b);
	}

	/**
	 * Checks if two lines intersects. They only intersect if the intersection
	 * point is within the lines' range (i.e. these are not infinite lines)
	 * @param line1a first vertex point for line 1
	 * @param line1b second vertex point for line 1
	 * @param line2a first vertex point for line 2
	 * @param line2b second vertex point for line 2
	 * @return true if the lines intersect within the lines' range.
	 * 
	 * @author CommanderKeith on http://Java-Gaming.org
	 * 
	 * @see #linesIntersectNoCorners(Vector2,Vector2,Vector2,Vector2)
	 * @see #getLineLineIntersection(Vector2, Vector2, Vector2, Vector2) to get the point of intersection
	 */
	public static boolean linesIntersect(Vector2 line1a, Vector2 line1b, Vector2 line2a, Vector2 line2b) {
		// Return false if either of the lines have zero length
		if ((line1a.x == line1b.x && line1a.y == line1b.y) ||
				(line2a.x == line2b.x && line2a.y == line2b.y)){
			return false;
		}
		// Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
		float ax = line1b.x-line1a.x;
		float ay = line1b.y-line1a.y;
		float bx = line2a.x-line2b.x;
		float by = line2a.y-line2b.y;
		float cx = line1a.x-line2a.x;
		float cy = line1a.y-line2a.y;

		float alphaNumerator = by*cx - bx*cy;
		float commonDenominator = ay*bx - ax*by;
		if (commonDenominator > 0){
			if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
				return false;
			}
		}else if (commonDenominator < 0){
			if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
				return false;
			}
		}
		float betaNumerator = ax*cy - ay*cx;
		if (commonDenominator > 0){
			if (betaNumerator < 0 || betaNumerator > commonDenominator){
				return false;
			}
		}else if (commonDenominator < 0){
			if (betaNumerator > 0 || betaNumerator < commonDenominator){
				return false;
			}
		}
		if (commonDenominator == 0){
			// This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
			// The lines are parallel.
			// Check if they're collinear.
			float y3LessY1 = line2a.y-line1a.y;
			float collinearityTestForP3 = line1a.x*(line1b.y-line2a.y) + line1b.x*(y3LessY1) + line2a.x*(line1a.y-line1b.y);   // see http://mathworld.wolfram.com/Collinear.html
			// If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
			if (collinearityTestForP3 == 0){
				// The lines are collinear. Now check if they overlap.
				if (line1a.x >= line2a.x && line1a.x <= line2b.x || line1a.x <= line2a.x && line1a.x >= line2b.x ||
						line1b.x >= line2a.x && line1b.x <= line2b.x || line1b.x <= line2a.x && line1b.x >= line2b.x ||
						line2a.x >= line1a.x && line2a.x <= line1b.x || line2a.x <= line1a.x && line2a.x >= line1b.x){
					if (line1a.y >= line2a.y && line1a.y <= line2b.y || line1a.y <= line2a.y && line1a.y >= line2b.y ||
							line1b.y >= line2a.y && line1b.y <= line2b.y || line1b.y <= line2a.y && line1b.y >= line2b.y ||
							line2a.y >= line1a.y && line2a.y <= line1b.y || line2a.y <= line1a.y && line2a.y >= line1b.y){
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Calculates the point of intersection between two lines. Note that the lines
	 * are treated as infinite, so the lines don't have to intersect.
	 * @param line1Start starting position of line 1
	 * @param line1End end position of line 1
	 * @param line2Start starting position of line 2
	 * @param line2End end position of line 2
	 * @return point of intersection, null if lines are parallel.
	 * 
	 * @author CommanderKeith on http://Java-Gaming.org
	 *
	 * @see #linesIntersect(Vector2, Vector2, Vector2, Vector2) if the point is actually inside the lines
	 * @see #linesIntersectNoCorners(Vector2, Vector2, Vector2, Vector2)
	 */
	public static Vector2 getLineLineIntersection(Vector2 line1Start, Vector2 line1End, Vector2 line2Start, Vector2 line2End) {
		float det1And2 = det(line1Start, line1End);
		float det3And4 = det(line2Start, line2End);
		float x1LessX2 = line1Start.x - line1End.x;
		float y1LessY2 = line1Start.y - line1End.y;
		float x3LessX4 = line2Start.x - line2End.x;
		float y3LessY4 = line2Start.y - line2End.y;
		float det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
		if (det1Less2And3Less4 == 0){
			// the denominator is zero so the lines are parallel and there's either no solution (or multiple solutions if the lines overlap) so return null.
			return null;
		}
		float x = (det(det1And2, x1LessX2, det3And4, x3LessX4) / det1Less2And3Less4);
		float y = (det(det1And2, y1LessY2, det3And4, y3LessY4) / det1Less2And3Less4);
		return Vector2Pool.obtain().set(x, y);
	}

	/**
	 * Calculates the determinant between two vectors
	 * @param vectorA first vector
	 * @param vectorB second vector
	 * @return determinant between two vectors
	 * 
	 * @author CommanderKeith on http://Java-Gaming.org
	 * 
	 * @see #det(float, float, float, float)
	 */
	protected static float det(Vector2 vectorA, Vector2 vectorB) {
		return det(vectorA.x, vectorA.y, vectorB.x, vectorB.y);
	}

	/**
	 * Calculates the determinant between two vectors
	 * @param x1 x value of vector 1
	 * @param y1 y value of vector 1
	 * @param x2 x value of vector 2
	 * @param y2 y value of vector 2
	 * @return determinant between two vectors
	 * 
	 * @author CommanderKeith on http://Java-Gaming.org
	 * 
	 * @see #det(Vector2, Vector2)
	 */
	protected static float det(float x1, float y1, float x2, float y2) {
		return x1 * y2 - y1 * x2;
	}

	/**
	 * Makes a polygon counter-clockwise if it isn't
	 * @param vertices list of vertices for the polygon
	 */
	public static void makePolygonCounterClockwise(List<Vector2> vertices) {
		// Reverse order of vertices
		if (!isPolygonCounterClockwise(vertices)) {
			Collections.reverse(vertices);
		}
	}

	/**
	 * Checks if the vertices of a polygon are counter-clockwise
	 * @param vertices all vertices of the polygon
	 * @return true if the polygon is counter-clockwise
	 */
	public static boolean isPolygonCounterClockwise(final List<Vector2> vertices) {
		float area = 0;
		for (int i = 0; i < vertices.size(); i++) {
			final Vector2 p1 = vertices.get(i);
			final Vector2 p2 = vertices.get(computeNextIndex(vertices, i));
			area += p1.x * p2.y - p2.x * p1.y;
		}

		if (area < 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Computes the next index of an array, i.e. it wraps the index from back to front if needed.
	 * @param array the array to wrap
	 * @param index calculates the next index of this
	 * @return next index
	 */
	public static int computeNextIndex(final List<?> array, final int index) {
		return index == array.size() - 1 ? 0 : index + 1;
	}

	/**
	 * Computes the previous index of an array, i.e. it wraps the index from the front to back if needed.
	 * @param array the array to wrap
	 * @param index calculates the previous index of this
	 * @return previous index
	 */
	public static int computePreviousIndex(final List<?> array, final int index) {
		return index == 0 ? array.size() - 1 : index - 1;
	}

	/**
	 * Computes intercept direction of the target.
	 * @param objectPosition starting position of the object that shall be intercepted
	 * @param objectSpeed object speed
	 * @param targetPosition current position of the target
	 * @param targetVelocity current velocity of the target
	 * @return velocity of the object needed to intercept the object. Returns Vector2(NaN,NaN)
	 * if the object cannot intercept the target (because of speed).
	 * Be sure to free the returning variable using Vector2Pool.free(velocity);
	 */
	public static Vector2 interceptTarget(Vector2 objectPosition, float objectSpeed, Vector2 targetPosition, Vector2 targetVelocity) {
		Vector2 distanceVector = Vector2Pool.obtain();
		distanceVector.set(targetPosition).sub(objectPosition);
		float e = distanceVector.dot(distanceVector);
		float f = 2 * targetVelocity.dot(distanceVector);
		float g = (objectSpeed * objectSpeed) - targetVelocity.dot(targetVelocity);
		float t = (float) ((f + Math.sqrt((f * f) + 4 * g * e )) / (g * 2));

		Vector2 objectVelocity = Vector2Pool.obtain();
		objectVelocity.set(distanceVector).div(t).add(targetVelocity);

		Vector2Pool.free(distanceVector);

		return objectVelocity;
	}

	/**
	 * Creates a circle, or rather returns vertices that can be created as a circle.
	 * @param radius the radius of the circle
	 * @return array with the vertices of the circle, all vertices are created from Vector2Pool
	 */
	public static ArrayList<Vector2> createCircle(float radius) {
		ArrayList<Vector2> polygon = new ArrayList<Vector2>();

		int segments = calculateCircleSegments(radius);

		float angle = 2 * 3.1415926f / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		for (int i = 0; i < segments; i++) {
			Vector2 vertex = Vector2Pool.obtain();
			vertex.set(cx, cy);
			polygon.add(vertex);

			float temp = cx;
			cx = cos * cx - sin * cy;
			cy = sin * temp + cos * cy;
		}

		return polygon;
	}

	/**
	 * Calculates the number of segments a circle needs to be smooth.
	 * @param radius the radius of the circle
	 * @return number of segments of the circle
	 */
	public static int calculateCircleSegments(float radius) {
		return (int)(10 * (float)Math.cbrt(radius));
	}

	/**
	 * Calculates the direction between two points. Creates a new Vector2
	 * @param fromPoint from this point
	 * @param toPoint to this point
	 * @return normalized direction from lineA to lineB
	 */
	public static Vector2 getDirection(Vector2 fromPoint, Vector2 toPoint) {
		Vector2 direction = Vector2Pool.obtain();
		direction.set(toPoint).sub(fromPoint).nor();
		return direction;
	}

	/**
	 * Calculates the direction between two points. Reuses an old Vector2 instead
	 * of creating a new one.
	 * @param fromPoint from this point
	 * @param toPoint to this point
	 * @param direction sets the direction to this
	 * 
	 * @see #getDirection(Vector2, Vector2) instead creates a new Vector2 instead of reusing it
	 */
	public static void getDirection(Vector2 fromPoint, Vector2 toPoint, Vector2 direction) {
		direction.set(toPoint).sub(fromPoint).nor();
	}
}
