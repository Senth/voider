package com.spiddekauga.voider.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.badlogic.gdx.math.Vector2;

/**
 * Testing Geometry class
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GeometryTest {

	/**
	 * Tests intersection of lines.
	 */
	@Test
	public void linesIntersect() {
		// Lines are parallel
		Vector2 line1a = new Vector2(0, 0);
		Vector2 line1b = new Vector2(5, 0);
		Vector2 line2a = new Vector2(2, 4);
		Vector2 line2b = new Vector2(4, 4);
		assertTrue("lines parallel, no intersection", !Geometry.linesIntersect(line1a, line1b, line2a, line2b));
		assertTrue("lines parallel, no intersection", !Geometry.linesIntersectNoCorners(line1a, line1b, line2a, line2b));


		// Lines would intersect if length is unlimited
		line2a.set(3, 5);
		line2b.set(1, 5);
		assertTrue("lines would intersect if infinite, no intersection", !Geometry.linesIntersect(line1a, line1b, line2a, line2b));
		assertTrue("lines would intersect if infinite, no intersection", !Geometry.linesIntersectNoCorners(line1a, line1b, line2a, line2b));


		// Lines intersect in a point
		line2a.set(0, 0);
		line2b.set(0, 5);
		assertTrue("intersection at a point, intersects", Geometry.linesIntersect(line1a, line1b, line2a, line2b));
		assertTrue("intersection at a point, no intersection", !Geometry.linesIntersectNoCorners(line1a, line1b, line2a, line2b));


		// Lines intersect
		line2a.set(2, 3);
		line2b.set(2, -3);
		assertTrue("lines intersect", Geometry.linesIntersect(line1a, line1b, line2a, line2b));
		assertTrue("lines intersect", Geometry.linesIntersectNoCorners(line1a, line1b, line2a, line2b));
	}

}
