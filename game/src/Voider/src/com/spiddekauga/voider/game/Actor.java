package com.spiddekauga.voider.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.voider.Config;

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
		if (def.getTextureCount() > 0) {
			TextureRegion region = def.getTextureRegion(0);
			if (region != null) {
				mSprite = new Sprite(region);
			}
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
	 * @return the definition of the actor
	 */
	public ActorDef getDef() {
		return mDef;
	}

	/**
	 * Renders the actor
	 * @param spriteBatch the current sprite batch for the scene
	 */
	@SuppressWarnings("unused")
	public void render(SpriteBatch spriteBatch) {
		if (mSprite != null && !Config.Graphics.USE_DEBUG_RENDERER) {
			mSprite.draw(spriteBatch);
		}

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
