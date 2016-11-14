package com.spiddekauga.voider.resources;

import com.spiddekauga.utils.ShapeRendererEx;

/**
 * A resource that can be rendered when in editor mode
 *

 */
public interface IResourceEditorRender extends IResourceRenderOrder {
	/**
	 * Renders the resource
	 * @param shapeRenderer current shape renderer batch
	 */
	public void renderEditor(ShapeRendererEx shapeRenderer);
}
