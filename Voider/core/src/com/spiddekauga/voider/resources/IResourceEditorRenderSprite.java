package com.spiddekauga.voider.resources;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A resource that contains sprites to be rendered, but only in an editor
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IResourceEditorRenderSprite {
	/**
	 * Render sprites for the editor
	 * @param spriteBatch used for rendering sprites
	 */
	void renderEditorSprite(SpriteBatch spriteBatch);
}
