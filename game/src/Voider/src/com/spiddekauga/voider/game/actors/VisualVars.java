package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Actor.Pickup;
import com.spiddekauga.voider.Config.Editor.Bullet;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * Class for all shape variables
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class VisualVars implements Json.Serializable, Disposable {
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
		json.writeValue("color", color);

		switch (shapeType) {
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
		color = json.readValue("color", Color.class, jsonData);

		switch (shapeType) {
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

		calculateBoundingRadius();
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

		calculateBoundingRadius();
	}

	@Override
	public void dispose() {
		for (Vector2 corner : corners) {
			Pools.vector2.free(corner);
		}
		corners.clear();

		clearVertices();
	}

	/**
	 * Clears (and possibly frees) the vertices of the shape.
	 */
	void clearVertices() {
		// Remove border corner indexes first. These should include all
		// regular vertices, so no need to free them later
		if (borderVertices != null && !borderVertices.isEmpty()) {
			// Because the vertices contains duplicates, we save the ones that have been
			// freed, so we don't free them twice. Never remove corners though
			ArrayList<Vector2> freedVertices = new ArrayList<Vector2>();
			freedVertices.addAll(corners);
			for (Vector2 vertex : borderVertices) {
				if (!freedVertices.contains(vertex)) {
					Pools.vector2.free(vertex);
					freedVertices.add(vertex);
				}
			}
			borderVertices.clear();
		} else {
			// Because the vertices contains duplicates, we save the ones that have been
			// freed, so we don't free them twice. Never remove corners though
			ArrayList<Vector2> freedVertices = new ArrayList<Vector2>();
			freedVertices.addAll(corners);
			for (Vector2 vertex : vertices) {
				if (!freedVertices.contains(vertex)) {
					Pools.vector2.free(vertex);
					freedVertices.add(vertex);
				}
			}
		}
		vertices.clear();
	}

	/**
	 * @return bounding radius of the actor
	 */
	float getBoundingRadius() {
		return mBoundingRadius;
	}

	/**
	 * Calculates the bounding radius
	 */
	void calculateBoundingRadius() {
		switch (shapeType) {
		case CIRCLE:
			mBoundingRadius = shapeCircleRadius;
			if (!centerOffset.equals(Vector2.Zero)) {
				mBoundingRadius += centerOffset.len();
			}

			break;


		case RECTANGLE:
		case TRIANGLE:
		case CUSTOM: {
			Vector2 farthestAway = null;
			// Use corners
			if (shapeType == ActorShapeTypes.CUSTOM) {
				farthestAway = Geometry.vertexFarthestAway(centerOffset, corners);
			}
			// Use vertices
			else {
				farthestAway = Geometry.vertexFarthestAway(centerOffset, vertices);
			}

			if (farthestAway != null) {
				Vector2 diffVector = Pools.vector2.obtain();
				diffVector.set(centerOffset).sub(farthestAway);
				mBoundingRadius = diffVector.len();
				Pools.vector2.free(diffVector);
			} else {
				mBoundingRadius = 0;
			}
			break;
		}
		}
	}

	/** Color of the actor */
	Color color = new Color();
	/** Border color, automatically set */
	Color borderColor = new Color();
	/** Current shape of the enemy */
	ActorShapeTypes shapeType;
	/** radius of circle */
	float shapeCircleRadius;
	/** width of rectangle/triangle */
	float shapeWidth;
	/** height of rectangle/triangle */
	float shapeHeight;
	/** Center offset for fixtures */
	Vector2 centerOffset = new Vector2();
	/** Corners of polygon, used for custom shapes */
	ArrayList<Vector2> corners = new ArrayList<Vector2>();
	/** Array list of the polygon figure, this contains the vertices but not
	 * in triangles. */
	ArrayList<Vector2> polygon = null;
	/** Triangle vertices.
	 * It is made this way to easily render the target. No optimization has been done to reduce
	 * the number of vertices. */
	ArrayList<Vector2> vertices = new ArrayList<Vector2>();
	/** Triangle border vertices. */
	ArrayList<Vector2> borderVertices = new ArrayList<Vector2>();

	/** Radius of the actor, or rather circle bounding box */
	private float mBoundingRadius = 0;
	/** Actor type, used for setting default values */
	private ActorTypes mActorType = null;
}
