package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.voider.resources.IResourceTexture;

/**
 * Scene2D button that draws a resource in the specified size of the button
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceTextureButton extends ImageButton {
	/**
	 * Constructor which creates an actor button with the specified style and
	 * actor.
	 * @param resource the actor definition to get the texture from
	 * @param buttonStyle the button style to be used with this button
	 */
	public ResourceTextureButton(IResourceTexture resource, ImageButtonStyle buttonStyle) {
		super(new ImageButtonStyle(buttonStyle));
		setTexture(resource);
	}

	/**
	 * Constructor which creates an actor button with the specified style and
	 * actor. Will use the default skin for image button.
	 * @param resource the actor definition to get the texture from
	 * @param skin the skin to use for the button
	 */
	public ResourceTextureButton(IResourceTexture resource, Skin skin) {
		super(new ImageButtonStyle(skin.get(ImageButtonStyle.class)));
		setTexture(resource);
	}

	/**
	 * Constructor which creates an actor button with the specified style and
	 * actor.
	 * @param resource the actor definition to get the texture from
	 * @param skin the skin to use for the button
	 * @param styleName the style to search for in the skin
	 */
	public ResourceTextureButton(IResourceTexture resource, Skin skin, String styleName) {
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
	public void draw(SpriteBatch batch, float parentAlpha) {
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

	/** The actor definition for the button */
	private IResourceTexture mResource;
}
