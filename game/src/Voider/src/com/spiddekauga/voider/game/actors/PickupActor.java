package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;


/**
 * Pickup actors contains a collectible that will be transfered to the player
 * once the player collides with this pickup.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PickupActor extends Actor {
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
	 * @return pickup filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.PICKUP;
	}

	/**
	 * Can collide only with other players
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return ActorFilterCategories.PLAYER;
	}
}
