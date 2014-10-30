package com.spiddekauga.voider.resources;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Renders a resource as a sprite
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IResourceRenderSprite {
	/**
	 * Renders resource sprites
	 * @param spriteBatch batch used for rendering sprites
	 */
	void renderSprite(SpriteBatch spriteBatch);
}
