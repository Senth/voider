package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.EarClippingTriangulator;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * Definition of the actor. This include common attribute for a common type of actor.
 * E.g. A specific enemy will have the same variables here. The only thing changed during
 * it's life is the variables in the Actor class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorDef extends Def implements Json.Serializable, Disposable, IResourceCorner {
	/**
	 * Sets the visual variable to the specified type
	 * @param actorType the actor type to which set the default values of
	 * the visual variables
	 */
	protected ActorDef(ActorTypes actorType) {
		mVisualVars = new VisualVars(actorType);

		createFixtureDef();

		/** @todo remove default color */
		switch (actorType) {
		case BULLET:
			setColor(new Color(0.8f, 0.5f, 0, 1));
			break;

		case ENEMY:
			setColor(new Color(1, 0, 0, 1));
			break;

		case PICKUP:
			setColor(new Color(1, 1, 0, 1));
			break;

		case PLAYER:
			setColor(new Color(0, 1, 0, 1));
			break;

		case STATIC_TERRAIN:
			setColor(new Color(0, 0.75f, 0, 1));
			break;

		default:
			setColor(new Color(1, 1, 1, 1));
			break;
		}
	}

	/**
	 * Default constructor for JSON
	 */
	@SuppressWarnings("unused")
	private ActorDef() {
		// Does nothing
	}

	/**
	 * Sets the starting angle of the actor
	 * @param angle the starting angle
	 */
	public void setStartAngle(float angle) {
		getBodyDef().angle = MathUtils.degreesToRadians * angle;

		mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * @return starting angle of the actor (in degrees)
	 */
	public float getStartAngle() {
		return getBodyDef().angle;
	}

	/**
	 * Sets the collision damage of the actor
	 * If the actor is set to be destroyed on collision ({@link #setDestroyOnCollide(boolean)})
	 * it will decrease the full collisionDamage from the other actor and not per second.
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
		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
	 * Set the color of the actor
	 * @param color new color
	 */
	public void setColor(Color color) {
		mVisualVars.color.set(color);

		resetBorderColor();
	}

	/**
	 * @return color of the actor
	 */
	public Color getColor() {
		return mVisualVars.color;
	}

	/**
	 * @return border color
	 */
	public Color getBorderColor() {
		return mVisualVars.borderColor;
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
		copy.angularVelocity = mBodyDef.angularVelocity;
		copy.awake = mBodyDef.awake;
		copy.bullet = mBodyDef.bullet;
		copy.fixedRotation = mBodyDef.fixedRotation;
		copy.gravityScale = mBodyDef.gravityScale;
		copy.linearDamping = mBodyDef.linearDamping;
		copy.linearVelocity.set(mBodyDef.linearVelocity);
		copy.position.set(mBodyDef.position);
		copy.type = mBodyDef.type;
		return copy;
	}

	@Override
	public void dispose() {
		clearFixtures();
		mVisualVars.dispose();
	}

	/**
	 * Sets the shape type of the enemy. This will clear the existing fixture shape
	 * for the enemy and created another one with default values.
	 * @param shapeType type of shape the enemy has
	 */
	public void setShapeType(ActorShapeTypes shapeType) {
		mVisualVars.clearVertices();

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
			case CIRCLE:
				fixtureDef.shape = createCircleShape();
				break;


			case RECTANGLE:
				fixtureDef.shape = createRectangleShape();
				break;


			case TRIANGLE:
				fixtureDef.shape = createTriangleShape();
				break;


			case CUSTOM:
				fixCustomShapeFixtures();
				break;
			}

		} else {
			Gdx.app.error("EnemyActorDef", "FixtureDef null at setShapeType()");
		}

		mVisualVars.calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
				fixtureDef.shape.dispose();
				fixtureDef.shape = createCircleShape();
			}

		}

		mVisualVars.calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
		if (mVisualVars.shapeType == ActorShapeTypes.RECTANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createRectangleShape();
			}
		} else if (mVisualVars.shapeType == ActorShapeTypes.TRIANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createTriangleShape();
			}
		}

		mVisualVars.calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
		if (mVisualVars.shapeType == ActorShapeTypes.RECTANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createRectangleShape();
			}
		} else if (mVisualVars.shapeType == ActorShapeTypes.TRIANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createTriangleShape();
			}
		}

		mVisualVars.calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
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
	 * Sets the rotation speed of the actor. This might not work for
	 * some actors that rotate the actor on their own...
	 * @param rotationSpeed new rotation speed of the actor.
	 */
	public void setRotationSpeed(float rotationSpeed) {
		getBodyDef().angularVelocity = rotationSpeed;
		mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * @return rotation speed of the actor
	 */
	public float getRotationSpeed() {
		return getBodyDef().angularVelocity;
	}

	@Override
	public void addCorner(Vector2 corner) {
		addCorner(corner, mVisualVars.corners.size());
	}

	@Override
	public void addCorner(Vector2 corner, int index) {
		mVisualVars.corners.add(index, corner.cpy());

		//		// Make sure no intersection exist
		//		if (intersectionExists(index)) {
		//			mVisualVars.corners.remove(index);
		//			throw new PolygonComplexException();
		//		}
		//
		//		try {
		//			fixCustomShapeFixtures();
		//		} catch (PolygonCornerTooCloseException e) {
		//			mVisualVars.corners.remove(index);
		//
		//			// Reset to old fixtures
		//			try {
		//				fixCustomShapeFixtures();
		//			} catch (PolygonCornerTooCloseException e1) {
		//				Gdx.app.error("ActorDef", "Could not fix custom shape fixtures!");
		//			}
		//
		//			throw e;
		//		}
		//		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	@Override
	public Vector2 removeCorner(int index) {
		Vector2 removedPosition = null;
		if (index >= 0 && index < mVisualVars.corners.size()) {
			mVisualVars.clearVertices();

			removedPosition = mVisualVars.corners.remove(index);

			fixCustomShapeFixtures();
		}
		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();

		return removedPosition;
	}

	@Override
	public void moveCorner(int index, Vector2 newPos) {
		mVisualVars.corners.get(index).set(newPos);
	}

	@Override
	public int getCornerCount() {
		return mVisualVars.corners.size();
	}

	/**
	 * @return all the corners of the actor
	 */
	public ArrayList<Vector2> getCorners() {
		return mVisualVars.corners;
	}

	/**
	 * Sets the center offset for the fixtures
	 * @param centerOffset center offset position for fixtures
	 */
	public void setCenterOffset(Vector2 centerOffset) {
		mVisualVars.centerOffset.set(centerOffset);

		// Create new fixtures on the right place
		setShapeType(mVisualVars.shapeType);
	}

	/**
	 * Center the offset. This will set the offset to the middle of
	 * the fixture(s). It will NOT set the center to (0,0) (only if the actual
	 * center of the fixtures are there.
	 */
	public void resetCenterOffset() {
		Vector2 center = Pools.vector2.obtain();
		center.set(0, 0);

		switch (mVisualVars.shapeType) {
		case CIRCLE:
			/** @todo implement reset center for circle */
			break;


		case RECTANGLE:
			/** @todo implement reset center for rectangle */
			break;


		case TRIANGLE:
			/** @todo implement reset center for triangle */
			break;


		case CUSTOM:
			// Polygon, calculate center
			if (mVisualVars.corners.size() >= 3) {
				for (Vector2 vertex : mVisualVars.corners) {
					center.sub(vertex);
				}

				center.div(mVisualVars.corners.size());
			}
			break;
		}

		setCenterOffset(center);
		Pools.vector2.free(center);

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * @return center offset for the fixtures
	 */
	public Vector2 getCenterOffset() {
		return mVisualVars.centerOffset;
	}

	/**
	 * Sets whether this actor shall be destroyed on collision.
	 * If this actor has any collision damage set to it, it will decrease the
	 * other actors health with the whole amount instead of per second if this
	 * is set to true.
	 * @param destroyOnCollision set to true to destroy the actor on collision
	 */
	public void setDestroyOnCollide(boolean destroyOnCollision) {
		mDestroyOnCollide = destroyOnCollision;
	}

	/**
	 * @return true if this actor shall be destroyed on collision
	 */
	public boolean shallDestroyOnCollide() {
		return mDestroyOnCollide;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		// Write ActorDef's variables first
		json.writeValue("mMaxLife", mMaxLife);
		json.writeValue("mBodyDef", mBodyDef);
		json.writeValue("mCollisionDamage", mCollisionDamage);
		json.writeValue("mDestroyOnCollide", mDestroyOnCollide);
		json.writeValue("mVisualVars", mVisualVars);

	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);


		// Our variables
		mMaxLife = json.readValue("mMaxLife", float.class, jsonData);
		mBodyDef = json.readValue("mBodyDef", BodyDef.class, jsonData);
		mCollisionDamage = json.readValue("mCollisionDamage", float.class, jsonData);
		mDestroyOnCollide = json.readValue("mDestroyOnCollide", boolean.class, jsonData);
		mVisualVars = json.readValue("mVisualVars", VisualVars.class, jsonData);

		resetBorderColor();

		createFixtureDef();
	}

	@Override
	public Vector2 getCornerPosition(int index) {
		return mVisualVars.corners.get(index);
	}

	/**
	 * @return default fixture definition.
	 */
	protected abstract FixtureDef getDefaultFixtureDef();

	/**
	 * @return when this definition was changed that affects the fixtures. In global time.
	 */
	float getFixtureChangeTime() {
		return mFixtureChangeTime;
	}

	/**
	 * @return when this definition was changed that affects the body.
	 */
	float getBodyChangeTime() {
		return mBodyChangeTime;
	}

	/**
	 * Creates the fixture definition
	 */
	protected void createFixtureDef() {
		setShapeType(mVisualVars.shapeType);
	}

	/**
	 * Readjusts/fixes the fixtures for the custom shape
	 * would become too small. NOTE: When this exception is thrown all fixtures
	 * have been removed and some might have been added. Fix the faulty corner
	 * and call #fixCustomShapeFixtures() again to fix this.
	 */
	public void fixCustomShapeFixtures() {
		// Save fixture properties
		FixtureDef savedFixtureProperties = null;
		if (mFixtureDefs.size() >= 1) {
			savedFixtureProperties = mFixtureDefs.get(0);
		} else {
			savedFixtureProperties = getDefaultFixtureDef();
		}


		// Destroy previous fixtures
		clearFixtures();

		mVisualVars.clearVertices();


		// Create the new fixture
		// Polygon
		if (mVisualVars.corners.size() >= 3) {
			ArrayList<Vector2> triangles = null;
			ArrayList<Vector2> createdVertices = null;
			if (mVisualVars.corners.size() == 3) {
				triangles = new ArrayList<Vector2>(mVisualVars.corners);
				Geometry.makePolygonCounterClockwise(triangles);
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<Vector2> tempVertices = Pools.arrayList.obtain();
				tempVertices.clear();
				tempVertices.addAll(mVisualVars.corners);
				createdVertices = Geometry.makePolygonNonComplex(tempVertices);

				triangles = mEarClippingTriangulator.computeTriangles(tempVertices);
				// Always reverse, triangles are always clockwise, whereas box2d needs
				// counter clockwise...
				Collections.reverse(triangles);

				Pools.arrayList.free(tempVertices);
			}

			int cTriangles = triangles.size() / 3;
			Vector2[] triangleVertices = new Vector2[3];
			for (int i = 0; i < triangleVertices.length; ++i) {
				triangleVertices[i] = Pools.vector2.obtain();
			}
			Vector2 lengthTest = Pools.vector2.obtain();

			// Add the fixtures
			boolean cornerTooClose = false;
			for (int triangle = 0; triangle < cTriangles; ++triangle) {
				int offset = triangle * 3;
				for (int vertex = 0; vertex < triangleVertices.length; ++vertex) {
					triangleVertices[vertex].set(triangles.get(offset + vertex));
					triangleVertices[vertex].add(mVisualVars.centerOffset);
				}


				// Check so that the length between two corners isn't too small
				// 0 - 1
				lengthTest.set(triangleVertices[0]).sub(triangleVertices[1]);
				if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
					cornerTooClose = true;
					continue;
				}
				// 0 - 2
				lengthTest.set(triangleVertices[0]).sub(triangleVertices[2]);
				if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
					cornerTooClose = true;
					continue;
				}
				// 1 - 2
				lengthTest.set(triangleVertices[1]).sub(triangleVertices[2]);
				if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
					cornerTooClose = true;
					continue;
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
				Pools.vector2.free(triangleVertices[i]);
			}
			Pools.vector2.free(lengthTest);
			if (createdVertices != null) {
				Pools.vector2.freeAll(createdVertices);
				Pools.arrayList.free(createdVertices);
			}

			if (cornerTooClose) {
				Gdx.app.error("ActorDef", "Polygon corners are too close...");
			}

			mVisualVars.vertices = triangles;
			createBorder(mVisualVars.corners);
		}
		// Circle
		else if (mVisualVars.corners.size() >= 1) {
			CircleShape circle = new CircleShape();

			Vector2 offsetPosition = Pools.vector2.obtain();
			offsetPosition.set(mVisualVars.corners.get(0)).add(mVisualVars.centerOffset);
			circle.setPosition(offsetPosition);
			Pools.vector2.free(offsetPosition);

			float radius = 0;

			// One corner, use standard size
			if (mVisualVars.corners.size() == 1) {
				circle.setRadius(Config.Actor.Terrain.DEFAULT_CIRCLE_RADIUS);
				radius = Config.Actor.Terrain.DEFAULT_CIRCLE_RADIUS;
			}
			// Else two corners, determine radius of circle
			else {
				Vector2 lengthVector = Pools.vector2.obtain();
				lengthVector.set(mVisualVars.corners.get(0)).sub(mVisualVars.corners.get(1));
				radius = lengthVector.len();
				circle.setRadius(radius);
				Pools.vector2.free(lengthVector);
			}

			savedFixtureProperties.shape = circle;
			addFixtureDef(savedFixtureProperties);


			// Set AABB box
			//			mAabbBox.setFromCircle(circle.getPosition(), circle.getRadius());

			// Create vertices for the circle
			ArrayList<Vector2> circleVertices = Geometry.createCircle(radius);
			mVisualVars.vertices = mEarClippingTriangulator.computeTriangles(circleVertices);
			Collections.reverse(mVisualVars.vertices);

			createBorder(circleVertices);
		}
	}

	/**
	 * @return triangle vertices for the current shape. Grouped together in groups of three to form a triangle.
	 */
	ArrayList<Vector2> getTriangleVertices() {
		return mVisualVars.vertices;
	}

	/**
	 * @return border triangle vertices. Grouped together in groups of three vertices to form a triangle.
	 */
	ArrayList<Vector2> getTriangleBorderVertices() {
		return mVisualVars.borderVertices;
	}

	/**
	 * Creates a circle from the visual variables
	 * @return circle shape for fixture
	 */
	private CircleShape createCircleShape() {
		mVisualVars.clearVertices();

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(mVisualVars.shapeCircleRadius);
		/** @todo use center for all shapes */
		//		circleShape.setPosition(mVisualVars.centerOffset);

		// Set AABB box
		//		mAabbBox.setFromCircle(circleShape.getPosition(), circleShape.getRadius());


		// Create vertices for the circle
		ArrayList<Vector2> circleVertices = Geometry.createCircle(mVisualVars.shapeCircleRadius);
		mVisualVars.vertices = mEarClippingTriangulator.computeTriangles(circleVertices);
		Collections.reverse(circleVertices);

		mVisualVars.polygon = circleVertices;

		createBorder(circleVertices);


		return circleShape;
	}

	/**
	 * Creates a rectangle shape from the visual variables
	 * @return rectangle shape for fixture
	 */
	private PolygonShape createRectangleShape() {
		PolygonShape rectangleShape = new PolygonShape();

		float halfWidth = mVisualVars.shapeWidth * 0.5f;
		float halfHeight = mVisualVars.shapeHeight * 0.5f;

		/** @todo use center for all shapes */
		//		rectangleShape.setAsBox(halfWidth, halfHeight, mVisualVars.centerOffset, 0);
		rectangleShape.setAsBox(halfWidth, halfHeight);

		// Set AABB box
		//		mAabbBox.setFromBox(mVisualVars.centerOffset, halfWidth, halfHeight);
		//		mAabbBox.setFromBox(new Vector2(), halfWidth, halfHeight);

		mVisualVars.clearVertices();

		// Create triangle vertices and polygon for the rectangle
		if (rectangleShape.getVertexCount() == 4) {
			mVisualVars.polygon = new ArrayList<Vector2>();

			// First triangle
			Vector2 vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(0, vertex);
			mVisualVars.vertices.add(vertex);
			mVisualVars.polygon.add(vertex);
			vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(1, vertex);
			mVisualVars.vertices.add(vertex);
			mVisualVars.polygon.add(vertex);
			vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(2, vertex);
			mVisualVars.vertices.add(vertex);
			mVisualVars.polygon.add(vertex);

			// Second triangle
			mVisualVars.vertices.add(vertex);
			vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(3, vertex);
			mVisualVars.vertices.add(vertex);
			mVisualVars.polygon.add(vertex);
			mVisualVars.vertices.add(mVisualVars.vertices.get(0));

		} else {
			Gdx.app.error("ActorDef", "Vertex count is not 4 in rectangle!");
		}

		/** @todo create border for rectangle */

		return rectangleShape;
	}

	/**
	 * Creates a triangle shape from the visual variables
	 * @return triangle polygon used for fixture
	 */
	private PolygonShape createTriangleShape() {
		Vector2[] vertices = new Vector2[3];

		for (int i = 0; i < vertices.length; ++i) {
			vertices[i] = Pools.vector2.obtain();
		}

		// It will look something like this:
		// | \
		// |   >
		// | /

		// Lower left corner
		vertices[0].x = - mVisualVars.shapeWidth * 0.5f;
		vertices[0].y = - mVisualVars.shapeHeight * 0.5f;

		// Middle right corner
		vertices[1].x = mVisualVars.shapeWidth * 0.5f;
		vertices[1].y = 0;

		// Upper left corner
		vertices[2].x = vertices[0].x;
		vertices[2].y = - vertices[0].y;


		// Set the center...
		Vector2 center = Pools.vector2.obtain();
		center.set(vertices[0]).add(vertices[1]).add(vertices[2]).div(3);

		// Offset all vertices with negative center
		for (Vector2 vertex : vertices) {
			vertex.sub(center);
			/** @todo use center for all shapes */
			//			vertex.add(mVisualVars.centerOffset);
		}


		// Set AABB box
		//		mAabbBox.setFromPolygon(vertices);


		PolygonShape polygonShape = new PolygonShape();
		polygonShape.set(vertices);

		Pools.vector2.free(center);

		// Set vertices and create border
		mVisualVars.clearVertices();
		mVisualVars.polygon = new ArrayList<Vector2>();
		for (Vector2 vertex : vertices) {
			mVisualVars.vertices.add(vertex);
			mVisualVars.polygon.add(vertex);
		}
		createBorder(mVisualVars.vertices);


		return polygonShape;
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

	/**
	 * Resets the border color
	 */
	private void resetBorderColor() {
		mVisualVars.borderColor.set(mVisualVars.color);
		mVisualVars.borderColor.mul(0.75f, 0.75f, 0.75f, 1);
	}

	/**
	 * @return polygon shape of the actor.
	 */
	public ArrayList<Vector2> getPolygonShape() {
		return mVisualVars.polygon;
	}

	/**
	 * @return bounding radius of the actor
	 */
	public float getBoundingRadius() {
		return mVisualVars.getBoundingRadius();
	}

	/**
	 * Creates the border for the actor
	 * @param corners vertices for all the corners
	 */
	private void createBorder(ArrayList<Vector2> corners) {
		ArrayList<Vector2> borderCorners = Geometry.createdBorderCorners(corners, true, Config.Actor.BORDER_WIDTH);

		boolean allBordersInsidePolygon = true;
		Iterator<Vector2> borderVertexIt = borderCorners.iterator();
		while (borderVertexIt.hasNext() && allBordersInsidePolygon) {
			Vector2 borderVertex = borderVertexIt.next();
			boolean withinTriangle = false;
			for (int i = 0; i < mVisualVars.vertices.size(); i += 3) {
				if (Geometry.isPointWithinTriangle(borderVertex, mVisualVars.vertices, i)) {
					withinTriangle = true;
					break;
				}
			}

			if (!withinTriangle) {
				allBordersInsidePolygon = false;
			}
		}


		if (allBordersInsidePolygon) {
			mVisualVars.borderVertices = Geometry.createBorderVertices(corners, borderCorners);
		} else {
			Pools.vector2.freeAll(borderCorners);
		}
	}

	/** Time when the fixture was changed last time */
	protected float mFixtureChangeTime = 0;
	/** When the body was changed last time */
	protected float mBodyChangeTime = 0;
	/** Defines the mass, shape, etc. */
	private ArrayList<FixtureDef> mFixtureDefs = new ArrayList<FixtureDef>();
	/** Maximum life of the actor, usually starting amount of life */
	private float mMaxLife = 0;
	/** The body definition of the actor */
	private BodyDef mBodyDef = new BodyDef();

	/** Collision damage (per second) */
	private float mCollisionDamage = 0;
	/** If this actor shall be destroy on collision */
	private boolean mDestroyOnCollide = false;
	/** Visual variables */
	private VisualVars mVisualVars = null;
	/** Ear clipping triangulator for custom shapes */
	private static EarClippingTriangulator mEarClippingTriangulator = new EarClippingTriangulator();
}
