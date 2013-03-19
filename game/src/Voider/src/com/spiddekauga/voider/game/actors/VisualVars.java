package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Actor.Pickup;
import com.spiddekauga.voider.Config.Editor.Bullet;
import com.spiddekauga.voider.Config.Editor.Enemy;

/**
 * Class for all shape variables
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class VisualVars implements Json.Serializable {
	/**
	 * Sets the appropriate default values
	 * @param actorType the default values depends on which actor type is set
	 */
	VisualVars(ActorTypes actorType) {
		mActorType = actorType;
		setDefaultValues();
	}

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);

		json.writeValue("shapeType", shapeType);
		json.writeValue("mActorType", mActorType);
		json.writeValue("centerOffset", centerOffset);

		switch (shapeType) {
		case LINE:
			json.writeValue("shapeWidth", shapeWidth);
			break;

		case TRIANGLE:
		case RECTANGLE:
			json.writeValue("shapeWidth", shapeWidth);
			json.writeValue("shapeHeight", shapeHeight);
			break;

		case CIRCLE:
			json.writeValue("shapeCircleRadius", shapeCircleRadius);
			break;

		case CUSTOM:
			json.writeValue("corners", corners);
			break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		mActorType = json.readValue("mActorType", ActorTypes.class, jsonData);

		setDefaultValues();

		shapeType = json.readValue("shapeType", ActorShapeTypes.class, jsonData);
		centerOffset = json.readValue("centerOffset", Vector2.class, jsonData);

		switch (shapeType) {
		case LINE:
			shapeWidth = json.readValue("shapeWidth", float.class, jsonData);
			break;

		case TRIANGLE:
		case RECTANGLE:
			shapeWidth = json.readValue("shapeWidth", float.class, jsonData);
			shapeHeight = json.readValue("shapeHeight", float.class, jsonData);
			break;

		case CIRCLE:
			shapeCircleRadius = json.readValue("shapeCircleRadius", float.class, jsonData);
			break;

		case CUSTOM:
			corners = json.readValue("corners", ArrayList.class, jsonData);
			break;
		}
	}

	/**
	 * Default constructor for JSON
	 */
	@SuppressWarnings("unused")
	private VisualVars() {
		// Does nothing
	}

	/**
	 * Sets the default values of the visual vars depending on the current actor type
	 */
	private void setDefaultValues() {
		// Type specific settings
		switch (mActorType) {
		case ENEMY:
			shapeType = Enemy.Visual.SHAPE_DEFAULT;
			shapeCircleRadius = Enemy.Visual.RADIUS_DEFAULT;
			shapeWidth = Enemy.Visual.SIZE_DEFAULT;
			shapeHeight = Enemy.Visual.SIZE_DEFAULT;
			break;

		case BULLET:
			shapeType = Bullet.Visual.SHAPE_DEFAULT;
			shapeCircleRadius = Bullet.Visual.RADIUS_DEFAULT;
			shapeWidth = Bullet.Visual.SIZE_DEFAULT;
			shapeHeight = Bullet.Visual.SIZE_DEFAULT;
			break;

		case STATIC_TERRAIN:
			shapeType = ActorShapeTypes.CUSTOM;
			shapeCircleRadius = 0;
			shapeWidth = 0;
			shapeHeight = 0;
			break;

		case PICKUP:
			shapeType = ActorShapeTypes.CIRCLE;
			shapeCircleRadius = Pickup.RADIUS;
			shapeHeight = 0;
			shapeWidth = 0;
			break;

		case PLAYER:
			shapeType = ActorShapeTypes.CIRCLE;
			shapeCircleRadius = 1;
			shapeHeight = 0;
			shapeWidth = 0;
			break;

		default:
			Gdx.app.error("VisualVars", "Unknown actor type: " + mActorType);
			shapeType = ActorShapeTypes.CIRCLE;
			shapeCircleRadius = 1;
			shapeHeight = 1;
			shapeWidth = 1;
			break;
		}
	}

	/** Current shape of the enemy */
	ActorShapeTypes shapeType;
	/** radius of circle */
	float shapeCircleRadius;
	/** width of rectangle/triangle */
	float shapeWidth;
	/** height of rectangle/triangle */
	float shapeHeight;
	/** Number of circle segments */
	int cCircleSegments = 3;
	/** Circle vertices, as a circle is used from
	/** custom circle radius, only applicable when using custom shapes, automatically calculated */
	float customRadius = 0;
	/** Center offset for fixtures */
	Vector2 centerOffset = new Vector2();
	/** Corners of polygon, used for custom shapes */
	ArrayList<Vector2> corners = new ArrayList<Vector2>();


	/** Actor type, used for setting default values */
	private ActorTypes mActorType = null;
}
