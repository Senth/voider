package com.spiddekauga.voider.utils;

import java.util.Collections;
import java.util.List;

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
	 */
	public static boolean linesIntersect(Vector2 line1a, Vector2 line1b, Vector2 line2a, Vector2 line2b) {
		// Return false if either of the lines have zero length
		if ((line1a.x == line1b.x && line1a.y == line1b.y) ||
				(line2a.x == line2b.x && line2a.y == line2b.y)){
			return false;
		}
		// Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
		double ax = line1b.x-line1a.x;
		double ay = line1b.y-line1a.y;
		double bx = line2a.x-line2b.x;
		double by = line2a.y-line2b.y;
		double cx = line1a.x-line2a.x;
		double cy = line1a.y-line2a.y;

		double alphaNumerator = by*cx - bx*cy;
		double commonDenominator = ay*bx - ax*by;
		if (commonDenominator > 0){
			if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
				return false;
			}
		}else if (commonDenominator < 0){
			if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
				return false;
			}
		}
		double betaNumerator = ax*cy - ay*cx;
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
			double y3LessY1 = line2a.y-line1a.y;
			double collinearityTestForP3 = line1a.x*(line1b.y-line2a.y) + line1b.x*(y3LessY1) + line2a.x*(line1a.y-line1b.y);   // see http://mathworld.wolfram.com/Collinear.html
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
	 * Computes the next index of a polygon, i.e. it wraps the index from back to front if needed.
	 * @param vertices list of vertices for teh polygon
	 * @param index the index of the vertex
	 * @return next index for the index
	 */
	public static int computeNextIndex(final List<Vector2> vertices, final int index) {
		return index == vertices.size() - 1 ? 0 : index + 1;
	}

	/**
	 * Computes intercept direction of the target.
	 * @param objectPosition starting position of the object that shall intercetp
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
}
