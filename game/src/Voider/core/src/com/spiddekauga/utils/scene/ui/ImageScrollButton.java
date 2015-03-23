package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

/**
 * An image button with several image layers that can scroll if specified. The size of
 * this image should be set externally.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ImageScrollButton extends Button {
	/**
	 * Creates an image button with the images and when to scroll. Be sure to call
	 * {@link #addLayer(TextureAtlas, String, float)} to add images to this button.
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
			removeActor(imageScroll);
		}
		mImages.clear();
	}

	/**
	 * Add an image to render on top of the existing
	 * @param atlas the atlas that contains the layerName
	 * @param imageName name of the image/layer (postfix should be 0-0) to get the correct
	 *        coordinates
	 * @param speed scroll speed of the image
	 */
	public void addLayer(TextureAtlas atlas, String imageName, float speed) {
		ImageScroll imageScroll = new ImageScroll(atlas, imageName, speed);
		addLayer(imageScroll, speed);


	}

	/**
	 * Add an image to render on top of the existing
	 * @param atlas the atlas that contains the layerName
	 * @param imageName name of the image/layer (postfix should be 0-0) to get the correct
	 *        coordinates
	 */
	public void addLayer(TextureAtlas atlas, String imageName) {
		addLayer(atlas, imageName, 0);
	}

	/**
	 * Add a layer from an already existing image scroll
	 * @param layer the layer to add
	 * @param speed speed of the layer
	 */
	public void addLayer(ImageScroll layer, float speed) {
		addActor(layer);
		layer.setScrollSpeed(speed);
		mImages.add(layer);
		setCorrectSize(layer);
		layer.setPosition(getPadLeft(), getPadRight());

		if (mScrollWhen == ScrollWhen.NEVER || mScrollWhen == ScrollWhen.HOVER) {
			layer.setScrollingEnabled(false);
		}
	}

	/**
	 * Add a layer with no speed from an already existing image scroll
	 * @param layer the layer to add
	 */
	public void addLayer(ImageScroll layer) {
		addLayer(layer, 0);
	}

	/**
	 * Set the correct size of a layer
	 * @param layer
	 */
	private void setCorrectSize(ImageScroll layer) {
		float width = getWidth() - getPadX();
		float height = getHeight() - getPadY();
		layer.setPosition(getPadLeft(), getPadRight());
		layer.setSize(width, height);
	}


	@Override
	public void setWidth(float width) {
		super.setWidth(width);

		resetSizes();
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height);

		resetSizes();
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);

		resetSizes();
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
	private void resetSizes() {
		if (mImages != null) {
			for (ImageScroll imageScroll : mImages) {
				setCorrectSize(imageScroll);
			}
		}
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		updateScrollForHover();
	}

	/**
	 * Update scrolling for hover
	 */
	private void updateScrollForHover() {
		if (mScrollWhen == ScrollWhen.HOVER) {
			for (ImageScroll imageScroll : mImages) {
				imageScroll.setScrollingEnabled(isOver());
			}
		}
	}

	/** When to scroll */
	private ScrollWhen mScrollWhen;
	/** Images to render */
	private ArrayList<ImageScroll> mImages = new ArrayList<>();
}
