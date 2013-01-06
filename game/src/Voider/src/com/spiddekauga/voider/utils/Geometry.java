package com.spiddekauga.voider.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Various geometry help functions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Geometry {
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
	 */
	public static boolean linesIntersects(Vector2 line1a, Vector2 line1b, Vector2 line2a, Vector2 line2b) {
		// Return false if either of the lines have zero length
		if (line1a.x == line1b.x && line1a.y == line1b.y ||
				line2a.x == line2b.x && line2a.y == line2b.y){
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
}
