package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Actor.Pickup;
import com.spiddekauga.voider.Config.Editor.Bullet;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.EarClippingTriangulator;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;
import com.spiddekauga.voider.utils.Pools;

/**
 * Class for all shape variables
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class VisualVars implements KryoSerializable, Disposable, IResourceCorner {
	/**
	 * Sets the appropriate default values
	 * @param actorType the default values depends on which actor type is set
	 */
	VisualVars(ActorTypes actorType) {
		this();

		mActorType = actorType;
		setDefaultValues();
		createFixtureDef();
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(CLASS_REVISION, true);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		@SuppressWarnings("unused")
		int classRevision = input.readInt(true);

		calculateBoundingRadius();
		createFixtureDef();
	}

	/**
	 * Default constructor for Kryo
	 */
	protected VisualVars() {

	}

	/**
	 * Sets the default values of the visual vars depending on the current actor type
	 */
	private void setDefaultValues() {
		// Type specific settings
		switch (mActorType) {
		case ENEMY:
			mShapeType = Enemy.Visual.SHAPE_DEFAULT;
			mShapeCircleRadius = Enemy.Visual.RADIUS_DEFAULT;
			mShapeWidth = Enemy.Visual.SIZE_DEFAULT;
			mShapeHeight = Enemy.Visual.SIZE_DEFAULT;
			break;

		case BULLET:
			mShapeType = Bullet.Visual.SHAPE_DEFAULT;
			mShapeCircleRadius = Bullet.Visual.RADIUS_DEFAULT;
			mShapeWidth = Bullet.Visual.SIZE_DEFAULT;
			mShapeHeight = Bullet.Visual.SIZE_DEFAULT;
			break;

		case STATIC_TERRAIN:
			mShapeType = ActorShapeTypes.CUSTOM;
			mShapeCircleRadius = 0;
			mShapeWidth = 0;
			mShapeHeight = 0;
			break;

		case PICKUP:
			mShapeType = ActorShapeTypes.CIRCLE;
			mShapeCircleRadius = Pickup.RADIUS;
			mShapeHeight = 0;
			mShapeWidth = 0;
			break;

		case PLAYER:
			mShapeType = ActorShapeTypes.CIRCLE;
			mShapeCircleRadius = 1;
			mShapeHeight = 0;
			mShapeWidth = 0;
			break;

		default:
			if (Gdx.app != null) {
				Gdx.app.error("VisualVars", "Unknown actor type: " + mActorType);
			}
			mShapeType = ActorShapeTypes.CIRCLE;
			mShapeCircleRadius = 1;
			mShapeHeight = 1;
			mShapeWidth = 1;
			break;
		}
	}

	/**
	 * Sets the shape radius (only applicable for circle)
	 * @param radius new radius value
	 */
	public void setShapeRadius(float radius) {
		mShapeCircleRadius = radius;

		// Update fixture if circle
		if (mShapeType == ActorShapeTypes.CIRCLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createCircleShape();
			}

		}

		calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * @return shape radius of the circle
	 */
	public float getShapeRadius() {
		return mShapeCircleRadius;
	}

	/**
	 * Sets the shape width (only applicable for rectangle/triangle)
	 * @param width new width of the rectangle/triangle
	 */
	public void setShapeWidth(float width) {
		mShapeWidth = width;

		// Update fixture if rectangle/triangle
		if (mShapeType == ActorShapeTypes.RECTANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createRectangleShape();
			}
		} else if (mShapeType == ActorShapeTypes.TRIANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createTriangleShape();
			}
		}

		calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * @return shape width of rectangle/triangle
	 */
	public float getShapeWidth() {
		return mShapeWidth;
	}

	/**
	 * Sets the shape height (only applicable for rectangle/triangle)
	 * @param height new height of the rectangle/triangle
	 */
	public void setShapeHeight(float height) {
		mShapeHeight = height;

		// Update fixture if rectangle/triangle
		if (mShapeType == ActorShapeTypes.RECTANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createRectangleShape();
			}
		} else if (mShapeType == ActorShapeTypes.TRIANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createTriangleShape();
			}
		}

		calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * @return shape height of rectangle/triangle
	 */
	public float getShapeHeight() {
		return mShapeHeight;
	}

	/**
	 * @return current shape type of the enemy
	 */
	public ActorShapeTypes getShapeType() {
		return mShapeType;
	}

	@Override
	public void dispose() {
		Pools.vector2.freeAll(mCorners);
		Pools.vector2.free(mCenterOffset);
		mCenterOffset = null;
		clearVertices();

		clearFixtures();
		Pools.arrayList.freeAll(mCorners, mFixtureDefs);
		mCorners = null;
		mFixtureDefs = null;
	}

	/**
	 * Clears (and possibly frees) the vertices of the shape.
	 */
	public void clearVertices() {
		if (mVertices != null) {
			Pools.vector2.freeDuplicates(mVertices);
			Pools.arrayList.free(mVertices);
			mVertices = null;
		}
		if (mPolygon != null) {
			Pools.vector2.freeDuplicates(mPolygon);
			Pools.arrayList.free(mPolygon);
			mPolygon = null;
		}
	}

	/**
	 * @return bounding radius of the actor
	 */
	public float getBoundingRadius() {
		return mBoundingRadius;
	}

	/**
	 * Calculates the bounding radius
	 */
	private void calculateBoundingRadius() {
		switch (mShapeType) {
		case CIRCLE:
			mBoundingRadius = mShapeCircleRadius;
			if (!mCenterOffset.equals(Vector2.Zero)) {
				mBoundingRadius += mCenterOffset.len();
			}

			break;


		case RECTANGLE:
		case TRIANGLE:
		case CUSTOM: {
			Vector2 farthestAway = null;
			// Use corners
			if (mShapeType == ActorShapeTypes.CUSTOM) {
				farthestAway = Geometry.vertexFarthestAway(mCenterOffset, mCorners);
			}
			// Use vertices
			else {
				farthestAway = Geometry.vertexFarthestAway(mCenterOffset, mVertices);
			}

			if (farthestAway != null) {
				Vector2 diffVector = Pools.vector2.obtain();
				diffVector.set(mCenterOffset).sub(farthestAway);
				mBoundingRadius = diffVector.len();
				Pools.vector2.free(diffVector);
			} else {
				mBoundingRadius = 0;
			}
			break;
		}
		}
	}

	/**
	 * Set the color of the actor
	 * @param color new color
	 */
	public void setColor(Color color) {
		mColor = color;
	}

	/**
	 * @return color of the actor
	 */
	public Color getColor() {
		return mColor;
	}

	/**
	 * @return true if the shape is complete and can be rendered
	 */
	public boolean isComplete() {
		return mShapeComplete;
	}

	/**
	 * Sets the shape type of the enemy. This will clear the existing fixture shape
	 * for the enemy and created another one with default values.
	 * @param shapeType type of shape the enemy has
	 */
	public void setShapeType(ActorShapeTypes shapeType) {
		if (shapeType == null) {
			return;
		}


		clearVertices();

		mShapeType = shapeType;

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

		calculateBoundingRadius();

		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Adds another fixture definition
	 * @param fixtureDef new fixture def to add
	 */
	public void addFixtureDef(FixtureDef fixtureDef) {
		mFixtureDefs.add(fixtureDef);
		mFixtureChangeTime = GameTime.getTotalGlobalTimeElapsed();
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

	@Override
	public void addCorner(Vector2 corner) {
		addCorner(corner, mCorners.size());
	}

	@Override
	public void addCorner(Vector2 corner, int index) {
		mCorners.add(index, Pools.vector2.obtain().set(corner));
	}

	@Override
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mCorners.size(); ++i) {
			if (mCorners.get(i).equals(position)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public Vector2 removeCorner(int index) {
		return mCorners.remove(index);
	}

	@Override
	public void moveCorner(int index, Vector2 newPos) {
		mCorners.get(index).set(newPos);
	}

	@Override
	public int getCornerCount() {
		return mCorners.size();
	}

	/**
	 * @return all the corners of the actor
	 */
	public ArrayList<Vector2> getCorners() {
		return mCorners;
	}

	/**
	 * Sets the center offset for the fixtures
	 * @param centerOffset center offset position for fixtures
	 */
	public void setCenterOffset(Vector2 centerOffset) {
		mCenterOffset.set(centerOffset);

		//		// Special case for draw and circle...
		//		if (mShapeType == ActorShapeTypes.CUSTOM && mCorners.size() > 0 && mCorners.size() <= 2) {
		//			mCenterOffset.add(mCorners.get(0));
		//		}

		// Create new fixtures on the right place
		setShapeType(mShapeType);
	}

	/**
	 * Center the offset. This will set the offset to the middle of
	 * the fixture(s). It will NOT set the center to (0,0) (only if the actual
	 * center of the fixtures are there.
	 */
	public void resetCenterOffset() {
		Vector2 center = Pools.vector2.obtain();
		center.set(0, 0);

		switch (mShapeType) {
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
			if (mCorners.size() >= 3) {
				for (Vector2 vertex : mCorners) {
					center.sub(vertex);
				}

				center.div(mCorners.size());
			} else {
				center.set(0,0);
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
		return mCenterOffset;
	}

	/**
	 * Creates the fixture definition
	 */
	protected void createFixtureDef() {
		setShapeType(mShapeType);
	}

	/**
	 * Readjusts/fixes the fixtures for the custom shape
	 * would become too small. NOTE: When this exception is thrown all fixtures
	 * have been removed and some might have been added. Fix the faulty corner
	 * and call #fixCustomShapeFixtures() again to fix this.
	 * @throws PolygonComplexException if the method failed to make the polygon non-complex
	 * @throws PolygonCornersTooCloseException if some corners are too close
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
		clearVertices();


		// Create the new fixture
		// Polygon
		if (mCorners.size() >= 3) {
			ArrayList<Vector2> triangles = null;
			if (mCorners.size() == 3) {
				triangles = createCopy(mCorners);
				Geometry.makePolygonCounterClockwise(triangles);
			} else {
				ArrayList<Vector2> tempVertices = createCopy(mCorners);

				switch (Geometry.intersectionExists(tempVertices)) {
				case NONE:
					mShapeComplete = true;
					break;

				case INTERSECTS:
					mShapeComplete = false;
					handlePolygonComplexException(tempVertices, null);
					break;

				case INTERSECTS_WITH_LOOP:
					mShapeComplete = false;
					break;
				}


				try {
					triangles = mEarClippingTriangulator.computeTriangles(tempVertices);
					// Always reverse, triangles are always clockwise, whereas box2d needs
					// counter clockwise...
					Collections.reverse(triangles);
				} catch (PolygonComplexException e) {
					handlePolygonComplexException(tempVertices, e);
				}
				Pools.arrayList.free(tempVertices);
				tempVertices = null;
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
					triangleVertices[vertex].add(mCenterOffset);
				}


				// Check so that the length between two corners isn't too small
				// 0 - 1
				lengthTest.set(triangleVertices[0]).sub(triangleVertices[1]);
				if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
					cornerTooClose = true;
				} else {
					// 0 - 2
					lengthTest.set(triangleVertices[0]).sub(triangleVertices[2]);
					if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
						cornerTooClose = true;
					} else {
						// 1 - 2
						lengthTest.set(triangleVertices[1]).sub(triangleVertices[2]);
						if (lengthTest.len2() <= Config.Graphics.EDGE_LENGTH_MIN) {
							cornerTooClose = true;
						}
					}
				}

				String throwMessage = null;
				if (cornerTooClose) {
					throwMessage = "Corners too close, skipping (" + lengthTest.len() + ")";
				}
				// Check area
				else {
					float triangleArea = Geometry.calculateTriangleArea(triangleVertices[0], triangleVertices[1], triangleVertices[2]);

					// Make clockwise
					if (triangleArea < 0) {
						Collections.reverse(Arrays.asList(triangleVertices));
						triangleArea = -triangleArea;
						Gdx.app.log("ActorDef", "Fixture triangle negative area, reversing order...");
					}

					if (triangleArea <= Config.Graphics.POLYGON_AREA_MIN) {
						throwMessage = "Area too small: (" + triangleArea + ")";
					}
				}

				if (throwMessage != null) {
					Gdx.app.error("ActorDef", throwMessage);
					Pools.vector2.freeDuplicates(triangles);
					Pools.arrayList.free(triangles);
					triangles = null;
					Pools.vector2.free(lengthTest);
					lengthTest = null;
					Pools.vector2.freeAll(triangleVertices);
					triangleVertices = null;

					throw new PolygonCornersTooCloseException(throwMessage);
				}


				FixtureDef fixtureDef = new FixtureDef();
				copyFixtureDef(savedFixtureProperties, fixtureDef);
				PolygonShape polygonShape = new PolygonShape();
				polygonShape.set(triangleVertices);
				fixtureDef.shape = polygonShape;
				addFixtureDef(fixtureDef);
			}


			mVertices = triangles;

			// Free vertices stuff
			if (!mShapeComplete) {
				clearVertices();
			}

			// Free stuff
			Pools.vector2.freeAll(triangleVertices);
			Pools.vector2.free(lengthTest);
			triangleVertices = null;
			lengthTest = null;
		}
		// Circle
		else if (mCorners.size() >= 1) {
			CircleShape circle = new CircleShape();

			Vector2 offsetPosition = Pools.vector2.obtain();
			offsetPosition.set(0,0).sub(mCenterOffset).sub(mCorners.get(0));

			float radius = 0;

			// One corner, use standard size
			if (mCorners.size() == 1) {
				radius = Config.Actor.Terrain.DEFAULT_CIRCLE_RADIUS;
			}
			// Else two corners, determine radius of circle
			else {
				Vector2 lengthVector = Pools.vector2.obtain();
				lengthVector.set(mCorners.get(0)).sub(mCorners.get(1));
				radius = lengthVector.len();
				Pools.vector2.free(lengthVector);
				lengthVector = null;
			}
			circle.setRadius(radius);

			savedFixtureProperties.shape = circle;
			addFixtureDef(savedFixtureProperties);

			// Create vertices for the circle
			ArrayList<Vector2> circleVertices = Geometry.createCircle(radius);
			for (Vector2 vertex : circleVertices) {
				vertex.add(offsetPosition);
			}
			mVertices = mEarClippingTriangulator.computeTriangles(circleVertices);
			Collections.reverse(mVertices);

			Pools.vector2.free(offsetPosition);
			offsetPosition = null;
		}
	}

	/**
	 * @return triangle vertices for the current shape. Grouped together in groups of three to form a triangle.
	 */
	ArrayList<Vector2> getTriangleVertices() {
		return mVertices;
	}

	/**
	 * Creates a circle from the visual variables
	 * @return circle shape for fixture
	 */
	@SuppressWarnings("unchecked")
	private CircleShape createCircleShape() {
		clearVertices();

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(mShapeCircleRadius);
		/** @todo use center for all shapes */
		//		circleShape.setPosition(centerOffset);

		// Create vertices for the circle
		ArrayList<Vector2> circleVertices = Geometry.createCircle(mShapeCircleRadius);
		mVertices = mEarClippingTriangulator.computeTriangles(circleVertices);
		Collections.reverse(circleVertices);

		mPolygon = Pools.arrayList.obtain();
		for (Vector2 vertex : circleVertices) {
			mPolygon.add(Pools.vector2.obtain().set(vertex));
		}

		return circleShape;
	}

	/**
	 * Creates a rectangle shape from the visual variables
	 * @return rectangle shape for fixture
	 */
	@SuppressWarnings("unchecked")
	private PolygonShape createRectangleShape() {
		PolygonShape rectangleShape = new PolygonShape();

		float halfWidth = mShapeWidth * 0.5f;
		float halfHeight = mShapeHeight * 0.5f;

		/** @todo use center for all shapes */
		//		rectangleShape.setAsBox(halfWidth, halfHeight, centerOffset, 0);
		rectangleShape.setAsBox(halfWidth, halfHeight);

		clearVertices();

		// Create triangle vertices and polygon for the rectangle
		if (rectangleShape.getVertexCount() == 4) {
			mVertices = Pools.arrayList.obtain();
			mPolygon = Pools.arrayList.obtain();

			// First triangle
			Vector2 vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(0, vertex);
			mVertices.add(vertex);
			mPolygon.add(Pools.vector2.obtain().set(vertex));
			vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(1, vertex);
			mVertices.add(vertex);
			mPolygon.add(Pools.vector2.obtain().set(vertex));
			vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(2, vertex);
			mVertices.add(vertex);
			mPolygon.add(Pools.vector2.obtain().set(vertex));

			// Second triangle
			mVertices.add(vertex);
			vertex = Pools.vector2.obtain();
			rectangleShape.getVertex(3, vertex);
			mVertices.add(vertex);
			mPolygon.add(Pools.vector2.obtain().set(vertex));
			mVertices.add(mVertices.get(0));

		} else {
			Gdx.app.error("ActorDef", "Vertex count is not 4 in rectangle!");
		}

		return rectangleShape;
	}

	/**
	 * Creates a triangle shape from the visual variables
	 * @return triangle polygon used for fixture
	 */
	@SuppressWarnings("unchecked")
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
		vertices[0].x = - mShapeWidth * 0.5f;
		vertices[0].y = - mShapeHeight * 0.5f;

		// Middle right corner
		vertices[1].x = mShapeWidth * 0.5f;
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
			//			vertex.add(centerOffset);
		}

		PolygonShape polygonShape = new PolygonShape();
		polygonShape.set(vertices);

		Pools.vector2.free(center);
		center = null;

		// Set vertices and create border
		clearVertices();
		mVertices = Pools.arrayList.obtain();
		mPolygon = Pools.arrayList.obtain();
		for (Vector2 vertex : vertices) {
			mVertices.add(vertex);
			mPolygon.add(Pools.vector2.obtain().set(vertex));
		}


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
			Gdx.app.error("VisualVars", "Too few/many fixture definitions! " + fixtureDefs.size());
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
	 * @return polygon shape of the actor.
	 */
	public ArrayList<Vector2> getPolygonShape() {
		return mPolygon;
	}

	/**
	 * @return when this definition was changed that affects the fixtures. In global time.
	 */
	float getFixtureChangeTime() {
		return mFixtureChangeTime;
	}

	@Override
	public Vector2 getCornerPosition(int index) {
		return mCorners.get(index);
	}

	/**
	 * @return default fixture definition for all actors
	 */
	private FixtureDef getDefaultFixtureDef() {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;
		fixtureDef.density = 0.001f;
		return fixtureDef;
	}

	/**
	 * Creates a copy of the list
	 * @param vertices all vertices to create a copy of
	 * @return new array list with a copy of the vertices
	 */
	private ArrayList<Vector2> createCopy(ArrayList<Vector2> vertices) {
		@SuppressWarnings("unchecked")
		ArrayList<Vector2> copy = Pools.arrayList.obtain();

		for (Vector2 vertex : vertices) {
			copy.add(Pools.vector2.obtain().set(vertex));
		}

		return copy;
	}

	/**
	 * Handles PolygonComplexException for #fixCustomShapeFixtures()
	 * @param tempVertices
	 * @param exception if null it will throw a new exception, else it will re-throw
	 * the exception.
	 */
	private void handlePolygonComplexException(ArrayList<Vector2> tempVertices, PolygonComplexException exception) {
		Pools.vector2.freeAll(tempVertices);
		Pools.arrayList.free(tempVertices);
		tempVertices = null;

		if (exception == null) {
			throw new PolygonComplexException();
		} else {
			throw exception;
		}
	}

	/**
	 * @return actor type of the visual vars
	 */
	public ActorTypes getActorType() {
		return mActorType;
	}


	/** Color of the actor */
	@Tag(52) private Color mColor = new Color();
	/** Current shape of the enemy */
	@Tag(49) private ActorShapeTypes mShapeType = null;
	/** radius of circle */
	@Tag(60) private float mShapeCircleRadius;
	/** width of rectangle/triangle */
	@Tag(61) private float mShapeWidth;
	/** height of rectangle/triangle */
	@Tag(62) private float mShapeHeight;
	/** Center offset for fixtures */
	@Tag(51) private Vector2 mCenterOffset = Pools.vector2.obtain().set(0,0);
	/** Corners of polygon, used for custom shapes */
	@SuppressWarnings("unchecked")
	@Tag(63) private ArrayList<Vector2> mCorners = Pools.arrayList.obtain();
	/** Array list of the polygon figure, this contains the vertices but not
	 * in triangles. Used for when creating a border of some kind */
	private ArrayList<Vector2> mPolygon = null;
	/** Triangle vertices.
	 * To easier render the shape. No optimization has been done to reduce
	 * the number of vertices. */
	private ArrayList<Vector2> mVertices = null;
	/** True if shape is drawable/complete */
	private boolean mShapeComplete = true;
	/** Defines the mass, shape, etc. */
	@SuppressWarnings("unchecked")
	private ArrayList<FixtureDef> mFixtureDefs = Pools.arrayList.obtain();
	/** Radius of the actor, or rather circle bounding box */
	private float mBoundingRadius = 0;
	/** Actor type, used for setting default values */
	@Tag(50) private ActorTypes mActorType = null;
	/** Time when the fixture was changed last time */
	protected float mFixtureChangeTime = 0;
	/** Class structure revision */
	protected final int CLASS_REVISION = 1;

	/** Ear clipping triangulator for custom shapes */
	private static EarClippingTriangulator mEarClippingTriangulator = new EarClippingTriangulator();
}
