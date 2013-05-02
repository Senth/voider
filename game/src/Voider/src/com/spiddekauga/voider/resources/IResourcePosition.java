package com.spiddekauga.voider.resources;

import com.badlogic.gdx.math.Vector2;

/**
 * If the resource has a position
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
}
