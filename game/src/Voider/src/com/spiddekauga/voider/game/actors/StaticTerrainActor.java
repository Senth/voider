package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.TriggerAction;

/**
 * Static terrain actor. This terrain will not move, and cannot be destroyed.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class StaticTerrainActor extends Actor {

	/**
	 * Default constructor, creates a new definition for the actor
	 */
	public StaticTerrainActor() {
		super(new StaticTerrainActorDef());
	}

	@Override
	public void onTriggered(TriggerAction action) {
		// Does nothing
	}

	@Override
	public void renderEditor(SpriteBatch spriteBatch) {
		/** @TODO render the corners */
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

	@Override
	public boolean savesDef() {
		return true;
	}

	/**
	 * @return static terrain filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.STATIC_TERRAIN;
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
