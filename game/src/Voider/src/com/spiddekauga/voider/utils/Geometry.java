package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.Gdx;
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
		return Pools.vector2.obtain().set(x, y);
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
	 * Be sure to free the returning variable using Pools.vector2.free(velocity);
	 */
	public static Vector2 interceptTarget(Vector2 objectPosition, float objectSpeed, Vector2 targetPosition, Vector2 targetVelocity) {
		Vector2 distanceVector = Pools.vector2.obtain();
		distanceVector.set(targetPosition).sub(objectPosition);
		float e = distanceVector.dot(distanceVector);
		float f = 2 * targetVelocity.dot(distanceVector);
		float g = (objectSpeed * objectSpeed) - targetVelocity.dot(targetVelocity);
		float t = (float) ((f + Math.sqrt((f * f) + 4 * g * e )) / (g * 2));

		Vector2 objectVelocity = Pools.vector2.obtain();
		objectVelocity.set(distanceVector).div(t).add(targetVelocity);

		Pools.vector2.free(distanceVector);

		return objectVelocity;
	}

	/**
	 * Creates a circle, or rather returns vertices that can be created as a circle.
	 * @param radius the radius of the circle
	 * @return array with the vertices of the circle, all vertices are created from Vector2Pool
	 */
	public static ArrayList<Vector2> createCircle(float radius) {
		@SuppressWarnings("unchecked")
		ArrayList<Vector2> polygon = Pools.arrayList.obtain();
		polygon.clear();

		int segments = calculateCircleSegments(radius);

		float angle = 2 * 3.1415926f / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		for (int i = 0; i < segments; i++) {
			Vector2 vertex = Pools.vector2.obtain();
			vertex.set(cx, cy);
			polygon.add(vertex);

			float temp = cx;
			cx = cos * cx - sin * cy;
			cy = sin * temp + cos * cy;
		}

		return polygon;
	}

	/**
	 * Creates a polygon line, i.e. a line made out of triangles
	 * @param corners all the positions of the line
	 * @param width width of the line
	 * @return list with all the vertices for the line. These have been created with
	 * Vector2Pool, make sure to free them afterwards :)
	 */
	public static ArrayList<Vector2> createLinePolygon(ArrayList<Vector2> corners, float width) {
		// Do nothing if only one or zero corners. Cannot draw a line from this...
		if (corners.size() < 2) {
			return null;
		}

		@SuppressWarnings("unchecked")
		ArrayList<Vector2> vertices = Pools.arrayList.obtain();
		vertices.clear();

		Vector2 directionBefore = Pools.vector2.obtain();
		Vector2 directionAfter = Pools.vector2.obtain();

		Vector2 borderAboveBefore1 = Pools.vector2.obtain();
		Vector2 borderAboveBefore2 = Pools.vector2.obtain();
		Vector2 borderAboveAfter1 = Pools.vector2.obtain();
		Vector2 borderAboveAfter2 = Pools.vector2.obtain();
		Vector2 borderBelowBefore1 = Pools.vector2.obtain();
		Vector2 borderBelowBefore2 = Pools.vector2.obtain();
		Vector2 borderBelowAfter1 = Pools.vector2.obtain();
		Vector2 borderBelowAfter2 = Pools.vector2.obtain();

		for (int i = 0; i < corners.size(); ++i) {
			int nextIndex = computeNextIndex(corners, i);
			int prevIndex = computePreviousIndex(corners, i);

			// First position only takes into account the forward direction
			if (i == 0) {
				directionAfter.set(corners.get(nextIndex)).sub(corners.get(i));
				directionAfter.rotate(90).nor().scl(width * 0.5f);

				borderAboveAfter1.set(corners.get(i)).add(directionAfter);
				borderBelowAfter1.set(corners.get(i)).sub(directionAfter);
				vertices.add(Pools.vector2.obtain().set(borderAboveAfter1));
				vertices.add(Pools.vector2.obtain().set(borderBelowAfter1));
			}
			// Last position only takes into account the previous direction
			else if (i == corners.size() - 1) {
				directionBefore.set(corners.get(i)).sub(corners.get(prevIndex));
				directionBefore.rotate(90).nor().scl(width * 0.5f);

				borderAboveBefore2.set(corners.get(i)).add(directionBefore);
				borderBelowBefore2.set(corners.get(i)).sub(directionBefore);
				vertices.add(Pools.vector2.obtain().set(borderAboveBefore2));
				vertices.add(Pools.vector2.obtain().set(borderBelowBefore2));
			}
			// The rest uses both forward and backward directions to calculate
			// the intersection of these
			else {
				// Before lines
				directionBefore.set(corners.get(i)).sub(corners.get(prevIndex));
				directionBefore.rotate(90).nor().scl(width * 0.5f);

				borderAboveBefore1.set(corners.get(prevIndex)).add(directionBefore);
				borderAboveBefore2.set(corners.get(i)).add(directionBefore);
				borderBelowBefore1.set(corners.get(prevIndex)).sub(directionBefore);
				borderBelowBefore2.set(corners.get(i)).sub(directionBefore);

				// After lines
				directionAfter.set(corners.get(nextIndex)).sub(corners.get(i));
				directionAfter.rotate(90).nor().scl(width * 0.5f);

				borderAboveAfter1.set(corners.get(i)).add(directionAfter);
				borderAboveAfter2.set(corners.get(nextIndex)).add(directionAfter);
				borderBelowAfter1.set(corners.get(i)).sub(directionAfter);
				borderBelowAfter2.set(corners.get(nextIndex)).add(directionAfter);

				// Calculate intersection lines
				Vector2 aboveIntersection = getLineLineIntersection(borderAboveBefore1, borderAboveBefore2, borderAboveAfter1, borderAboveAfter2);
				Vector2 belowIntersection = getLineLineIntersection(borderBelowBefore1, borderBelowBefore2, borderBelowAfter1, borderBelowAfter2);

				if (aboveIntersection != null && belowIntersection != null) {
					vertices.add(aboveIntersection);
					vertices.add(belowIntersection);
				} else {
					Gdx.app.error("Geometry", "No intersection for line when creating polygon line!");
					return null;
				}
			}
		}

		@SuppressWarnings("unchecked")
		ArrayList<Vector2> triangles = Pools.arrayList.obtain();
		triangles.clear();

		// Create triangles from the positions
		if (Geometry.isPolygonCounterClockwise(corners)) {
			for (int i = 0; i < vertices.size() - 2; ++i) {
				// Even - counter clockwise use correct order
				if ((i & 1) == 0) {
					triangles.add(vertices.get(i));
					triangles.add(vertices.get(i+1));
					triangles.add(vertices.get(i+2));
				}
				// Odd - clockwise, use different order
				else {
					triangles.add(vertices.get(i));
					triangles.add(vertices.get(i+2));
					triangles.add(vertices.get(i+1));
				}
			}
		} else {
			for (int i = 0; i < vertices.size() - 2; ++i) {
				// Even - clockwise, use different order
				if ((i & 1) == 0) {
					triangles.add(vertices.get(i));
					triangles.add(vertices.get(i+2));
					triangles.add(vertices.get(i+1));
				}
				// Odd - counter clockwise use correct order
				else {
					triangles.add(vertices.get(i));
					triangles.add(vertices.get(i+1));
					triangles.add(vertices.get(i+2));
				}
			}
		}

		Pools.arrayList.free(vertices);

		Pools.vector2.free(directionBefore);
		Pools.vector2.free(directionAfter);

		Pools.vector2.free(borderAboveBefore1);
		Pools.vector2.free(borderAboveBefore2);
		Pools.vector2.free(borderAboveAfter1);
		Pools.vector2.free(borderAboveAfter2);
		Pools.vector2.free(borderBelowBefore1);
		Pools.vector2.free(borderBelowBefore2);
		Pools.vector2.free(borderBelowAfter1);
		Pools.vector2.free(borderBelowAfter2);

		return triangles;
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
		Vector2 direction = Pools.vector2.obtain();
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

	/**
	 * Checks whether the specified point is within the triangle. Uses Barycentric coordinates.
	 * @param point the point to test whether it inside the triangle or not
	 * @param triangle three vertices that makes triangle
	 * @param triangleStartIndex starting index of the triangle this can be used when the triangle already
	 * is within another larger list.
	 * @return true if the point is inside the triangle
	 * @author andreasdr http://stackoverflow.com/questions/2049582/how-to-determine-a-point-in-a-triangle
	 */
	public static boolean isPointWithinTriangle(final Vector2 point, final ArrayList<Vector2> triangle, int triangleStartIndex) {
		int index0 = triangleStartIndex;
		int index1 = triangleStartIndex + 1;
		int index2 = triangleStartIndex + 2;

		// s = p0y*p2x - p0x*p2y + (p2y - p0y)*px + (p0x - p2x)*py;
		// t = p0x*p1y - p0y*p1x + (p0y - p1y)*px + (p1x - p0x)*py;
		float s = triangle.get(index0).y * triangle.get(index2).x - triangle.get(index0).x * triangle.get(index2).y +
				(triangle.get(index2).y - triangle.get(index0).y) * point.x +
				(triangle.get(index0).x - triangle.get(index2).x) * point.y;

		// Fast return
		if (s < 0) {
			return false;
		}

		float t = triangle.get(index0).x * triangle.get(index1).y - triangle.get(index0).y * triangle.get(index1).x +
				(triangle.get(index0).y - triangle.get(index1).y) * point.x +
				(triangle.get(index1).x - triangle.get(index0).x) * point.y;

		// Fast return
		if (t < 0) {
			return false;
		}

		// Calculate the area
		// A = 1/2*(-p1y*p2x + p0y*(p2x - p1x) + p0x*(p1y - p2y) + p1x*p2y);
		float area = 0.5f * (-triangle.get(index1).y * triangle.get(index2).x +
				triangle.get(index0).y * (triangle.get(index2).x - triangle.get(index1).x) +
				triangle.get(index0).x * (triangle.get(index1).y - triangle.get(index2).y) +
				triangle.get(index1).x * triangle.get(index2).y);

		if (area < 0) {
			area = -area;
		}

		// Check for 1 - s - t > 0. But as area is not included in s and t, we check against
		// s' + t' < 2*area
		return s + t < 2 * area;
	}

	/**
	 * Checks whether the specified point is within the triangle. Uses Barycentric coordinates.
	 * @param point the point to test whether it inside the triangle or not
	 * @param triangle three vertices that makes triangle
	 * @return true if the point is inside the triangle
	 * @author andreasdr http://stackoverflow.com/questions/2049582/how-to-determine-a-point-in-a-triangle
	 */
	public static boolean isPointWithinTriangle(final Vector2 point, final ArrayList<Vector2> triangle) {
		return isPointWithinTriangle(point, triangle, 0);
	}

	/**
	 * Creates the border vertices for the corner
	 * @param corners vertices for all the corners
	 * @param inward set this to true if the border shall be inward, false if outwards
	 * @param width the border width
	 * @return border corner vertices
	 */
	public static ArrayList<Vector2> createdBorderCorners(ArrayList<Vector2> corners, boolean inward, float width) {
		boolean clockwise = !Geometry.isPolygonCounterClockwise(corners);
		if (!inward) {
			clockwise = !clockwise;
		}

		Vector2 directionBefore = Pools.vector2.obtain();
		Vector2 directionAfter = Pools.vector2.obtain();

		Vector2 borderBefore1 = Pools.vector2.obtain();
		Vector2 borderBefore2 = Pools.vector2.obtain();
		Vector2 borderAfter1 = Pools.vector2.obtain();
		Vector2 borderAfter2 = Pools.vector2.obtain();

		ArrayList<Vector2> borderCorners = new ArrayList<Vector2>();
		for (int i = 0; i < corners.size(); ++i) {
			// Get direction of lines that uses this vertex (i.e. that has it
			// as its end (line before) or start (line after) position.
			int indexBefore = Geometry.computePreviousIndex(corners, i);
			int indexAfter = Geometry.computeNextIndex(corners, i);
			Geometry.getDirection(corners.get(indexBefore), corners.get(i), directionBefore);
			Geometry.getDirection(corners.get(i), corners.get(indexAfter), directionAfter);

			// Rotate direction to point inwards into the polygon
			if (clockwise) {
				directionBefore.rotate(-90);
				directionAfter.rotate(-90);
			} else {
				directionBefore.rotate(90);
				directionAfter.rotate(90);
			}

			// Border width
			directionBefore.scl(width);
			directionAfter.scl(width);

			// Calculate temporary border points
			borderBefore1.set(corners.get(indexBefore)).add(directionBefore);
			borderBefore2.set(corners.get(i)).add(directionBefore);
			borderAfter1.set(corners.get(i)).add(directionAfter);
			borderAfter2.set(corners.get(indexAfter)).add(directionAfter);

			// Get intersection point add it as a corner
			Vector2 borderCorner = Geometry.getLineLineIntersection(borderBefore1, borderBefore2, borderAfter1, borderAfter2);
			if (borderCorner != null) {
				borderCorners.add(borderCorner);
			} else {
				Gdx.app.error("ActorDef", "No intersection for the border corner!");
			}
		}

		Pools.vector2.free(directionBefore);
		Pools.vector2.free(directionAfter);

		Pools.vector2.free(borderBefore1);
		Pools.vector2.free(borderBefore2);
		Pools.vector2.free(borderAfter1);
		Pools.vector2.free(borderAfter2);

		return borderCorners;
	}

	/**
	 * Creates border vertices that will be in pair of triangles to be easily
	 * rendered.
	 * @param corners the regular corners of the actor
	 * @param borderCorners the border corners of the actor
	 * @return vector with all border vertices, null if corner size and border corner size isn't the same
	 */
	public static ArrayList<Vector2> createBorderVertices(ArrayList<Vector2> corners, ArrayList<Vector2> borderCorners) {
		if (corners.size() != borderCorners.size()) {
			Gdx.app.error("Geometry", "Not same amount of border corners as there are corners!");
			return null;
		}

		ArrayList<Vector2> vertices = new ArrayList<Vector2>();

		// Create the two triangle in front of the index
		// For example if we're at index 1 we will create the triangles
		// 1 - 2 - 2' AND 1 - 2' - 1'
		// 0' 1' 2' 3' = borders corners
		// ——————————
		// | /| /| /|
		// |/ |/ |/ |
		// ——————————
		// 0  1  2  3 = corners

		if (Geometry.isPolygonCounterClockwise(corners)) {
			for (int i = 0; i < corners.size(); ++i) {
				int nextIndex = Geometry.computeNextIndex(corners, i);

				// First triangle
				vertices.add(corners.get(i));
				vertices.add(corners.get(nextIndex));
				vertices.add(borderCorners.get(nextIndex));

				// Second triangle
				vertices.add(corners.get(i));
				vertices.add(borderCorners.get(nextIndex));
				vertices.add(borderCorners.get(i));
			}
		} else {
			for (int i = 0; i < corners.size(); ++i) {
				int nextIndex = Geometry.computeNextIndex(corners, i);

				// First triangle
				vertices.add(corners.get(i));
				vertices.add(borderCorners.get(nextIndex));
				vertices.add(corners.get(nextIndex));

				// Second triangle
				vertices.add(corners.get(i));
				vertices.add(borderCorners.get(i));
				vertices.add(borderCorners.get(nextIndex));
			}
		}

		return vertices;
	}

	/**
	 * Rotates all vertices
	 * @param vertices the vertices to rotate
	 * @param degrees how many degrees to rotate the vertices
	 * @param containsReferences set to true if the vertices contains references
	 * thus we don't have to recalculate all vertices
	 */
	public static void rotateVertices(ArrayList<Vector2> vertices, float degrees, boolean containsReferences) {
		if (containsReferences) {
			@SuppressWarnings("unchecked")
			HashSet<Vector2> rotatedVertices = Pools.hashSet.obtain();
			rotatedVertices.clear();
			for (Vector2 vertex : vertices) {
				if (!rotatedVertices.contains(vertex)) {
					vertex.rotate(degrees);
					rotatedVertices.add(vertex);
				}
			}
			Pools.hashSet.free(rotatedVertices);
		} else {
			for (Vector2 vertex : vertices) {
				vertex.rotate(degrees);
			}
		}
	}

	/**
	 * Moves all the vertices. If the array duplicates of same vertex set containReferences to true
	 * @param vertices all vertices to move
	 * @param offset the offset to move all vertices with
	 * @param containsReferences true if the array contains duplicates of the same vertex reference
	 */
	public static void moveVertices(ArrayList<Vector2> vertices, Vector2 offset, boolean containsReferences) {
		if (containsReferences) {
			@SuppressWarnings("unchecked")
			HashSet<Vector2> movedVertices = Pools.hashSet.obtain();
			movedVertices.clear();
			for (Vector2 vertex : vertices) {
				if (!movedVertices.contains(vertex)) {
					vertex.add(offset);
					movedVertices.add(vertex);
				}
			}
		} else {
			for (Vector2 vertex : vertices) {
				vertex.add(offset);
			}
		}
	}

	/**
	 * Calculates the vertex farthest away from the specified point
	 * @param point the point to check the vertices against
	 * @param vertices the vertices to check
	 * @return vertex farthest away from the specified point, if vertices are empty
	 * a null vertex will be returned.
	 */
	public static Vector2 vertexFarthestAway(Vector2 point, ArrayList<Vector2> vertices) {
		Vector2 farthestAway = null;
		float longestSquareDistance = 0;

		Vector2 diffVector = Pools.vector2.obtain();

		for (Vector2 vertex : vertices) {
			diffVector.set(point).sub(vertex);
			float squaredDistance = diffVector.len2();

			if (squaredDistance > longestSquareDistance) {
				longestSquareDistance = squaredDistance;
				farthestAway = vertex;
			}
		}

		Pools.vector2.free(diffVector);

		return farthestAway;
	}

	/**
	 * Makes a polygon non-complex, i.e. it will create new vertices so that it doesn't intersect
	 * itself.
	 * @param vertices all the corner vertices of the polygon, this array will be modified if
	 * an intersection exists
	 * @return newly created vertices, these have been created from Pools.vector2 be sure to free
	 * them after their use. Same goes for the ArrayList. Null if no vertices were created.
	 */
	public static ArrayList<Vector2> makePolygonNonComplex(ArrayList<Vector2> vertices) {
		@SuppressWarnings("unchecked")
		ArrayList<Vector2> newVertices = Pools.arrayList.obtain();
		newVertices.clear();


		for (int i = 0; i < vertices.size(); ++i) {
			int intersectionIndex = intersectionExists(vertices, i, i);

			if (intersectionIndex != -1) {
				Vector2 line1a = vertices.get(i);
				Vector2 line1b = vertices.get(computeNextIndex(vertices, i));
				Vector2 line2a = vertices.get(intersectionIndex);
				Vector2 line2b = vertices.get(computeNextIndex(vertices, intersectionIndex));
				Vector2 intersectionPoint = getLineLineIntersection(line1a, line1b, line2a, line2b);
				newVertices.add(intersectionPoint);


				// Reorder...
				// If let say we have indices with 1 2 3 4 5 6 7 and there is an intersection
				// between 3-4 and 5-6 the new order will be (X is the intersectionPoint)
				// 1 2 3 X 5 4 X 6 7. The order is always reversed between i (3) and intersectionIndex + 1 (6).

				// First reverse
				for (int forwardIndex = i + 1, backwardIndex = intersectionIndex; forwardIndex < backwardIndex; ++forwardIndex, --backwardIndex) {
					Collections.swap(vertices, forwardIndex, backwardIndex);
				}

				// Add intersection point to the vertices
				vertices.add(computeNextIndex(vertices, intersectionIndex), intersectionPoint);
				vertices.add(computeNextIndex(vertices, i), intersectionPoint);
			}
		}

		if (newVertices.isEmpty()) {
			Pools.arrayList.free(newVertices);
			return null;
		} else {
			return newVertices;
		}
	}

	/**
	 * Checks if an intersection exists from a line that starts from the specified index
	 * @param vertices all vertices to check the intersection with
	 * @param lineIndex which vertex the line start from
	 * @param testFromIndex only tests from the specified index (lines before this index is not tested)
	 * @return index of the other line that intersects with lineIndex, -1 if no intersection
	 * exists.
	 */
	public static int intersectionExists(ArrayList<Vector2> vertices, int lineIndex, int testFromIndex) {
		if (vertices.size() < 3) {
			return -1;
		}

		if (lineIndex < 0 || lineIndex < testFromIndex || lineIndex >= vertices.size()) {
			throw new IndexOutOfBoundsException();
		}

		// Calculate start/end of line
		Vector2 lineStart = vertices.get(lineIndex);
		Vector2 lineEnd = vertices.get(computeNextIndex(vertices, lineIndex));


		for (int i = testFromIndex; i < vertices.size(); ++i) {
			// Skip checking with line that starts on lineIndex, as it is the same line...
			if (i == lineIndex) {
				continue;
			}

			Vector2 compareLineStart = vertices.get(i);
			Vector2 compareLineEnd = vertices.get(computeNextIndex(vertices, i));

			if (linesIntersectNoCorners(lineStart, lineEnd, compareLineStart, compareLineEnd)) {
				return i;
			}
		}

		return -1;
	}
}
