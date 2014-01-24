package com.spiddekauga.voider.resources;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

/**
 * Common interface for resources with corners (custom vertices)
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceCorner extends IResource {
	/**
	 * Add another corner position to the back of the array
	 * @param corner a new corner that will be placed at the back
	 */
	void addCorner(Vector2 corner);

	/**
	 * Add a corner in the specified index
	 * @param corner position of the corner to add
	 * @param index where in the list the corner will be added
	 */
	void addCorner(Vector2 corner, int index);

	/**
	 * Removes a corner with the specific id.
	 * @param index the corner to remove
	 * @return position of the corner we removed, null if none was removed
	 */
	Vector2 removeCorner(int index);

	/**
	 * Moves a corner, identifying the corner from index
	 * @param index index of the corner to move
	 * @param newPos new position of the corner
	 */
	void moveCorner(int index, Vector2 newPos);

	/**
	 * @return number of corners in this resource.
	 */
	int getCornerCount();

	/**
	 * @param index the index we want to get the corner from
	 * @return corner position of the specified index
	 */
	Vector2 getCornerPosition(int index);

	/**
	 * Gets the index of the corner at the exact specified location
	 * @param position get the index of the corner at this position
	 * @return index of the corner at the specified position -1 if no
	 * corner was found on this exact position.
	 */
	int getCornerIndex(Vector2 position);

	/**
	 * @return all the corners of the resource
	 */
	ArrayList<Vector2> getCorners();

	/**
	 * Creates body corners for the resource
	 */
	void createBodyCorners();

	/**
	 * Destroys all body corners
	 */
	void destroyBodyCorners();

	/**
	 * Removes all the corners
	 */
	void clearCorners();
}
