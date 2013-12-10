package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.voider.resources.IResourceTexture;

/**
 * Scene2D button that draws a resource in the specified size of the button
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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

	/**
	 * Sets the correct image style
	 * @param resource the resource to get the texture from
	 */
	private void setTexture(IResourceTexture resource) {
		mResource = resource;

		ImageButtonStyle imageButtonStyle = getStyle();
		imageButtonStyle.imageDown = resource.getTextureRegionDrawable();
		imageButtonStyle.imageUp = resource.getTextureRegionDrawable();

		if (imageButtonStyle.checked != null) {
			imageButtonStyle.imageChecked = resource.getTextureRegionDrawable();
		}
	}

	/** The actor definition for the button */
	private IResourceTexture mResource;
}
