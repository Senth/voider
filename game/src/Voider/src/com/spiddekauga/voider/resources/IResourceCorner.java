package com.spiddekauga.voider.resources;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

/**
 * Common interface for resources with corners (custom vertices)
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceCorner {
	/**
	 * Add another corner position to the back of the array
	 * @param corner a new corner that will be placed at the back
	 */
	public void addCorner(Vector2 corner);

	/**
	 * Add a corner in the specified index
	 * @param corner position of the corner to add
	 * @param index where in the list the corner will be added
	 */
	public void addCorner(Vector2 corner, int index);

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
	 */
	public void moveCorner(int index, Vector2 newPos);

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
}
