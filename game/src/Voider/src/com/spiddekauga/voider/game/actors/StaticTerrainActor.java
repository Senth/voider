package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.TriggerAction;
import com.spiddekauga.voider.utils.Geometry;

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

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.game.ITriggerListener#onTriggered(java.lang.String)
	 */
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
		json.writeValue("mCorners", mWorldCorners);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		OrderedMap<String, Object> actorMap = json.readValue("Actor", OrderedMap.class, jsonData);
		super.read(json, actorMap);


		mWorldCorners = json.readValue("mCorners", ArrayList.class, jsonData);

		// Create local corners
		for (Vector2 worldCorner : mWorldCorners) {
			mLocalCorners.add(toLocalPos(worldCorner));
		}

		// Corner bodies are created via createBody which is called in Actor superclass.
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
		mWorldCorners.add(corner.cpy());

		// Make sure no intersection exists
		if (intersectionExists(mWorldCorners.size() - 1)) {
			mWorldCorners.remove(mWorldCorners.size() - 1);
			throw new PolygonComplexException();
		}

		mLocalCorners.add(toLocalPos(corner));

		try {
			readjustFixtures();
		} catch (PolygonCornerTooCloseException e) {
			mWorldCorners.remove(mWorldCorners.size() - 1);
			mLocalCorners.remove(mLocalCorners.size() - 1);
			throw e;
		}

		if (mEditorActive) {
			createBodyCorner(corner);
		}

		mLastAddedCornerIndex = mWorldCorners.size()-1;
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
		mWorldCorners.add(index, corner.cpy());

		// Make sure no intersection exists
		if (intersectionExists(index)) {
			mWorldCorners.remove(index);
			throw new PolygonComplexException();
		}

		mLocalCorners.add(index, toLocalPos(corner));

		try {
			readjustFixtures();
		} catch (PolygonCornerTooCloseException e) {
			mWorldCorners.remove(index);
			mLocalCorners.remove(index);
			throw e;
		}

		if (mEditorActive) {
			createBodyCorner(corner, index);
		}

		mLastAddedCornerIndex = index;
	}

	/**
	 * @return index of last added corner
	 */
	public int getLastAddedCornerIndex() {
		return mLastAddedCornerIndex;
	}

	/**
	 * Removes a corner with the specific id
	 * @param index the corner to remove
	 */
	public void removeCorner(int index) {
		if (index >= 0 && index < mWorldCorners.size()) {
			mWorldCorners.remove(index);

			// Only free local corners as world corners still could be used outside this class
			Vector2 removedCorner = mLocalCorners.remove(index);
			if (removedCorner != null) {
				Pools.free(removedCorner);
			}

			if (mEditorActive) {
				removeBodyCorner(index);
			}

			try {
				readjustFixtures();
			} catch (PolygonCornerTooCloseException e) {
				Gdx.app.error("StaticTerrainActor", "Failed to remove corner, exception, should never happen");
			}
		}
	}

	/**
	 * @param index corner's index which position we want to get
	 * @return position of the specified corner
	 */
	public Vector2 getCorner(int index) {
		return mWorldCorners.get(index);
	}

	/**
	 * @param position the position of a corner
	 * @return corner index of the specified position, -1 if none was found
	 */
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mWorldCorners.size(); ++i) {
			if (mWorldCorners.get(i).equals(position)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * @return number of corners in this terrain
	 */
	public int getCornerCount() {
		return mWorldCorners.size();
	}

	/**
	 * Checks if a line, that starts/énds from the specified index, intersects
	 * with any other line from the polygon.
	 * @param index the corner to check the lines from
	 * @return true if there is an intersection
	 */
	public boolean intersectionExists(int index) {
		if (mWorldCorners.size() < 3 || index < 0 || index >= mWorldCorners.size()) {
			return false;
		}


		// Get the two lines
		// First vertex is between before
		Vector2 vertexBefore = null;

		// If index is 0, get the last corner instead
		if (index == 0) {
			vertexBefore = mWorldCorners.get(mWorldCorners.size() - 1);
		} else {
			vertexBefore = mWorldCorners.get(index - 1);
		}

		// Vertex of index
		Vector2 vertexIndex = mWorldCorners.get(index);

		// Vertex after index
		Vector2 vertexAfter = null;

		// If index is the last, wrap and use first
		if (index == mWorldCorners.size() - 1) {
			vertexAfter = mWorldCorners.get(0);
		} else {
			vertexAfter = mWorldCorners.get(index + 1);
		}


		boolean intersects = false;
		// Using shape because the chain is looped, i.e. first and last is the same vertex
		for (int i = 0; i < mWorldCorners.size(); ++i) {
			// Skip checking index before and the current index, these are the lines
			// we are checking with... If index is 0 we need to wrap it
			if (i == index || i == index - 1 || (index == 0 && i == mWorldCorners.size() - 1)) {
				continue;
			}


			/** @TODO can be optimized if necessary. Swap instead of getting
			 * new vertexes all the time. */
			Vector2 lineA = mWorldCorners.get(i);
			Vector2 lineB = mWorldCorners.get(Geometry.computeNextIndex(mWorldCorners, i));


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
	 * Moves a corner, identifying the corner from index
	 * @param index index of the corner to move
	 * @param newPos new position of the corner
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 * @throws PolygonCornerTooCloseException thrown when a corner is too close to
	 * another corner inside the polygon.
	 */
	public void moveCorner(int index, Vector2 newPos) throws PolygonComplexException, PolygonCornerTooCloseException {
		Vector2 oldWorldPos = Pools.obtain(Vector2.class);
		oldWorldPos.set(mWorldCorners.get(index));
		mWorldCorners.get(index).set(newPos);

		// Set local
		Vector2 localPos = toLocalPos(newPos);
		Vector2 oldLocalPos = Pools.obtain(Vector2.class);
		oldLocalPos.set(mLocalCorners.get(index));
		mLocalCorners.get(index).set(localPos);
		Pools.free(localPos);

		if (intersectionExists(index)) {
			mWorldCorners.get(index).set(oldWorldPos);
			mLocalCorners.get(index).set(oldWorldPos);
			Pools.free(oldLocalPos);
			Pools.free(oldWorldPos);
			throw new PolygonComplexException();
		}

		try {
			readjustFixtures();
		} catch (PolygonCornerTooCloseException e) {
			mWorldCorners.get(index).set(oldWorldPos);
			mLocalCorners.get(index).set(oldLocalPos);
			Pools.free(oldLocalPos);
			Pools.free(oldWorldPos);
			throw e;
		}

		Pools.free(oldLocalPos);
		Pools.free(oldWorldPos);

		if (mEditorActive) {
			mCornerBodies.get(index).setTransform(newPos, 0f);
		}
	}

	@Override
	public void createBody() {
		super.createBody();

		if (mEditorActive) {
			createBodyCorners();
		}
	}

	@Override
	public void destroyBody() {
		super.destroyBody();

		if (mEditorActive) {
			destroyBodyCorners();
		}
	}

	/**
	 * Creates all body corners, will only have an effect if
	 * no body corners have been created yet
	 */
	public void createBodyCorners() {
		if (mCornerBodies.size() == 0 && mEditorActive) {
			for (Vector2 corner : mWorldCorners) {
				createBodyCorner(corner);
			}
		}
	}

	/**
	 * Destroys all body corners
	 */
	public void destroyBodyCorners() {
		for (Body body : mCornerBodies) {
			body.getWorld().destroyBody(body);
		}
		mCornerBodies.clear();
	}

	@Override
	public void setPosition(Vector2 position) {
		// Get diff movement for moving all corners
		Vector2 diffMovement = Pools.obtain(Vector2.class);
		diffMovement.set(position).sub(getPosition());

		super.setPosition(position);

		// Move all corners
		for (int i = 0; i < mWorldCorners.size(); ++i) {
			mWorldCorners.get(i).add(diffMovement);

			// Move body corners
			if (mEditorActive) {
				mCornerBodies.get(i).setTransform(mWorldCorners.get(i), 0.0f);
			}
		}
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

	@Override
	protected boolean savesDef() {
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

	/**
	 * @param worldPos the position to convert to local
	 * @return the local position of a position. This shall be freed with
	 * Pools.free() later.
	 */
	private Vector2 toLocalPos(Vector2 worldPos) {
		return Pools.obtain(Vector2.class).set(worldPos).sub(getPosition());
	}

	/**
	 * Readjust fixtures, this makes all the fixtures convex
	 * @throws PolygonCornerTooCloseException thrown when a resulting triangle polygon
	 * would become too small. NOTE: When this exception is thrown all fixtures
	 * have been removed and some might have been added. Fix the faulty corner
	 * and call readjustFixtures() again to fix this.
	 */
	private void readjustFixtures() throws PolygonCornerTooCloseException {
		// Destroy previous fixture
		clearFixtures();


		// Create the new fixture
		// Polygon
		if (mLocalCorners.size() >= 3) {
			List<Vector2> triangles = null;
			if (mLocalCorners.size() == 3) {
				triangles = new ArrayList<Vector2>(mLocalCorners);
				Geometry.makePolygonCounterClockwise(triangles);
			} else {
				triangles = mEarClippingTriangulator.computeTriangles(mLocalCorners);
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


				PolygonShape polygonShape = new PolygonShape();
				polygonShape.set(triangleVertices);
				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = polygonShape;
				fixtureDef.density = 0f;
				addFixture(fixtureDef);
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
		else if (mLocalCorners.size() >= 1) {
			CircleShape circle = new CircleShape();
			circle.setPosition(mLocalCorners.get(0));

			// One corner, use standard size
			if (mLocalCorners.size() == 1) {
				circle.setRadius(Config.Actor.Terrain.DEFAULT_CIRCLE_RADIUS);
			}
			// Else two corners, determine radius of circle
			else {
				Vector2 lengthVector = Pools.obtain(Vector2.class);
				lengthVector.set(mLocalCorners.get(0)).sub(mLocalCorners.get(1));
				circle.setRadius(lengthVector.len());
				Pools.free(lengthVector);
			}
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = circle;
			fixtureDef.density = 0f;
			addFixture(fixtureDef);
		}
	}

	/**
	 * Creates a body for the specific point
	 * @param corner the corner to create a body for
	 */
	private void createBodyCorner(Vector2 corner) {
		createBodyCorner(corner, mCornerBodies.size());
	}

	/**
	 * Creates a body for the specific point at the specific index
	 * @param corner the corner to create a body for
	 * @param index where in the list the body corner should be stored.
	 */
	private void createBodyCorner(Vector2 corner, int index) {
		Body body = mWorld.createBody(new BodyDef());
		body.createFixture(Config.Editor.getPickingShape(), 0f);
		body.setTransform(corner, 0f);
		HitWrapper hitWrapper = new HitWrapper(this, true);
		body.setUserData(hitWrapper);
		mCornerBodies.add(index, body);
	}

	/**
	 * Removes a corner body of the specified index
	 * @param index corner body to remove
	 */
	private void removeBodyCorner(int index) {
		Body removedBody = mCornerBodies.remove(index);
		if (removedBody != null) {
			removedBody.getWorld().destroyBody(removedBody);
		}
	}

	/** Index of last added corner */
	private int mLastAddedCornerIndex = -1;
	/** An array with all world corner positions */
	private ArrayList<Vector2> mWorldCorners = new ArrayList<Vector2>();
	/** Array with all local corner positions */
	private ArrayList<Vector2> mLocalCorners = new ArrayList<Vector2>();
	/** All bodies for the corners, used for picking */
	private ArrayList<Body> mCornerBodies = new ArrayList<Body>();
	/** Ear clipping triangulator */
	private static EarClippingTriangulator mEarClippingTriangulator = new EarClippingTriangulator();
}
