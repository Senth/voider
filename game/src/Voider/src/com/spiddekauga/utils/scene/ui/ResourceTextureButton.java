package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.voider.resources.IResourceTexture;

/**
 * Scene2D button that draws a resource in the specified size of the button
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceTextureButton extends ImageButton {
	/**
	 * Constructor which creates an actor button with the specified style and actor.
	 * @param resource the actor definition to get the texture from
	 * @param buttonStyle the button style to be used with this button
	 */
	public ResourceTextureButton(
			IResourceTexture resource, ImageButtonStyle buttonStyle) {
		super(new ImageButtonStyle(buttonStyle));
		setTexture(resource);
	}

	/**
	 * Constructor which creates an actor button with the specified style and actor. Will
	 * use the default skin for image button.
	 * @param resource the actor definition to get the texture from
	 * @param skin the skin to use for the button
	 */
	public ResourceTextureButton(
			IResourceTexture resource, Skin skin) {
		super(new ImageButtonStyle(skin.get(ImageButtonStyle.class)));
		setTexture(resource);
	}

	/**
	 * Constructor which creates an actor button with the specified style and actor.
	 * @param resource the actor definition to get the texture from
	 * @param skin the skin to use for the button
	 * @param styleName the style to search for in the skin
	 */
	public ResourceTextureButton(
			IResourceTexture resource, Skin skin, String styleName) {
		super(new ImageButtonStyle(skin.get(styleName, ImageButtonStyle.class)));
		setTexture(resource);
	}

	/**
	 * @return actorDefinition that is bound to this button
	 */
	public IResourceTexture getResource() {
		return mResource;
	}


	@Override
	public void draw(Batch batch, float parentAlpha) {
		updateImage();
		super.draw(batch, parentAlpha);
	}

	/**
	 * Update the current texture of the image. If the resource was changed the texture
	 * can be invalid.
	 */
	private void updateImage() {
		ImageButtonStyle imageButtonStyle = getStyle();

		if (imageButtonStyle.imageUp != mResource.getTextureRegionDrawable()) {
			imageButtonStyle.imageUp = mResource.getTextureRegionDrawable();
		}
	}

	/**
	 * Sets the correct image style
	 * @param resource the resource to get the texture from
	 */
	private void setTexture(IResourceTexture resource) {
		mResource = resource;

		updateImage();
	}

	@Override
	public float getPrefWidth() {
		float wrapWidth = super.getPrefWidth();
		float width = 0;

		ImageButtonStyle style = getStyle();
		if (style.imageUp != null) {
			width = Math.max(width, style.imageUp.getMinWidth());
		}
		if (style.imageDown != null) {
			width = Math.max(width, style.imageDown.getMinWidth());
		}
		if (style.imageChecked != null) {
			width = Math.max(width, style.imageChecked.getMinWidth());
		}

		return width + wrapWidth;
	}

	@Override
	public float getPrefHeight() {
		float wrapHeight = super.getPrefHeight();
		float height = 0;

		ImageButtonStyle style = getStyle();
		if (style.imageUp != null) {
			height = Math.max(height, style.imageUp.getMinHeight());
		}
		if (style.imageDown != null) {
			height = Math.max(height, style.imageDown.getMinHeight());
		}
		if (style.imageChecked != null) {
			height = Math.max(height, style.imageChecked.getMinHeight());
		}

		return height + wrapHeight;
	}

	/** The actor definition for the button */
	private IResourceTexture mResource;
}
