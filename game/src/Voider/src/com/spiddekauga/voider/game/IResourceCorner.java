package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.resources.IResource;

/**
 * Common interface for resources with corners (custom vertices)
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceCorner extends IResource {
	/**
	 * Add another corner position to the back of the array
	 * @param corner a new corner that will be placed at the back
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 * @throws PolygonCornerTooCloseException thrown when a corner is too close to
	 * another corner inside the polygon.
	 */
	public void addCorner(Vector2 corner) throws PolygonComplexException, PolygonCornerTooCloseException;

	/**
	 * Add a corner in the specified index
	 * @param corner position of the corner to add
	 * @param index where in the list the corner will be added
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 * @throws PolygonCornerTooCloseException thrown when a corner is too close to
	 * another corner inside the polygon.
	 */
	public void addCorner(Vector2 corner, int index) throws PolygonComplexException, PolygonCornerTooCloseException;

	/**
	 * Removes a corner with the specific id.
	 * @param index the corner to remove
	 * @return position of the corner we removed, null if none was removed
	 */
	public Vector2 removeCorner(int index);

	/**
	 * Moves a corner, identifying the corner from index
	 * @param index index of the corner to move
	 * @param newPos new position of the corner
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 * @throws PolygonCornerTooCloseException thrown when a corner is too close to
	 * another corner inside the polygon.
	 */
	public void moveCorner(int index, Vector2 newPos) throws PolygonComplexException, PolygonCornerTooCloseException;

	/**
	 * @return number of corners in this resource.
	 */
	public int getCornerCount();

	/**
	 * @param index the index we want to get the corner from
	 * @return corner position of the specified index
	 */
	public Vector2 getCornerPosition(int index);

	/**
	 * @return all the corners of the resource
	 */
	public ArrayList<Vector2> getCorners();

	/**
	 * Exception class for when trying to create a new, or move an existing corner
	 * and this makes the polygon complex, i.e. it intersects with itself.
	 */
	public class PolygonComplexException extends Exception {
		/** for serialization */
		private static final long serialVersionUID = -2564535357356811708L;
	}

	/**
	 * Exception class for when a triangle of the polygon would make too
	 * small area.
	 */
	public class PolygonCornerTooCloseException extends Exception {
		/** For serialization */
		private static final long serialVersionUID = 5402912928691451496L;
	}
}
