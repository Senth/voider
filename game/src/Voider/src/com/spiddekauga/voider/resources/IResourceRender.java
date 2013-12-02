package com.spiddekauga.voider.resources;

import com.spiddekauga.utils.ShapeRendererEx;

/**
 * A Resource that can be rendered
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceRender extends IResourceRenderOrder {
	/**
	 * Renders the resource
	 * @param shapeRenderer current shape renderer batch
	 */
	public void render(ShapeRendererEx shapeRenderer);
}
