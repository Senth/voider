package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Collectibles;




/**
 * The ship the player controls
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActor extends com.spiddekauga.voider.game.actors.Actor {
	/**
	 * Creates a default player
	 */
	public PlayerActor() {
		super(new PlayerActorDef());
	}

	/**
	 * Adds a collectible to the player
	 * @param collectible the collectible to add to the player
	 */
	public void addCollectible(Collectibles collectible) {
		switch (collectible) {
		case HEALTH_25:
			mLife += 25;
			break;

		case HEALTH_50:
			mLife += 50;
			break;
		}
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart("Actor");
		super.write(json);
		json.writeObjectEnd();

		json.writeValue("REVISION", Config.REVISION);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		OrderedMap<String, Object> actorMap = json.readValue("Actor", OrderedMap.class, jsonData);
		super.read(json, actorMap);
	}

	/**
	 * @return player filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.PLAYER;
	}

	/**
	 * Can collide with everything except player and player bullets
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return (short) (ActorFilterCategories.ENEMY | ActorFilterCategories.PICKUP | ActorFilterCategories.STATIC_TERRAIN | ActorFilterCategories.SCREEN_BORDER);
	}
}
