package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config.Actor.Pickup;
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

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mCollectible = json.readValue("mCollectible", Collectibles.class, jsonData);
	}

	@Override
	protected FixtureDef getDefaultFixtureDef() {
		CircleShape circle = new CircleShape();
		circle.setRadius(Pickup.RADIUS);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		return fixtureDef;
	}

	/** Collectible inside the pickup */
	private Collectibles mCollectible = null;
}
