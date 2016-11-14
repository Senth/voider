package com.spiddekauga.voider.resources;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Renders a resource as a sprite

 */
public interface IResourceRenderSprite extends IResource {
	/**
	 * Renders resource sprites
	 * @param spriteBatch batch used for rendering sprites
	 */
	void renderSprite(SpriteBatch spriteBatch);
}
