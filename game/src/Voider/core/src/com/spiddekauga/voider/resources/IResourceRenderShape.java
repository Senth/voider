package com.spiddekauga.voider.resources;

import com.spiddekauga.utils.ShapeRendererEx;

/**
 * A Resource that can be rendered
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IResourceRenderShape extends IResourceRenderOrder {
	/**
	 * Renders the resource as a shape
	 * @param shapeRenderer current shape renderer batch
	 */
	public void renderShape(ShapeRendererEx shapeRenderer);
}
