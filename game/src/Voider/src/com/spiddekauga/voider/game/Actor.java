package com.spiddekauga.voider.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * The abstract base class for all actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Actor implements ITriggerListener, Json.Serializable {
	/**
	 * Sets the texture of the actor including the actor definition.
	 * Automatically creates a body for the actor
	 * @param def actor definition
	 */
	public Actor(ActorDef def) {
		TextureRegion region = def.getTextureRegion(0);
		if (region != null) {
			mSprite = new Sprite(region);
		}

		mDef = def;
		mLife = def.getMaxLife();
		/** @TODO create body */
	}

	/**
	 * Updates the actor
	 * @param deltaTime seconds elapsed since last call
	 */
	public abstract void update(float deltaTime);

	/**
	 * Renders the actor
	 * @param spriteBatch the current sprite batch for the scene
	 */
	public void render(SpriteBatch spriteBatch) {
		mSprite.draw(spriteBatch);
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		// TODO Auto-generated method stub

	}

	/**
	 * Physical body
	 */
	private Body mBody;
	/**
	 * Current life
	 */
	private final float mLife;
	/**
	 * Sprite, i.e. the graphical representation
	 */
	private Sprite mSprite = null;
	/**
	 * The belonging definition of this actor
	 */
	private final ActorDef mDef;
}
