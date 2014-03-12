package com.spiddekauga.voider.resources;

import com.badlogic.gdx.math.Vector2;

/**
 * If the resource has a position
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IResourcePosition extends IResource {
	/**
	 * Sets the position of the resource
	 * @param position new position of the resource
	 */
	void setPosition(Vector2 position);

	/**
	 * @return current position of the resource
	 */
	Vector2 getPosition();

	/**
	 * @return radius bounding area of the resource
	 */
	float getBoundingRadius();

	/**
	 * Set if the resource is currently being moved
	 * @param isBeingMoved set to true if it's being moved
	 */
	void setIsBeingMoved(boolean isBeingMoved);

	/**
	 * @return true if the resource is being moved
	 */
	boolean isBeingMoved();
}
