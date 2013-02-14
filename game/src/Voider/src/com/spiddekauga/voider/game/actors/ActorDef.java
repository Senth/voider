package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.utils.Geometry;

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
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
				fixtureDef.shape = null;
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

		for (Vector2 corner : mVisualVars.corners) {
			Pools.free(corner);
		}
	}

	/**
	 * Sets the shape type of the enemy. This will clear the existing fixture shape
	 * for the enemy and created another one with default values.
	 * @param shapeType type of shape the enemy has
	 */
	public void setShapeType(ActorShapeTypes shapeType) {
		mVisualVars.shapeType = shapeType;

		// Too many fixtures
		if (mFixtureDefs.size() > 1) {
			// Save first fixture def
			FixtureDef fixtureDef = mFixtureDefs.get(0);
			clearFixtures();
			addFixtureDef(fixtureDef);
		}
		// Too few
		else if (mFixtureDefs.size() == 0) {
			addFixtureDef(getDefaultFixtureDef());
		}

		FixtureDef fixtureDef = getFirstFixtureDef();

		if (fixtureDef != null) {
			// Remove the old shape if one exists
			if (fixtureDef.shape != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = null;
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
				try {
					fixCustomShapeFixtures();
				} catch (PolygonCornerTooCloseException e) {
					Gdx.app.error("ActorDef", "Could not set custom shape when changing shapes");
				}
				break;
			}

		} else {
			Gdx.app.error("EnemyActorDef", "FixtureDef null at setShapeType()");
		}
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
	 * Add another corner position to the back of the array
	 * @param corner a new corner that will be placed at the back
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 * @throws PolygonCornerTooCloseException thrown when a corner is too close to
	 * another corner inside the polygon.
	 */
	public void addCorner(Vector2 corner) throws PolygonComplexException, PolygonCornerTooCloseException {
		addCorner(corner, mVisualVars.corners.size());
	}

	/**
	 * Add a corner in the specified index
	 * @param corner position of the corner to add
	 * @param index where in the list the corner will be added
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 * @throws PolygonCornerTooCloseException thrown when a corner is too close to
	 * another corner inside the polygon.
	 */
	public void addCorner(Vector2 corner, int index) throws PolygonComplexException, PolygonCornerTooCloseException{
		mVisualVars.corners.add(index, corner.cpy());

		// Make sure no intersection exist
		if (intersectionExists(index)) {
			mVisualVars.corners.remove(index);
			throw new PolygonComplexException();
		}

		try {
			fixCustomShapeFixtures();
		} catch (PolygonCornerTooCloseException e) {
			mVisualVars.corners.remove(index);

			// Reset to old fixtures
			try {
				fixCustomShapeFixtures();
			} catch (PolygonCornerTooCloseException e1) {
				Gdx.app.error("ActorDef", "Could not fix custom shape fixtures!");
			}

			throw e;
		}
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Removes a corner with the specific id.
	 * @param index the corner to remove
	 * @return position of the corner we removed, null if none was removed
	 */
	public Vector2 removeCorner(int index) {
		Vector2 removedPosition = null;
		if (index >= 0 && index < mVisualVars.corners.size()) {
			removedPosition = mVisualVars.corners.remove(index);

			try {
				fixCustomShapeFixtures();
			} catch (PolygonCornerTooCloseException e) {
				Gdx.app.error("ActorDef", "Failed to remove corner, exception, should never happen");
			}
		}
		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();

		return removedPosition;
	}

	/**
	 * Moves a corner, identifying the corner from index
	 * @param index index of the corner to move
	 * @param newPos new position of the corner
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 * @throws PolygonCornerTooCloseException thrown when a corner is too close to
	 * another corner inside the polygon.
	 */
	public void moveCorner(int index, Vector2 newPos) throws PolygonComplexException, PolygonCornerTooCloseException {
		Vector2 oldPos = Pools.obtain(Vector2.class);
		oldPos.set(mVisualVars.corners.get(index));
		mVisualVars.corners.get(index).set(newPos);

		if (intersectionExists(index)) {
			mVisualVars.corners.get(index).set(oldPos);
			Pools.free(oldPos);
			throw new PolygonComplexException();
		}

		try {
			fixCustomShapeFixtures();
		} catch (PolygonCornerTooCloseException e) {
			mVisualVars.corners.get(index).set(oldPos);
			Pools.free(oldPos);
			throw e;
		}

		Pools.free(oldPos);

		mLastChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * @return number of corners in this actor. Only applicable if actor is set as custom shape
	 */
	public int getCornerCount() {
		return mVisualVars.corners.size();
	}


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

	/**
	 * Exception class for when trying to create a new, or move an existing corner
	 * and this makes the polygon complex, i.e. it intersects with itself.
	 */
	public class PolygonComplexException extends Exception {
		/** for serialization */
		private static final long serialVersionUID = -2564535357356811708L;
	}

	/**
	 * Exception class for when a triangle of the polygon would make too
	 * small area.
	 */
	public class PolygonCornerTooCloseException extends Exception {
		/** For serialization */
		private static final long serialVersionUID = 5402912928691451496L;
	}

	/**
	 * @param index the index we want to get the corner from
	 * @return corner position of the specified index
	 */
	public Vector2 getCornerPosition(int index) {
		return mVisualVars.corners.get(index);
	}

	/**
	 * @return default fixture definition.
	 */
	protected abstract FixtureDef getDefaultFixtureDef();

	/**
	 * @return all the corners of the actor
	 */
	ArrayList<Vector2> getCorners() {
		return mVisualVars.corners;
	}

	/**
	 * @return time when this definition was last changed. In global time.
	 */
	float getLastChangeTime() {
		return mLastChangeTime;
	}

	/**
	 * Readjusts/fixes the fixtures for the custom shape
	 * @throws PolygonCornerTooCloseException thrown when a resulting triangle polygon
	 * would become too small. NOTE: When this exception is thrown all fixtures
	 * have been removed and some might have been added. Fix the faulty corner
	 * and call #fixCustomShapeFixtures() again to fix this.
	 */
	private void fixCustomShapeFixtures() throws PolygonCornerTooCloseException {
		// Save fixture properties
		FixtureDef savedFixtureProperties = null;
		if (mFixtureDefs.size() >= 1) {
			savedFixtureProperties = mFixtureDefs.get(0);
		} else {
			savedFixtureProperties = getDefaultFixtureDef();
		}


		// Destroy previous fixture
		clearFixtures();


		// Create the new fixture
		// Polygon
		if (mVisualVars.corners.size() >= 3) {
			List<Vector2> triangles = null;
			if (mVisualVars.corners.size() == 3) {
				triangles = new ArrayList<Vector2>(mVisualVars.corners);
				Geometry.makePolygonCounterClockwise(triangles);
			} else {
				triangles = mEarClippingTriangulator.computeTriangles(mVisualVars.corners);
				// Always reverse, triangles should always be clockwise, whereas box2d needs
				// counter clockwise...
				Collections.reverse(triangles);
			}

			int cTriangles = triangles.size() / 3;
			Vector2[] triangleVertices = new Vector2[3];
			for (int i = 0; i < triangleVertices.length; ++i) {
				triangleVertices[i] = Pools.obtain(Vector2.class);
			}
			Vector2 lengthTest = Pools.obtain(Vector2.class);

			// Add the fixtures
			boolean cornerTooClose = false;
			for (int triangle = 0; triangle < cTriangles; ++triangle) {
				for (int vertex = 0; vertex < triangleVertices.length; ++vertex) {
					int offset = triangle * 3;
					triangleVertices[vertex].set(triangles.get(offset + vertex));
				}


				// Check so that the length between two corners isn't too small
				// 0 - 1
				lengthTest.set(triangleVertices[0]).sub(triangleVertices[1]);
				if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
					cornerTooClose = true;
					break;
				}
				// 0 - 2
				lengthTest.set(triangleVertices[0]).sub(triangleVertices[2]);
				if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
					cornerTooClose = true;
					break;
				}
				// 1 - 2
				lengthTest.set(triangleVertices[1]).sub(triangleVertices[2]);
				if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
					cornerTooClose = true;
					break;
				}


				FixtureDef fixtureDef = new FixtureDef();
				copyFixtureDef(savedFixtureProperties, fixtureDef);
				PolygonShape polygonShape = new PolygonShape();
				polygonShape.set(triangleVertices);
				fixtureDef.shape = polygonShape;
				addFixtureDef(fixtureDef);
			}

			// Free stuff
			for (int i = 0; i < triangleVertices.length; ++i) {
				Pools.free(triangleVertices[i]);
			}
			Pools.free(lengthTest);

			if (cornerTooClose) {
				throw new PolygonCornerTooCloseException();
			}
		}
		// Circle
		else if (mVisualVars.corners.size() >= 1) {
			CircleShape circle = new CircleShape();
			circle.setPosition(mVisualVars.corners.get(0));

			// One corner, use standard size
			if (mVisualVars.corners.size() == 1) {
				circle.setRadius(Config.Actor.Terrain.DEFAULT_CIRCLE_RADIUS);
			}
			// Else two corners, determine radius of circle
			else {
				Vector2 lengthVector = Pools.obtain(Vector2.class);
				lengthVector.set(mVisualVars.corners.get(0)).sub(mVisualVars.corners.get(1));
				circle.setRadius(lengthVector.len());
				Pools.free(lengthVector);
			}

			savedFixtureProperties.shape = circle;
			addFixtureDef(savedFixtureProperties);
		}
	}

	/**
	 * Checks if a line, that starts/ends from the specified index, intersects
	 * with any other line from the polygon.
	 * @param index the corner to check the lines from
	 * @return true if there is an intersection
	 */
	private boolean intersectionExists(int index) {
		if (mVisualVars.corners.size() < 3 || index < 0 || index >= mVisualVars.corners.size()) {
			return false;
		}


		// Get the two lines
		// First vertex is between before
		Vector2 vertexBefore = null;

		// If index is 0, get the last corner instead
		if (index == 0) {
			vertexBefore = mVisualVars.corners.get(mVisualVars.corners.size() - 1);
		} else {
			vertexBefore = mVisualVars.corners.get(index - 1);
		}

		// Vertex of index
		Vector2 vertexIndex = mVisualVars.corners.get(index);

		// Vertex after index
		Vector2 vertexAfter = null;

		// If index is the last, wrap and use first
		if (index == mVisualVars.corners.size() - 1) {
			vertexAfter = mVisualVars.corners.get(0);
		} else {
			vertexAfter = mVisualVars.corners.get(index + 1);
		}


		boolean intersects = false;
		// Using shape because the chain is looped, i.e. first and last is the same vertex
		for (int i = 0; i < mVisualVars.corners.size(); ++i) {
			// Skip checking index before and the current index, these are the lines
			// we are checking with... If index is 0 we need to wrap it
			if (i == index || i == index - 1 || (index == 0 && i == mVisualVars.corners.size() - 1)) {
				continue;
			}


			/** @TODO can be optimized if necessary. Swap instead of getting
			 * new vertexes all the time. */
			Vector2 lineA = mVisualVars.corners.get(i);
			Vector2 lineB = mVisualVars.corners.get(Geometry.computeNextIndex(mVisualVars.corners, i));


			// Check with first line
			if (Geometry.linesIntersectNoCorners(vertexBefore, vertexIndex, lineA, lineB)) {
				intersects = true;
				break;
			}

			// Check second line
			if (Geometry.linesIntersectNoCorners(vertexIndex, vertexAfter, lineA, lineB)) {
				intersects = true;
				break;
			}
		}


		return intersects;
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

	/**
	 * Sets the fixture def to the default values
	 * @param fixtureDef the fixture def to copy values from the default fixture def
	 */
	private void setDefaultFixtureValues(FixtureDef fixtureDef) {
		copyFixtureDef(getDefaultFixtureDef(), fixtureDef);
	}

	/**
	 * Copies the values from another fixture def to the specified
	 * @param fixtureDefOriginal the original fixture def to copy value FROM
	 * @param fixtureDefCopy the duplicate to copy value TO
	 */
	private void copyFixtureDef(FixtureDef fixtureDefOriginal, FixtureDef fixtureDefCopy) {
		fixtureDefCopy.density = fixtureDefOriginal.density;
		// Always skip filter, this will be set in actor...
		fixtureDefCopy.friction = fixtureDefOriginal.friction;
		fixtureDefCopy.isSensor = fixtureDefOriginal.isSensor;
		fixtureDefCopy.restitution = fixtureDefOriginal.restitution;
		fixtureDefCopy.shape = fixtureDefOriginal.shape;
	}

	/** Time when the definition was changed last time */
	private float mLastChangeTime = 0;
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
	/** Ear clipping triangulator for custom shapes */
	private static EarClippingTriangulator mEarClippingTriangulator = new EarClippingTriangulator();
}
