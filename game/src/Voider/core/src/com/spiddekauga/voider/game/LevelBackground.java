package com.spiddekauga.voider.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.spiddekauga.utils.scene.ui.ImageScroll;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Game;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * Level background
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LevelBackground {
	/**
	 * Create a level background from an internal name
	 * @param internalName this have to be loaded!
	 */
	public LevelBackground(InternalNames internalName) {
		if (!ResourceCacheFacade.isLoaded(internalName)) {
			throw new IllegalArgumentException("Resource not loaded!");
		}

		mName = internalName;


		// Get all Region names
		TextureAtlas atlas = ResourceCacheFacade.get(mName);

		mTopLayer = new ImageScroll(atlas, "top");
		mBottomLayer = new ImageScroll(atlas, "bottom");
	}


	/**
	 * Render the background layers on the entire screen
	 * @param spriteBatch
	 * @param offset x offset of the level
	 */
	public void render(SpriteBatch spriteBatch, float offset) {
		render(spriteBatch, offset, 0, Gdx.graphics.getHeight());
	}

	/**
	 * Render the background layers on a part of the screen (not the whole height but
	 * whole width of the screen)
	 * @param spriteBatch
	 * @param offset x offset of the level
	 * @param y y-coordinate to start rendering on
	 * @param height how high the strip should be
	 */
	public void render(SpriteBatch spriteBatch, float offset, int y, int height) {
		IC_Game icGame = ConfigIni.getInstance().game;

		// Bottom
		mBottomLayer.setScrollSpeed(icGame.getLayerBottomSpeed());
		mBottomLayer.render(spriteBatch, offset, y, height);

		// Top
		mTopLayer.setScrollSpeed(icGame.getLayerTopSpeed());
		mTopLayer.render(spriteBatch, offset, y, height);
	}

	/**
	 * @return dependency of the level background
	 */
	public InternalNames getDependency() {
		return mName;
	}

	/**
	 * @return top layer of the level background
	 */
	public ImageScroll getTopLayer() {
		return mTopLayer;
	}

	/**
	 * @return bottom layer of the level background
	 */
	public ImageScroll getBottomLayer() {
		return mBottomLayer;
	}

	private ImageScroll mTopLayer;
	private ImageScroll mBottomLayer;
	private InternalNames mName;
}
