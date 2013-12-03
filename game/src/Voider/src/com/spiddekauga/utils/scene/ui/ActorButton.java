package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Scene2D button that draws an actor in the specified size of the button
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorButton extends Button {
	/**
	 * Constructor which creates an actor button with the specified style and
	 * actor.
	 * @param actorDef the actor definition to use to create a new actor
	 * @param buttonStyle the button style to be used with this button
	 */
	public ActorButton(ActorDef actorDef, ButtonStyle buttonStyle) {
		super(buttonStyle);
		mActorDef = actorDef;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}

	/** The actor definition to use for draawing */
	protected ActorDef mActorDef;
}
