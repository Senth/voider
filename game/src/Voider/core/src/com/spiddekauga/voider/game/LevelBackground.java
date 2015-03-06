package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.spiddekauga.voider.Config;
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

		mTopLayer = new BackgroundLayer(atlas, "top");
		mBottomLayer = new BackgroundLayer(atlas, "bottom");

		calculateSplitSize();
	}

	/**
	 * Calculate split size
	 */
	private void calculateSplitSize() {
		TextureRegion topSprite = mTopLayer.mRegions.get(0).get(0);
		TextureRegion bottomSprite = mBottomLayer.mRegions.get(0).get(0);

		int largestValue = topSprite.getRegionWidth();
		if (topSprite.getRegionHeight() > largestValue) {
			largestValue = topSprite.getRegionHeight();
		}
		if (bottomSprite.getRegionWidth() > largestValue) {
			largestValue = bottomSprite.getRegionWidth();
		}
		if (bottomSprite.getRegionHeight() > largestValue) {
			largestValue = bottomSprite.getRegionHeight();
		}

		mSplitSize = largestValue;
	}


	/**
	 * Construct a region name from layer and coordinates
	 * @param layerName name of the layer
	 * @param x
	 * @param y
	 * @return region name
	 */
	private String constructRegionName(String layerName, int x, int y) {
		return layerName + "-" + y + "-" + x;
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
		mBottomLayer.render(spriteBatch, offset, icGame.getLayerBottomSpeed(), mSplitSize, y, height);

		// Top
		mTopLayer.render(spriteBatch, offset, icGame.getLayerTopSpeed(), mSplitSize, y, height);
	}

	/**
	 * @return dependency of the level background
	 */
	public InternalNames getDependency() {
		return mName;
	}

	private BackgroundLayer mTopLayer;
	private BackgroundLayer mBottomLayer;
	private InternalNames mName;
	private int mSplitSize;

	private class BackgroundLayer {
		/**
		 * Create a background layer
		 * @param atlas where to get the name from
		 * @param layerName name of the layer
		 */
		private BackgroundLayer(TextureAtlas atlas, String layerName) {
			int x = 0;
			int y = 0;

			boolean foundAllX = false;
			while (!foundAllX) {
				boolean foundAllY = false;
				y = 0;

				ArrayList<TextureRegion> yRegions = new ArrayList<>();

				while (!foundAllY) {
					TextureRegion region = atlas.findRegion(constructRegionName(layerName, x, y));

					if (region != null) {
						yRegions.add(region);
						y++;
					} else {
						foundAllY = true;
					}
				}

				if (!yRegions.isEmpty()) {
					mRegions.add(yRegions);
					x++;
				} else {
					foundAllX = true;
				}
			}

			// If 0 indexes something is wrong
			if (x == 0 || mRegions.size() == 0) {
				throw new IllegalStateException("Number of indexes is 0");
			}

			mXIndexes = x;
			mYIndexes = mRegions.get(0).size();


			// Calculate height
			ArrayList<TextureRegion> yRegions = mRegions.get(0);
			for (int i = 0; i < mYIndexes; ++i) {
				mHeight += yRegions.get(i).getRegionHeight();
			}


			// Calculate width
			for (int i = 0; i < mXIndexes; i++) {
				yRegions = mRegions.get(i);
				mWidth += yRegions.get(0).getRegionWidth();
			}
		}

		/**
		 * Render the layer
		 * @param spriteBatch
		 * @param offset
		 * @param layerSpeed
		 * @param splitSize
		 * @param startY where to start rendering
		 * @param renderHeight height of render
		 */
		private void render(SpriteBatch spriteBatch, float offset, float layerSpeed, int splitSize, int startY, int renderHeight) {
			float layerOffset = offset * layerSpeed;

			// Conversion to screen coordinates
			layerOffset /= Config.Graphics.WORLD_SCALE;

			// Texture scaling
			int screenWidth = Gdx.graphics.getWidth();
			float scale = ((float) renderHeight) / mHeight;
			float widthScaled = mWidth * scale;
			float splitSizeScaled = splitSize * scale;

			// Modulate so it's within the layer width
			float layerOffsetScaled = layerOffset % widthScaled;
			if (layerOffsetScaled < 0) {
				layerOffsetScaled += widthScaled;
			}

			int index = (int) (layerOffsetScaled / splitSizeScaled);
			float regionOffsetScaled = layerOffsetScaled % splitSizeScaled;
			int totalWidth = -(int) regionOffsetScaled;


			// Draw background
			while (totalWidth < screenWidth) {
				ArrayList<TextureRegion> yRegions = mRegions.get(index);

				int regionWidth = yRegions.get(0).getRegionWidth();
				float regionWidthScaled = regionWidth * scale;

				float drawWidth = (int) regionWidthScaled;
				if (drawWidth > screenWidth) {
					drawWidth = screenWidth;
				}

				// Draw call
				int yOffset = startY + renderHeight;
				for (TextureRegion region : yRegions) {
					int drawHeight = (int) (region.getRegionHeight() * scale);
					yOffset -= drawHeight;
					spriteBatch.draw(region, totalWidth, yOffset, drawWidth, drawHeight);
				}

				index++;
				if (index >= mXIndexes) {
					index = 0;
				}

				totalWidth += drawWidth;
				regionOffsetScaled = 0;
			}

		}

		/** First access is X coordinate i.e. mSprites.get(X).get(Y) */
		private ArrayList<ArrayList<TextureRegion>> mRegions = new ArrayList<>();
		private int mWidth = 0;
		private int mHeight = 0;
		private int mXIndexes;
		private int mYIndexes;
	}
}
