package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Scene2D button that draws an actor in the specified size of the button
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorButton extends ImageButton {
	/**
	 * Constructor which creates an actor button with the specified style and
	 * actor.
	 * @param actorDef the actor definition to get the texture from
	 * @param buttonStyle the button style to be used with this button
	 */
	public ActorButton(ActorDef actorDef, ImageButtonStyle buttonStyle) {
		super(new ImageButtonStyle(buttonStyle));
		setTexture(actorDef);
	}

	/**
	 * Constructor which creates an actor button with the specified style and
	 * actor. Will use the default skin for image button.
	 * @param actorDef the actor definition to get the texture from
	 * @param skin the skin to use for the button
	 */
	public ActorButton(ActorDef actorDef, Skin skin) {
		super(new ImageButtonStyle(skin.get(ImageButtonStyle.class)));
		setTexture(actorDef);
	}

	/**
	 * Constructor which creates an actor button with the specified style and
	 * actor.
	 * @param actorDef the actor definition to get the texture from
	 * @param skin the skin to use for the button
	 * @param styleName the style to search for in the skin
	 */
	public ActorButton(ActorDef actorDef, Skin skin, String styleName) {
		super(new ImageButtonStyle(skin.get(styleName, ImageButtonStyle.class)));
		setTexture(actorDef);
	}

	/**
	 * Sets the correct image style
	 * @param actorDef the actor definition to get the texture from
	 */
	private void setTexture(ActorDef actorDef) {
		ImageButtonStyle imageButtonStyle = getStyle();
		imageButtonStyle.imageDown = actorDef.getTextureRegionDrawable();
		imageButtonStyle.imageUp = actorDef.getTextureRegionDrawable();

		if (imageButtonStyle.checked != null) {
			imageButtonStyle.imageChecked = actorDef.getTextureRegionDrawable();
		}
	}
}
