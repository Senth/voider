package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ActorDef;
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
		// Create spherical fixture
		CircleShape circle = new CircleShape();
		circle.setRadius(Config.Actor.Pickup.RADIUS);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		addFixtureDef(fixtureDef);
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
		json.writeObjectStart("ActorDef");
		super.write(json);
		json.writeObjectEnd();

		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("mCollectible", mCollectible);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		// Superclass
		OrderedMap<String, Object> superMap = json.readValue("ActorDef", OrderedMap.class, jsonData);
		if (superMap != null) {
			super.read(json, superMap);
		}

		mCollectible = json.readValue("mCollectible", Collectibles.class, jsonData);
	}

	/**
	 * @return pickup filter category
	 */
	@Override
	protected short getFilterCategory() {
		return FixtureFilterCategories.PICKUP;
	}

	/**
	 * Can collide only with other players
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return FixtureFilterCategories.PLAYER;
	}

	/** Collectible inside the pickup */
	private Collectibles mCollectible = null;
}
