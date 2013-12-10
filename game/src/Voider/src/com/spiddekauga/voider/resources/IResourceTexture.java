package com.spiddekauga.voider.resources;

import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * If the resource has a texture that shows how the definition
 * looks like.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceTexture extends IResource {
	/**
	 * @return texture for how the resource looks like
	 */
	TextureRegionDrawable getTextureRegionDrawable();
}
