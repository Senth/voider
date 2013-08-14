package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.voider.game.Collectibles;
/**
 * Pickup actor definition, does nothing more than specify that the actor
 * is a pickup object
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PickupActorDef extends ActorDef {
	/**
	 * Default constructor
	 */
	public PickupActorDef() {
		super(ActorTypes.PICKUP);

		getBodyDef().type = BodyType.StaticBody;
	}

	/**
	 * Sets the collectible of the pickup
	 * @param collectible the collectible this pickup has
	 */
	public void setCollectible(Collectibles collectible) {
		if (collectible != null) {
			mCollectible = collectible;

			setName(collectible.toString());

			/** @TODO change picture or something? */
		}
	}

	@Override
	public Collectibles getCollectible() {
		return mCollectible;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mCollectible", mCollectible);
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		super.read(json, jsonValue);

		mCollectible = json.readValue("mCollectible", Collectibles.class, jsonValue);
	}

	/** Collectible inside the pickup */
	private Collectibles mCollectible = null;
}
