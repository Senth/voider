package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * An image button with several image layers that can scroll if specified. The size of
 * this image should be set externally.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ImageScrollButton extends Button {
	/**
	 * Creates an image button with the images and when to scroll. Be sure to call
	 * {@link #addLayer(Texture, float)} to add images to this button.
	 * @param buttonStyle underlying button style
	 * @param scrollWhen when to scroll
	 */
	public ImageScrollButton(ButtonStyle buttonStyle, ScrollWhen scrollWhen) {
		super(buttonStyle);
		mScrollWhen = scrollWhen;
	}

	/**
	 * Clear all layers
	 */
	public void clearLayers() {
		for (ImageScroll imageScroll : mImages) {
			removeActor(imageScroll.mImage);
		}
		mImages.clear();
	}

	/**
	 * Add an image to render on top of the existing
	 * @param image the image to add
	 * @param speed scroll speed of the image
	 */
	public void addLayer(Texture image, float speed) {
		mImages.add(new ImageScroll(image, speed));
	}

	/**
	 * Add an image (with no scroll speed) to render on top of the existing
	 * @param image the image to add
	 */
	public void addLayer(Texture image) {
		mImages.add(new ImageScroll(image, 0));
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		updateImages();
		super.draw(batch, parentAlpha);
	}

	/**
	 * Update image scrolling
	 */
	private void updateImages() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		for (ImageScroll imageScroll : mImages) {
			imageScroll.updateImage(deltaTime);
		}
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(width);

		invalidateSizes();
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height);

		invalidateSizes();
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);

		invalidateSizes();
	};

	@Override
	public float getPrefHeight() {
		return getHeight();
	}

	@Override
	public float getPrefWidth() {
		return getWidth();
	}

	/**
	 * Update size of all images
	 */
	private void invalidateSizes() {
		if (mImages != null) {
			for (ImageScroll imageScroll : mImages) {
				imageScroll.invalidateSize();
			}
		}
	}

	/**
	 * Wrapper for an image to scroll and its scroll speed
	 */
	private class ImageScroll {
		/**
		 * Set image and scroll speed
		 * @param texture the image to show
		 * @param speed scroll speed of image, can be negative.
		 */
		ImageScroll(Texture texture, float speed) {
			mTexture = texture;
			mSpeed = speed;

			mRegion = new TextureRegion(mTexture);
			mImage = new Image(mRegion);
			addActor(mImage);
			mImage.setPosition(getPadLeft(), getPadBottom());
		}

		/**
		 * Invalidate size
		 */
		void invalidateSize() {
			mValidSize = false;
		}

		/**
		 * Updates the size of the image
		 */
		void updateSize() {
			int width = (int) (getWidth() - getPadX());
			int height = (int) (getHeight() - getPadY());

			// Change texture region size
			float scale = ((float) height) / mTexture.getHeight();
			int regionWidth = (int) (width / scale);
			mRegion.setRegion((int) mOffset, 0, regionWidth, mTexture.getHeight());

			// Set image size
			mImage.setSize(width, height);
		}

		/**
		 * Update image scrolling
		 * @param deltaTime time elapsed since last frame
		 */
		void updateImage(float deltaTime) {
			switch (mScrollWhen) {
			case ALWAYS:
				scroll(deltaTime);
				break;

			case HOVER:
				if (isOver()) {
					scroll(deltaTime);
				}
				break;

			case NEVER:
				// Does nothing
				break;
			}

			if (!mValidSize) {
				updateSize();
			}
		}

		/**
		 * Scroll the image
		 * @param deltaTime time elapsed since last frame
		 */
		void scroll(float deltaTime) {
			int oldOffset = (int) mOffset;
			mOffset += mSpeed * deltaTime;

			if (oldOffset != mOffset) {
				mRegion.scroll((int) mOffset, 0);
				mImage.invalidate();
			}
		}

		/** Offset coordinate */
		float mOffset = 0;
		/** Texture region to draw */
		TextureRegion mRegion;
		/** Image to scroll */
		Image mImage;
		/** Texture for image */
		Texture mTexture;
		/** Scroll speed */
		float mSpeed;
		/** Valid size */
		boolean mValidSize = false;
	}

	/**
	 * Scroll when
	 */
	public enum ScrollWhen {
		/** All the time */
		ALWAYS,
		/** Only on hover */
		HOVER,
		/** Never */
		NEVER,
	}

	/** When to scroll */
	private ScrollWhen mScrollWhen;
	/** Images to render */
	private ArrayList<ImageScroll> mImages = new ArrayList<>();
}
