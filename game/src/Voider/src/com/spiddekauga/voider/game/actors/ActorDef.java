package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.resources.Def;

/**
 * Definition of the actor. This include common attribute for a common type of actor.
 * E.g. A specific enemy will have the same variables here. The only thing changed during
 * it's life is the variables in the Actor class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorDef extends Def implements Json.Serializable, Disposable {
	/**
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 */
	public ActorDef(
			float maxLife,
			String name,
			FixtureDef fixtureDef
			)
	{
		setName(name);
		mMaxLife = maxLife;
		if (fixtureDef != null) {
			addFixtureDef(fixtureDef);
		}
	}

	/**
	 * Sets the visual variable to the specified type
	 * @param actorType the actor type to which set the default values of
	 * the visual variables
	 */
	protected ActorDef(ActorTypes actorType) {
		mVisualVars = new VisualVars(actorType);
	}

	/**
	 * Default constructor
	 */
	public ActorDef() {
		// Does nothing
	}

	/**
	 * Sets the starting angle of the actor
	 * @param angle the starting angle
	 */
	public void setStartAngle(float angle) {
		getBodyDef().angle = (float)Math.toRadians(angle);
	}

	/**
	 * @return starting angle of the actor (in degrees)
	 */
	public float getStartAngle() {
		return (float) Math.toDegrees(getBodyDef().angle);
	}

	/**
	 * Sets the collision damage of the actor
	 * @param collisionDamage damage (per second) this actor will make to another when
	 * colliding actor.
	 * @return this for chaining commands
	 */
	public ActorDef setCollisionDamage(float collisionDamage) {
		mCollisionDamage = collisionDamage;
		return this;
	}

	/**
	 * @return the collision damage (per second) this actor will make to another
	 * colliding actor.
	 */
	public float getCollisionDamage() {
		return mCollisionDamage;
	}

	/**
	 * Sets the maximum life of the actor. I.e. starting amount of
	 * life.
	 * @param maxLife the maximum/starting amount of life.
	 * @return this for chaining commands
	 */
	public ActorDef setMaxLife(float maxLife) {
		mMaxLife = maxLife;
		return this;
	}

	/**
	 * @return Maximum life of the actor. I.e. starting amount of life
	 */
	public float getMaxLife() {
		return mMaxLife;
	}

	/**
	 * Adds another fixture definition
	 * @param fixtureDef
	 */
	public void addFixtureDef(FixtureDef fixtureDef) {
		mFixtureDefs.add(fixtureDef);
	}

	/**
	 * @return collectible of the actor def. Only works for PickupActorDef other
	 * actors defs returns null.
	 */
	public Collectibles getCollectible() {
		return null;
	}

	/**
	 * Clears all existing fixtures
	 */
	public void clearFixtures() {
		for (FixtureDef fixtureDef : mFixtureDefs) {
			if (fixtureDef.shape != null) {
				fixtureDef.shape.dispose();
			}
		}

		mFixtureDefs.clear();
	}

	/**
	 * Returns the fixture definition. Includes shape, mass, etc.
	 * @return fixture definition.
	 */
	public final List<FixtureDef> getFixtureDefs() {
		return mFixtureDefs;
	}

	/**
	 * @return body definition of the actor
	 */
	public final BodyDef getBodyDef() {
		return mBodyDef;
	}

	/**
	 * @return a copy of the body definition
	 */
	public BodyDef getBodyDefCopy() {
		BodyDef copy = new BodyDef();
		copy.angle = mBodyDef.angle;
		copy.active = mBodyDef.active;
		copy.allowSleep = mBodyDef.allowSleep;
		copy.angularDamping = mBodyDef.angularDamping;
		copy.awake = mBodyDef.awake;
		copy.bullet = mBodyDef.bullet;
		copy.fixedRotation = mBodyDef.fixedRotation;
		copy.gravityScale = mBodyDef.gravityScale;
		copy.linearDamping = mBodyDef.linearDamping;
		copy.position.set(mBodyDef.position);
		copy.type = mBodyDef.type;
		return copy;
	}

	@Override
	public void dispose() {
		clearFixtures();
	}

	/**
	 * Sets the shape type of the enemy. This will clear the existing fixture shape
	 * for the enemy and created another one with default values.
	 * @param shapeType type of shape the enemy has
	 */
	public void setShapeType(ActorShapeTypes shapeType) {
		mVisualVars.shapeType = shapeType;

		FixtureDef fixtureDef = getFirstFixtureDef();


		// Should only have one fixture def...
		if (fixtureDef != null) {
			// Remove the old shape if one exists
			if (fixtureDef.shape != null) {
				fixtureDef.shape.dispose();
			}

			// Create the new shape
			switch (shapeType) {
			case CIRCLE: {
				CircleShape circleShape = new CircleShape();
				circleShape.setRadius(mVisualVars.shapeCircleRadius);
				fixtureDef.shape = circleShape;
				break;
			}

			case RECTANGLE: {
				PolygonShape rectangleShape = new PolygonShape();
				rectangleShape.setAsBox(mVisualVars.shapeWidth * 0.5f, mVisualVars.shapeHeight * 0.5f);
				fixtureDef.shape = rectangleShape;
				break;
			}

			case TRIANGLE: {
				PolygonShape triangleShape = new PolygonShape();

				Vector2[] vertices = createTriangle(mVisualVars.shapeWidth, mVisualVars.shapeHeight);

				triangleShape.set(vertices);
				fixtureDef.shape = triangleShape;

				for (Vector2 vertex : vertices) {
					Pools.free(vertex);
				}

				break;
			}

			case LINE:
				EdgeShape edgeShape = new EdgeShape();
				edgeShape.set(0, 0, -mVisualVars.shapeWidth, 0);
				fixtureDef.shape = edgeShape;
				break;

			case CUSTOM:
				/** @todo implement custom actor shape */
				break;
			}

		} else {
			Gdx.app.error("EnemyActorDef", "FixtureDef null at setShapeType()");
		}
	}

	/**
	 * Sets the shape radius (only applicable for circle)
	 * @param radius new radius value
	 */
	public void setShapeRadius(float radius) {
		mVisualVars.shapeCircleRadius = radius;

		// Update fixture if circle
		if (mVisualVars.shapeType == ActorShapeTypes.CIRCLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				if (fixtureDef.shape instanceof CircleShape) {
					fixtureDef.shape.setRadius(radius);
				} else {
					Gdx.app.error("EnemyActorDef", "FixtureDef shape is not a circle!");
				}
			} else {
				Gdx.app.error("EnemyActorDef", "FixtureDef null at setShapeRadius()");
			}

		}
	}

	/**
	 * @return shape radius of the circle
	 */
	public float getShapeRadius() {
		return mVisualVars.shapeCircleRadius;
	}

	/**
	 * Sets the shape width (only applicable for rectangle/triangle)
	 * @param width new width of the rectangle/triangle
	 */
	public void setShapeWidth(float width) {
		mVisualVars.shapeWidth = width;

		// Update fixture if rectangle/triangle
		if (mVisualVars.shapeType == ActorShapeTypes.RECTANGLE || mVisualVars.shapeType == ActorShapeTypes.TRIANGLE || mVisualVars.shapeType == ActorShapeTypes.LINE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				if (fixtureDef.shape instanceof PolygonShape) {
					// RECTANGLE
					if (mVisualVars.shapeType == ActorShapeTypes.RECTANGLE) {
						((PolygonShape)fixtureDef.shape).setAsBox(mVisualVars.shapeWidth * 0.5f, mVisualVars.shapeHeight * 0.5f);
					}
					// TRIANGLE
					else if (mVisualVars.shapeType == ActorShapeTypes.TRIANGLE) {
						Vector2[] vertices = createTriangle(mVisualVars.shapeWidth, mVisualVars.shapeHeight);

						((PolygonShape)fixtureDef.shape).set(vertices);

						for (Vector2 vertex : vertices) {
							Pools.free(vertex);
						}
					}
				}
				else if (fixtureDef.shape instanceof EdgeShape) {
					// LINE
					if (mVisualVars.shapeType == ActorShapeTypes.LINE) {
						((EdgeShape)fixtureDef.shape).set(0, 0, mVisualVars.shapeWidth, 0);
					}
				}
				else {
					Gdx.app.error("EnemyActorDef", "FixtureDef shape is neither a polygon nor a line!");
				}
			} else {
				Gdx.app.error("EnemyActorDef", "FixtureDef null at setShapeWidth()");
			}
		}
	}

	/**
	 * @return shape width of rectangle/triangle
	 */
	public float getShapeWidth() {
		return mVisualVars.shapeWidth;
	}

	/**
	 * Sets the shape height (only applicable for rectangle/triangle)
	 * @param height new height of the rectangle/triangle
	 */
	public void setShapeHeight(float height) {
		mVisualVars.shapeHeight = height;

		// Update fixture if rectangle/triangle
		if (mVisualVars.shapeType == ActorShapeTypes.RECTANGLE || mVisualVars.shapeType == ActorShapeTypes.TRIANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				if (fixtureDef.shape instanceof PolygonShape) {
					// RECTANGLE
					if (mVisualVars.shapeType == ActorShapeTypes.RECTANGLE) {
						((PolygonShape)fixtureDef.shape).setAsBox(mVisualVars.shapeWidth * 0.5f, mVisualVars.shapeHeight * 0.5f);
					}
					// TRIANGLE
					else if (mVisualVars.shapeType == ActorShapeTypes.TRIANGLE) {
						Vector2[] vertices = createTriangle(mVisualVars.shapeWidth, mVisualVars.shapeHeight);

						((PolygonShape)fixtureDef.shape).set(vertices);

						for (Vector2 vertex : vertices) {
							Pools.free(vertex);
						}
					}
				} else {
					Gdx.app.error("EnemyActorDef", "FixtureDef shape is not a polygon!");
				}
			} else {
				Gdx.app.error("EnemyActorDef", "FixtureDef null at setShapeWidth()");
			}
		}
	}

	/**
	 * @return shape height of rectangle/triangle
	 */
	public float getShapeHeight() {
		return mVisualVars.shapeHeight;
	}

	/**
	 * @return current shape type of the enemy
	 */
	public ActorShapeTypes getShapeType() {
		return mVisualVars.shapeType;
	}

	/**
	 * Creates vertices for the triangle shape.
	 * @param width width of the triangle (i.e. how long it shall be)
	 * @param height height of the triangle
	 * @return 3 vertices used for the triangle shape. Don't forget to free these triangles
	 * using Pools.free(vertices)
	 */
	private Vector2[] createTriangle(float width, float height) {
		Vector2[] vertices = new Vector2[3];

		for (int i = 0; i < vertices.length; ++i) {
			vertices[i] = Pools.obtain(Vector2.class);
		}

		// It will look something like this:
		// | \
		// |   >
		// | /

		// Lower left corner
		vertices[0].x = - width * 0.5f;
		vertices[0].y = - height * 0.5f;

		// Middle right corner
		vertices[1].x = width * 0.5f;
		vertices[1].y = 0;

		// Upper left corner
		vertices[2].x = vertices[0].x;
		vertices[2].y = - vertices[0].y;

		return vertices;
	}

	/**
	 * Gets the first fixture definition. Prints an error if there are more or less fixtures than 1
	 * @return first fixture definition, null if none is found
	 */
	private FixtureDef getFirstFixtureDef() {
		List<FixtureDef> fixtureDefs = getFixtureDefs();
		if (fixtureDefs.size() == 1) {
			return fixtureDefs.get(0);
		} else {
			Gdx.app.error("EnemyActorDef", "Too few/many fixture definitions! " + fixtureDefs.size());
			return null;
		}
	}

	/** Defines the mass, shape, etc. */
	private ArrayList<FixtureDef> mFixtureDefs = new ArrayList<FixtureDef>();
	/** Maximum life of the actor, usually starting amount of life */
	private float mMaxLife = 0;
	/** The body definition of the actor */
	private BodyDef mBodyDef = new BodyDef();
	/** Collision damage (per second) */
	private float mCollisionDamage = 0;
	/** Visual variables */
	private VisualVars mVisualVars = null;

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);

		json.writeObjectStart("Def");
		super.write(json);
		json.writeObjectEnd();

		// Write ActorDef's variables first
		json.writeValue("mMaxLife", mMaxLife);
		json.writeValue("mBodyDef", mBodyDef);
		json.writeValue("mFixtureDefs", mFixtureDefs);
		json.writeValue("mCollisionDamage", mCollisionDamage);
		json.writeValue("mVisualVars", mVisualVars);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		// Superclass
		OrderedMap<String, Object> superMap = json.readValue("Def", OrderedMap.class, jsonData);
		if (superMap != null) {
			super.read(json, superMap);
		}


		// Our variables
		mMaxLife = json.readValue("mMaxLife", float.class, jsonData);
		mBodyDef = json.readValue("mBodyDef", BodyDef.class, jsonData);
		mFixtureDefs = json.readValue("mFixtureDefs", ArrayList.class, jsonData);
		mCollisionDamage = json.readValue("mCollisionDamage", float.class, jsonData);
		mVisualVars = json.readValue("mVisualVars", VisualVars.class, jsonData);
	}
}
