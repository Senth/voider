package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.game.Collectibles;

/**
 * Pickup actor definition, does nothing more than specify that the actor is a pickup object
 */
public class PickupActorDef extends ActorDef {
/** Collectible inside the pickup */
@Tag(76)
private Collectibles mCollectible = null;

/**
 * Default constructor
 */
public PickupActorDef() {
	super(ActorTypes.PICKUP);

	getBodyDef().type = BodyType.StaticBody;
}

@Override
public Collectibles getCollectible() {
	return mCollectible;
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
}
