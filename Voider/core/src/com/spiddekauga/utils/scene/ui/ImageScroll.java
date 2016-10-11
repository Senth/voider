package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.spiddekauga.voider.Config;

/**
 * A big image (layer) that combines several texture regions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ImageScroll extends WidgetGroup {
	/**
	 * Create a background layer with 0 in scroll speed.
	 * @param atlas where to get the name from
	 * @param layerName name of the layer
	 */
	public ImageScroll(TextureAtlas atlas, String layerName) {
		this(atlas, layerName, 0);
	}

	/**
	 * Create a background layer
	 * @param atlas where to get the name from
	 * @param layerName name of the layer
	 * @param scrollSpeed how fast this image should scroll
	 */
	public ImageScroll(TextureAtlas atlas, String layerName, float scrollSpeed) {
		mScrollSpeed = scrollSpeed;
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
			mHeightFull += yRegions.get(i).getRegionHeight();
		}


		// Calculate width
		for (int i = 0; i < mXIndexes; i++) {
			yRegions = mRegions.get(i);
			mWidthFull += yRegions.get(0).getRegionWidth();
		}

		calculateSplitSize();
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
	 * Render the layer for the full width of the screen
	 * @param spriteBatch
	 * @param offset
	 * @param startY where to start rendering
	 * @param renderHeight height of render
	 */
	public void render(SpriteBatch spriteBatch, float offset, int startY, int renderHeight) {
		float layerOffset = offset * mScrollSpeed;

		// Conversion to screen coordinates
		layerOffset /= Config.Graphics.WORLD_SCALE;

		// Texture scaling
		int screenWidth = Gdx.graphics.getWidth();
		float scale = ((float) renderHeight) / mHeightFull;
		float widthScaled = mWidthFull * scale;
		float splitSizeScaled = mSplitSize * scale;

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

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (batch instanceof SpriteBatch) {
			SpriteBatch spriteBatch = (SpriteBatch) batch;

			// Texture scaling
			float splitSizeScaled = mSplitSize * mScale;

			// Modulate so it's within the layer width

			int index = (int) (mOffsetX / splitSizeScaled);

			// X-offset for the first image
			int regionOffsetScaled = (int) (mOffsetX % splitSizeScaled);
			int totalWidth = (int) getX();
			int endWidth = (int) getWidth() + totalWidth;

			// Draw background
			while (totalWidth < endWidth) {
				ArrayList<TextureRegion> yRegions = mRegions.get(index);

				int regionWidth = yRegions.get(0).getRegionWidth();
				int regionWidthScaled = (int) (regionWidth * mScale);

				int drawWidth = regionWidthScaled - regionOffsetScaled;
				if (drawWidth + totalWidth > endWidth) {
					drawWidth = endWidth - totalWidth;
				}

				// Draw call
				int yOffset = (int) (getHeight() + getY());
				for (TextureRegion region : yRegions) {
					TextureRegion drawRegion = region;

					if (!MathUtils.isEqual(drawWidth, regionWidthScaled)) {
						drawRegion = new TextureRegion(region, (int) (regionOffsetScaled / mScale), 0, (int) (drawWidth / mScale),
								region.getRegionHeight());
					}
					int drawHeight = (int) (region.getRegionHeight() * mScale);
					yOffset -= drawHeight;
					spriteBatch.draw(drawRegion, totalWidth, yOffset, drawWidth, drawHeight);
				}

				index++;
				if (index >= mXIndexes) {
					index = 0;
				}

				totalWidth += drawWidth;
				regionOffsetScaled = 0;
			}
		}
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		if (mEnabled) {
			scrollImage(delta);
		}
	}

	@Override
	public float getPrefHeight() {
		return getHeight();
	}

	@Override
	public float getPrefWidth() {
		return getWidth();
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(width);

		updateImageSizes();
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height);

		updateImageSizes();
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);

		updateImageSizes();
	}

	/**
	 * Update image sizes
	 */
	private void updateImageSizes() {
		mScale = getHeight() / mHeightFull;
		mWidthScaled = (int) (mWidthFull * mScale);
	}

	/**
	 * Update image positions
	 * @param deltaTime elapsed time since last frame
	 */
	private void scrollImage(float deltaTime) {
		mOffsetX += mScrollSpeed * deltaTime * mScale;

		if (mOffsetX >= mWidthScaled) {
			mOffsetX = mOffsetX % mWidthScaled;
		}
		while (mOffsetX < 0) {
			mOffsetX += mWidthScaled;
		}
	}

	/**
	 * Set the scroll speed
	 * @param scrollSpeed
	 */
	public void setScrollSpeed(float scrollSpeed) {
		mScrollSpeed = scrollSpeed;
	}

	/**
	 * @return scroll speed
	 */
	public float getScrollSpeed() {
		return mScrollSpeed;
	}

	/**
	 * Set whether scrolling should be enabled or not
	 * @param enabled true if enabled
	 */
	public void setScrollingEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	/**
	 * Calculate split size
	 */
	private void calculateSplitSize() {
		TextureRegion firstRegion = mRegions.get(0).get(0);

		int largestValue = firstRegion.getRegionWidth();
		if (firstRegion.getRegionHeight() > largestValue) {
			largestValue = firstRegion.getRegionHeight();
		}

		mSplitSize = largestValue;
	}

	/** First access is X coordinate i.e. mRegions.get(X).get(Y) */
	private ArrayList<ArrayList<TextureRegion>> mRegions = new ArrayList<>();
	private int mWidthFull = 0;
	private int mWidthScaled = 0;
	private int mHeightFull = 0;
	private int mXIndexes;
	private int mYIndexes;
	private int mSplitSize;
	/** How much the images are scaled */
	private float mScale = 0;
	private float mScrollSpeed = 0;
	private float mOffsetX = 0;
	private boolean mEnabled = true;
}