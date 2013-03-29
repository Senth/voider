package com.spiddekauga.voider.resources;

import com.spiddekauga.utils.ShapeRendererEx;

/**
 * A resource that can be rendered when in editor mode
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceEditorRender {
	/**
	 * Renders the resource
	 * @param shapeRenderer current shape renderer batch
	 */
	public void renderEditor(ShapeRendererEx shapeRenderer);
}
