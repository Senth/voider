package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.kryo.KryoPreWrite;
import com.spiddekauga.utils.kryo.KryoTaggedCopyable;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Actor.Pickup;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.repo.resource.SkinNames.GeneralImages;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceChangeListener;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.BoundingBox;
import com.spiddekauga.voider.utils.EarClippingTriangulator;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Geometry.PolygonAreaTooSmallException;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;
import com.spiddekauga.voider.utils.Geometry.PolygonException;
import com.spiddekauga.voider.utils.Graphics;

/**
 * Class for all shape variables
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Shape implements KryoSerializable, KryoTaggedCopyable, Disposable, IResourceCorner, KryoPreWrite {
	/**
	 * Sets the appropriate default values
	 * @param actorType the default values depends on which actor type is set
	 */
	Shape(ActorTypes actorType) {
		mActorType = actorType;

		setDefaultValues();
		createFixtureDef();
	}

	@Override
	public void preWrite() {
		mClassRevision = CLASS_REVISION;
	}

	@Override
	public void write(Kryo kryo, Output output) {
	}

	@Override
	public void read(Kryo kryo, Input input) {
		// Read old school class revision
		if (mClassRevision == 0) {
			mClassRevision = input.readInt(true);
		}

		// 1 -> 2
		if (mClassRevision <= 1) {
			// Fix image name
			if (unused1 instanceof GeneralImages) {
				GeneralImages generalImages = (GeneralImages) unused1;
				if (generalImages.ordinal() == 15) {
					mDrawName = DrawImages.SHUTTLE_LARGE;
				}
			}
		}

		setDefaultFixtureValues();
		createFixtureDef();
		calculateBounds();
	}

	@Override
	public void copy(Object fromOriginal) {
		setDefaultFixtureValues();
		createFixtureDef();
		calculateBounds();
	}

	/**
	 * Set default fixture values
	 */
	private void setDefaultFixtureValues() {
		IC_Visual icVisual = ConfigIni.getInstance().editor.actor.getVisual(mActorType);
		mDensity = icVisual.getDensityDefault();
		mFriction = icVisual.getFrictionDefault();
		mElasticity = icVisual.getElasticityDefault();
	}

	/**
	 * Default constructor for Kryo
	 */
	protected Shape() {

	}

	/**
	 * Sets the default values of the visual vars depending on the current actor type
	 */
	private void setDefaultValues() {
		IC_Visual icVisual = null;

		// Type specific settings
		switch (mActorType) {
		case ENEMY:
			icVisual = ConfigIni.getInstance().editor.enemy.visual;
			break;

		case BULLET:
			icVisual = ConfigIni.getInstance().editor.bullet.visual;
			break;

		case STATIC_TERRAIN:
			mType = ActorShapeTypes.CUSTOM;
			mCircleRadius = 0;
			mWidth = 0;
			mHeight = 0;
			break;

		case PICKUP:
			mType = ActorShapeTypes.CIRCLE;
			mCircleRadius = Pickup.RADIUS;
			mHeight = 0;
			mWidth = 0;
			break;

		case PLAYER:
			icVisual = ConfigIni.getInstance().editor.ship.visual;
			break;

		default:
			icVisual = ConfigIni.getInstance().editor.actor.visual;
			if (Gdx.app != null) {
				Gdx.app.error("VisualVars", "Unknown actor type: " + mActorType);
			}
			break;
		}

		if (icVisual != null) {
			mCircleRadius = icVisual.getRadiusDefault();
			mHeight = icVisual.getSizeDefault();
			mWidth = icVisual.getSizeDefault();
			mDensity = icVisual.getDensityDefault();
			mFriction = icVisual.getFrictionDefault();
			mElasticity = icVisual.getElasticityDefault();
			setShapeType(icVisual.getShapeDefault());
		}

	}

	/**
	 * Sets the shape radius (only applicable for circle)
	 * @param radius new radius value
	 */
	public void setShapeRadius(float radius) {
		mCircleRadius = radius;

		// Update fixture if circle
		if (mType == ActorShapeTypes.CIRCLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createCircleShape();
			}

		}

		calculateBounds();
		fixtureChanged();
	}

	/**
	 * @return shape radius of the circle
	 */
	public float getShapeRadius() {
		return mCircleRadius;
	}

	/**
	 * Sets the shape width (only applicable for rectangle/triangle)
	 * @param width new width of the rectangle/triangle
	 */
	public void setShapeWidth(float width) {
		mWidth = width;

		// Update fixture if rectangle/triangle
		if (mType == ActorShapeTypes.RECTANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createRectangleShape();
			}
		} else if (mType == ActorShapeTypes.TRIANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createTriangleShape();
			}
		}

		calculateBounds();
		fixtureChanged();
	}

	/**
	 * @return shape width of rectangle/triangle
	 */
	public float getShapeWidth() {
		return mWidth;
	}

	/**
	 * Sets the shape height (only applicable for rectangle/triangle)
	 * @param height new height of the rectangle/triangle
	 */
	public void setShapeHeight(float height) {
		mHeight = height;

		// Update fixture if rectangle/triangle
		if (mType == ActorShapeTypes.RECTANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createRectangleShape();
			}
		} else if (mType == ActorShapeTypes.TRIANGLE) {
			FixtureDef fixtureDef = getFirstFixtureDef();

			if (fixtureDef != null) {
				fixtureDef.shape.dispose();
				fixtureDef.shape = createTriangleShape();
			}
		}

		calculateBounds();
		fixtureChanged();
	}

	/**
	 * @return shape height of rectangle/triangle
	 */
	public float getShapeHeight() {
		return mHeight;
	}

	/**
	 * @return current shape type of the enemy
	 */
	public ActorShapeTypes getShapeType() {
		return mType;
	}

	@Override
	public void dispose() {
		mCenterOffset = null;
		clearVertices();

		clearFixtures();
		disposeImage();
	}

	/**
	 * Clears (and possibly frees) the vertices of the shape.
	 */
	public void clearVertices() {
		mVertices.clear();
		mPolygon.clear();
	}

	/**
	 * @return bounding radius of the actor
	 */
	public float getBoundingRadius() {
		return mBoundingRadius;
	}

	/**
	 * @return bounding box
	 */
	public BoundingBox getBoundingBox() {
		return mBoundingBox;
	}

	/**
	 * @return width (bounding box width) of the actor
	 */
	public float getWidth() {
		return mBoundingBox.getRight() - mBoundingBox.getLeft();
	}

	/**
	 * @return height (bounding box height) of the actor
	 */
	public float getHeight() {
		return mBoundingBox.getTop() - mBoundingBox.getBottom();
	}

	/**
	 * Calculate bounding box and radius bounds
	 */
	public void calculateBounds() {
		calculateBoundingRadius();
		calculateBoundingBox();
	}

	/**
	 * Calculate bounding box
	 */
	private void calculateBoundingBox() {
		mBoundingBox = Geometry.getBoundingBox(mPolygon, mStartAngle, mCenterOffset);

		// Offset bounding box with the center
		mBoundingBox.offset(mCenterOffset);
	}

	/**
	 * Calculates the bounding radius
	 */
	private void calculateBoundingRadius() {
		Vector2 farthestAway = null;

		switch (mType) {
		case CIRCLE:
			mBoundingRadius = mCircleRadius;
			if (!mCenterOffset.equals(Vector2.Zero)) {
				mBoundingRadius += mCenterOffset.len();
			}

			break;

		case RECTANGLE:
		case TRIANGLE:
			farthestAway = Geometry.vertexFarthestAway(mCenterOffset, mVertices);
			break;

		case IMAGE:
		case CUSTOM:
			farthestAway = Geometry.vertexFarthestAway(mCenterOffset, mCorners);
			break;
		}

		if (farthestAway != null) {
			Vector2 diffVector = new Vector2();
			diffVector.set(mCenterOffset).add(farthestAway);
			mBoundingRadius = diffVector.len();
		} else {
			mBoundingRadius = 0;
		}
	}

	/**
	 * Set the color of the actor
	 * @param color new color
	 */
	public void setColor(Color color) {
		mColor.set(color);
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
	 * Sets the shape type of the enemy. This will clear the existing fixture shape for
	 * the enemy and created another one with default values.
	 * @param shapeType type of shape the enemy has
	 */
	public void setShapeType(ActorShapeTypes shapeType) {
		if (shapeType == null || Debug.JUNIT_TEST) {
			return;
		}


		clearVertices();
		clearFixtures();

		mType = shapeType;

		addFixtureDef(getDefaultFixtureDef());

		FixtureDef fixtureDef = getFirstFixtureDef();

		if (fixtureDef != null) {
			// Create the new shape
			switch (shapeType) {
			case CIRCLE:
				fixtureDef.shape = createCircleShape();
				calculateBounds();
				break;


			case RECTANGLE:
				fixtureDef.shape = createRectangleShape();
				calculateBounds();
				break;


			case TRIANGLE:
				fixtureDef.shape = createTriangleShape();
				calculateBounds();
				break;


			case CUSTOM:
				fixCustomShapeFixtures();
				break;

			case IMAGE:
				fixCustomShapeFixtures();
				resetCenterOffset();
				break;
			}
		} else {
			Gdx.app.error("EnemyActorDef", "FixtureDef null at setShapeType()");
		}


		fixtureChanged();
	}

	/**
	 * Adds another fixture definition
	 * @param fixtureDef new fixture def to add
	 */
	public void addFixtureDef(FixtureDef fixtureDef) {
		mFixtureDefs.add(fixtureDef);
		fixtureChanged();
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
	public void addCorners(java.util.List<Vector2> corners) {
		for (Vector2 corner : corners) {
			addCorner(corner);
		}
	}

	@Override
	public void addCorners(Vector2[] corners) {
		for (Vector2 corner : corners) {
			addCorner(corner);
		}
	}

	@Override
	public void addCorner(Vector2 corner) {
		addCorner(corner, mCorners.size());
		fixtureChanged();
	}

	@Override
	public void addCorner(Vector2 corner, int index) {
		mCorners.add(index, new Vector2(corner));
		fixtureChanged();

		// If this was the first corner, fix the bounding box so this
		// resource will be drawn
		if (mCorners.size() == 1) {
			mBoundingBox = Geometry.getBoundingBox(mCorners);
		}
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
		fixtureChanged();
		return mCorners.remove(index);
	}

	@Override
	public void moveCorner(int index, Vector2 newPos) {
		mCorners.get(index).set(newPos);
		fixtureChanged();
	}

	@Override
	public int getCornerCount() {
		return mCorners.size();
	}

	/**
	 * @return all the corners of the actor
	 */
	@Override
	public List<Vector2> getCorners() {
		return mCorners;
	}

	@Override
	public void clearCorners() {
		mCorners.clear();
		clearFixtures();
		clearVertices();
		fixtureChanged();
	}

	/**
	 * Sets the center offset for the fixtures
	 * @param centerOffset center offset position for fixtures
	 */
	public void setCenterOffset(Vector2 centerOffset) {
		setCenterOffset(centerOffset.x, centerOffset.y);
	}

	/**
	 * Sets the center offset for the fixtures
	 * @param x offset in x-coordinates
	 * @param y offset in y-coordinates
	 */
	public void setCenterOffset(float x, float y) {
		mCenterOffset.set(x, y);

		// Create new fixtures on the right place
		if (mType != ActorShapeTypes.IMAGE) {
			setShapeType(mType);
		}
	}

	/**
	 * Center the offset. This will set the offset to the middle of the fixture(s). It
	 * will NOT set the center to (0,0) (only if the actual center of the fixtures are
	 * there.
	 */
	public void resetCenterOffset() {
		Vector2 center = new Vector2();

		switch (mType) {
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

				center.scl(1f / mCorners.size());
			} else {
				center.set(0, 0);
			}
			break;

		case IMAGE:
			center.set(0, 0);
			break;
		}

		setCenterOffset(center);
		fixtureChanged();
	}

	/**
	 * Updates the fixture change time
	 */
	private void fixtureChanged() {
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
		setShapeType(mType);
	}

	/**
	 * Readjusts/fixes the fixtures for the custom shape would become too small. NOTE:
	 * When this exception is thrown all fixtures have been removed and some might have
	 * been added. Fix the faulty corner and call #fixCustomShapeFixtures() again to fix
	 * this.
	 * @throws PolygonComplexException if the method failed to make the polygon
	 *         non-complex
	 * @throws PolygonCornersTooCloseException if some corners are too close
	 * @throws PolygonAreaTooSmallException if some corners would create a triangle with
	 *         too small area
	 */
	public void fixCustomShapeFixtures() {
		// Destroy previous fixtures
		clearFixtures();
		clearVertices();

		// Create the new fixture
		// Polygon
		if (mCorners.size() >= 3) {
			fixCustomShapePolygonFixtures();
		}
		// Circle
		else if (mCorners.size() >= 1) {
			fixCustomShapeCircleFixtures();
		}

		calculateBounds();
		fixtureChanged();
	}

	/**
	 * Fix custom
	 * @throws PolygonComplexException if the method failed to make the polygon
	 *         non-complex
	 * @throws PolygonCornersTooCloseException if some corners are too close
	 */
	private void fixCustomShapePolygonFixtures() {
		List<Vector2> triangles = getTrianglesFromCorners(mCorners);

		// Temporary variables
		int cTriangles = triangles.size() / 3;
		Vector2[] triangleVertices = new Vector2[3];
		for (int i = 0; i < triangleVertices.length; ++i) {
			triangleVertices[i] = new Vector2();
		}

		// Create Fixtures
		for (int triangle = 0; triangle < cTriangles; ++triangle) {
			int offset = triangle * 3;
			for (int vertex = 0; vertex < triangleVertices.length; ++vertex) {
				triangleVertices[vertex].set(triangles.get(offset + vertex));
				triangleVertices[vertex].add(mCenterOffset);
			}

			// Validate triangle
			try {
				Geometry.validateTriangle(triangleVertices);
			} catch (PolygonException e) {
				Gdx.app.debug(Shape.class.getSimpleName(), "Skipping triangle - " + e.getMessage());
				// Skip this triangle if it's invalid
				continue;
			}

			// Create fixture
			FixtureDef fixtureDef = getDefaultFixtureDef();
			PolygonShape polygonShape = new PolygonShape();
			polygonShape.set(triangleVertices);
			fixtureDef.shape = polygonShape;
			addFixtureDef(fixtureDef);
		}


		mVertices = triangles;

		// Free vertices stuff
		if (!mShapeComplete) {
			clearVertices();
		} else {
			for (Vector2 vertex : mCorners) {
				mPolygon.add(new Vector2(vertex));
			}
		}
	}

	/**
	 * Create triangles from the polygon corners
	 * @param corners the corners to create polygon triangles of
	 * @return list with all triangles
	 */
	private List<Vector2> getTrianglesFromCorners(List<Vector2> corners) {
		// Get triangles from corners
		List<Vector2> triangles = null;
		if (corners.size() == 3) {
			triangles = createCopy(corners);
			Geometry.makePolygonCounterClockwise(triangles);
		} else {
			List<Vector2> tempVertices = null;

			if (mType == ActorShapeTypes.CUSTOM) {
				tempVertices = createCopy(corners);
				switch (Geometry.intersectionExists(tempVertices)) {
				case NONE:
					mShapeComplete = true;
					break;

				case INTERSECTS:
				case INTERSECTS_WITH_LOOP:
					mShapeComplete = false;
					handlePolygonComplexException(tempVertices, null);
					break;
				}
			}
			// Fix intersections
			else if (mType == ActorShapeTypes.IMAGE) {
				int intersectionId = -1;
				do {
					intersectionId = Geometry.getIntersection(corners);
					if (intersectionId != -1) {
						corners.remove(intersectionId);
					}
				} while (intersectionId != -1);

				tempVertices = createCopy(corners);
			} else {
				return null;
			}


			try {
				triangles = mEarClippingTriangulator.computeTriangles(tempVertices);
				// Always reverse, triangles are always clockwise, whereas box2d needs
				// counter clockwise...
				Collections.reverse(triangles);
			} catch (PolygonComplexException e) {
				handlePolygonComplexException(tempVertices, e);
			}
			tempVertices = null;
		}
		return triangles;
	}

	/**
	 * Fix custom shapes that are circles
	 */
	private void fixCustomShapeCircleFixtures() {
		CircleShape circle = new CircleShape();

		Vector2 offsetPosition = new Vector2();
		offsetPosition.sub(mCenterOffset).sub(mCorners.get(0));

		float radius = 0;

		// One corner, use standard size
		if (mCorners.size() == 1) {
			radius = Config.Actor.Terrain.DEFAULT_CIRCLE_RADIUS;
		}
		// Else two corners, determine radius of circle
		else {
			Vector2 lengthVector = new Vector2(mCorners.get(0)).sub(mCorners.get(1));
			radius = lengthVector.len();
		}
		circle.setRadius(radius);

		FixtureDef fixtureDef = getDefaultFixtureDef();
		fixtureDef.shape = circle;
		addFixtureDef(fixtureDef);

		// Create vertices for the circle
		List<Vector2> circleVertices = Geometry.createCircle(radius);
		for (Vector2 vertex : circleVertices) {
			vertex.add(offsetPosition);
		}
		mVertices = mEarClippingTriangulator.computeTriangles(circleVertices);
		Collections.reverse(mVertices);

		for (Vector2 vertex : circleVertices) {
			mPolygon.add(new Vector2(vertex));
		}
	}

	/**
	 * @return triangle vertices for the current shape. Grouped together in groups of
	 *         three to form a triangle.
	 */
	public List<Vector2> getTriangleVertices() {
		return mVertices;
	}

	/**
	 * Creates a circle from the visual variables
	 * @return circle shape for fixture
	 */
	private CircleShape createCircleShape() {
		clearVertices();

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(mCircleRadius);
		/** @todo use center for all shapes */
		// circleShape.setPosition(centerOffset);

		// Create vertices for the circle
		List<Vector2> circleVertices = Geometry.createCircle(mCircleRadius);
		mVertices = mEarClippingTriangulator.computeTriangles(circleVertices);
		Collections.reverse(circleVertices);

		for (Vector2 vertex : circleVertices) {
			mPolygon.add(new Vector2(vertex));
		}

		return circleShape;
	}

	/**
	 * Creates a rectangle shape from the visual variables
	 * @return rectangle shape for fixture
	 */
	private PolygonShape createRectangleShape() {
		PolygonShape rectangleShape = new PolygonShape();

		float halfWidth = mWidth * 0.5f;
		float halfHeight = mHeight * 0.5f;

		/** @todo use center for all shapes */
		// rectangleShape.setAsBox(halfWidth, halfHeight, centerOffset, 0);
		rectangleShape.setAsBox(halfWidth, halfHeight);

		clearVertices();

		// Create triangle vertices and polygon for the rectangle
		if (rectangleShape.getVertexCount() == 4) {
			// First triangle
			Vector2 vertex = new Vector2();
			rectangleShape.getVertex(0, vertex);
			mVertices.add(vertex);
			mPolygon.add(new Vector2(vertex));
			vertex = new Vector2();
			rectangleShape.getVertex(1, vertex);
			mVertices.add(vertex);
			mPolygon.add(new Vector2(vertex));
			vertex = new Vector2();
			rectangleShape.getVertex(2, vertex);
			mVertices.add(vertex);
			mPolygon.add(new Vector2(vertex));

			// Second triangle
			mVertices.add(vertex);
			vertex = new Vector2();
			rectangleShape.getVertex(3, vertex);
			mVertices.add(vertex);
			mPolygon.add(new Vector2(vertex));
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
	private PolygonShape createTriangleShape() {
		Vector2[] vertices = com.spiddekauga.utils.Collections.fillNew(new Vector2[3], Vector2.class);

		// It will look something like this:
		// | \
		// | >
		// | /

		// Lower left corner
		vertices[0].x = -mWidth * 0.5f;
		vertices[0].y = -mHeight * 0.5f;

		// Middle right corner
		vertices[1].x = mWidth * 0.5f;
		vertices[1].y = 0;

		// Upper left corner
		vertices[2].x = vertices[0].x;
		vertices[2].y = -vertices[0].y;


		// Set the center...
		Vector2 center = new Vector2();
		center.set(vertices[0]).add(vertices[1]).add(vertices[2]).scl(1f / 3f);

		// Offset all vertices with negative center
		for (Vector2 vertex : vertices) {
			vertex.sub(center);
			/** @todo use center for all shapes */
			// vertex.add(centerOffset);
		}

		PolygonShape polygonShape = new PolygonShape();
		polygonShape.set(vertices);
		center = null;

		// Set vertices and create border
		clearVertices();
		for (Vector2 vertex : vertices) {
			mVertices.add(vertex);
			mPolygon.add(new Vector2(vertex));
		}


		return polygonShape;
	}

	/**
	 * Gets the first fixture definition. Prints an error if there are more or less
	 * fixtures than 1
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
	 * @return polygon shape of the actor.
	 */
	public List<Vector2> getPolygonShape() {
		return mPolygon;
	}

	/**
	 * @return true if it has a valid polygon shape
	 */
	public boolean isPolygonShapeValid() {
		return mPolygon != null && !mPolygon.isEmpty();
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
		fixtureDef.friction = mFriction;
		fixtureDef.restitution = mElasticity;
		fixtureDef.density = mDensity;
		return fixtureDef;
	}

	/**
	 * Creates a copy of the list
	 * @param vertices all vertices to create a copy of
	 * @return new array list with a copy of the vertices
	 */
	private List<Vector2> createCopy(List<Vector2> vertices) {
		List<Vector2> copy = new ArrayList<>();

		for (Vector2 vertex : vertices) {
			copy.add(new Vector2(vertex));
		}

		return copy;
	}

	/**
	 * Handles PolygonComplexException for #fixCustomShapeFixtures()
	 * @param tempVertices
	 * @param exception if null it will throw a new exception, else it will re-throw the
	 *        exception.
	 */
	private void handlePolygonComplexException(List<Vector2> tempVertices, PolygonComplexException exception) {
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

	@Override
	public void createBodyCorners() {
		// Does nothing
	}

	@Override
	public void destroyBodyCorners() {
		// Does nothing
	}

	@Override
	public UUID getId() {
		return null;
	}

	@Override
	public void setId(UUID id) {
		// Does nothing
	}

	@Override
	public void removeBoundResource(IResource boundResource, List<Command> commands) {
		// Does nothing
	}

	@Override
	public void addChangeListener(IResourceChangeListener listener) {
		// Does nothing
	}

	@Override
	public void removeChangeListener(IResourceChangeListener listener) {
		// Does nothing
	}

	/**
	 * Set the density of the actor
	 * @param density
	 */
	public void setDensity(float density) {
		mDensity = density;
		updateFixtureDefs();
	}

	/**
	 * @return density of the actor
	 */
	public float getDensity() {
		return mDensity;
	}

	/**
	 * Sets the friction of the actor
	 * @param friction
	 */
	public void setFriction(float friction) {
		mFriction = friction;
		updateFixtureDefs();
	}

	/**
	 * @return friction of the actor
	 */
	public float getFriction() {
		return mFriction;
	}

	/**
	 * Sets the elasticity of the actor
	 * @param elasticity
	 */
	public void setElasticity(float elasticity) {
		mElasticity = elasticity;
		updateFixtureDefs();
	}

	/**
	 * @return elasticity of the actor
	 */
	public float getElasticity() {
		return mElasticity;
	}

	/**
	 * Updates the fixture information
	 */
	private void updateFixtureDefs() {
		for (FixtureDef fixtureDef : mFixtureDefs) {
			fixtureDef.density = mDensity;
			fixtureDef.friction = mFriction;
			fixtureDef.restitution = mElasticity;
		}
		fixtureChanged();
	}

	/**
	 * Sets the image name for the actor
	 * @param drawName
	 */
	public void setDrawName(DrawImages drawName) {
		mDrawName = drawName;

		mContourRaw = null;
	}

	/**
	 * @return image name for the actor, null if not used
	 */
	public DrawImages getDrawName() {
		return mDrawName;
	}

	private void disposeImage() {
		mContourRaw = null;
	}

	/**
	 * Set image scaling. Call {@link #updateImageShape(float, Float, float)} to update
	 * the image
	 * @param scale
	 */
	public void setImageScale(float scale) {
		mImageScale = scale;
	}

	/**
	 * @return image scaling
	 */
	public float getImageScale() {
		return mImageScale;
	}

	/**
	 * Update the image actor's shape
	 * @param distMin minimum distance between corners
	 * @param angleMin minimum angle between corners, set to null to not use
	 * @param worldScale amount of world scale
	 */
	public void updateImageShape(float distMin, Float angleMin, float worldScale) {
		if (mDrawName == null) {
			return;
		}

		clearCorners();

		// Calculate raw corners if we don't have yet
		if (mContourRaw == null) {
			if (mImageOffset == null) {
				mImageOffset = new Vector2();
			}
			TextureRegion region = mDrawName.getRegion();
			if (region != null) {
				mContourRaw = Graphics.getContour(region, 0.5f, 1.02f, mImageOffset);
			} else {
				return;
			}
		}
		List<Vector2> transformedContour = createCopy(mContourRaw);

		// Remove excessive corners
		Geometry.removeExcessivePoints(distMin * distMin, angleMin, transformedContour);
		setCenterOffset(0, 0);


		// Scale
		mImageScaleWorld = mImageScale * worldScale;
		// float scale = worldScale;
		for (Vector2 point : transformedContour) {
			point.scl(mImageScaleWorld);
		}

		addCorners(transformedContour);
		fixCustomShapeFixtures();

		mImage = null;
	}

	/**
	 * @return true if the actor has an image sprite
	 */
	public boolean hasImage() {
		return mDrawName != null;
	}

	/**
	 * @param pos offset the image with this position
	 * @return image to draw as the actor; null if no image has been set
	 */
	public Sprite getImage(Vector2 pos) {
		if (mDrawName == null) {
			return null;
		}

		if (mImage == null) {
			mImage = new Sprite(mDrawName.getRegion());
			mImage.setOrigin(0, 0);
			mImageOffsetWorld.set(mImageOffset).scl(mImageScaleWorld);
			mImage.setScale(mImageScaleWorld);
		}


		mImage.setPosition(pos.x - mImageOffsetWorld.x, pos.y - mImageOffsetWorld.y);

		return mImage;
	}

	/**
	 * Set the starting angle, used for calculating the bounding box
	 * @param angle in degrees
	 */
	void setStartAngle(float angle) {
		mStartAngle = angle;
		calculateBoundingBox();
	}


	@Tag(52) private Color mColor = new Color();
	@Tag(49) private ActorShapeTypes mType = null;
	@Tag(60) private float mCircleRadius;
	@Tag(61) private float mWidth;
	@Tag(62) private float mHeight;
	@Tag(51) private Vector2 mCenterOffset = new Vector2();
	@Tag(50) private ActorTypes mActorType = null;
	/** Corners of polygon, used for custom shapes */
	@Tag(63) private List<Vector2> mCorners = new ArrayList<>();
	@Tag(127) private Vector2 mImageOffset = new Vector2();
	@Tag(128) private float mImageScaleWorld = 1;
	@Tag(144) private float mStartAngle = 0;
	@Tag(157) private DrawImages mDrawName = null;
	@Tag(158) private int mClassRevision = 0;

	/** Temporary raw image contour points of the actor */
	private List<Vector2> mContourRaw = null;
	/** Image/Texture to draw as the actor */
	private Sprite mImage = null;
	/** Image scale in the world */
	private float mImageScale = 1;
	/** Scaled offset */
	private Vector2 mImageOffsetWorld = new Vector2();
	/**
	 * Array list of the polygon figure, this contains the vertices but not in triangles.
	 * Used for when creating a border of some kind
	 */
	private List<Vector2> mPolygon = new ArrayList<>();
	/**
	 * Triangle vertices. To easier render the shape. No optimization has been done to
	 * reduce the number of vertices.
	 */
	private List<Vector2> mVertices = new ArrayList<>();
	/** True if shape is drawable/complete */
	private boolean mShapeComplete = true;
	/** Defines the mass, shape, etc. */
	private List<FixtureDef> mFixtureDefs = new ArrayList<>();
	/** Radius of the actor, or rather circle bounding box */
	private float mBoundingRadius = 0;
	private BoundingBox mBoundingBox = new BoundingBox();
	/** Time when the fixture was changed last time */
	protected float mFixtureChangeTime = 0;
	/** Class structure revision */
	private static final int CLASS_REVISION = 2;

	// Fixture def values
	private float mDensity = 0;
	private float mElasticity = 0;
	private float mFriction = 0;

	/** Ear clipping triangulator for custom shapes */
	private static EarClippingTriangulator mEarClippingTriangulator = new EarClippingTriangulator();

	@Deprecated @Tag(126) private IImageNames unused1 = null;
}
