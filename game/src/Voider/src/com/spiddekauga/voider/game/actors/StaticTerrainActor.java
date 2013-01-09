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
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.game.Actor;
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
	public void onTriggered(String action) {
		// Does nothing
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.game.Actor#update(float)
	 */
	@Override
	public void update(float deltaTime) {
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


		json.writeValue("mCorners", mCorners);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		OrderedMap<String, Object> actorMap = json.readValue("Actor", OrderedMap.class, jsonData);
		super.read(json, actorMap);


		mCorners = json.readValue("mCorners", ArrayList.class, jsonData);

		// Create corner bodies if editor...
		if (mEditorActive) {
			createBodyCorners();
		}
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
		mCorners.add(corner.cpy());

		// Make sure no intersection exists
		if (intersectionExists(mCorners.size() - 1)) {
			mCorners.remove(mCorners.size() - 1);
			throw new PolygonComplexException();
		}

		try {
			readjustFixtures();
		} catch (PolygonCornerTooCloseException e) {
			mCorners.remove(mCorners.size() - 1);
			throw e;
		}

		if (mEditorActive) {
			createBodyCorner(corner);
		}

		mLastAddedCornerIndex = mCorners.size()-1;
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
		if (index >= 0 && index < mCorners.size()) {
			mCorners.remove(index);

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
		return mCorners.get(index);
	}

	/**
	 * @param position the position of a corner
	 * @return corner index of the specified position, -1 if none was found
	 */
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mCorners.size(); ++i) {
			if (mCorners.get(i).equals(position)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Checks if a line, that starts/énds from the specified index, intersects
	 * with any other line from the polygon.
	 * @param index the corner to check the lines from
	 * @return true if there is an intersection
	 */
	public boolean intersectionExists(int index) {
		if (mCorners.size() < 3 || index < 0 || index >= mCorners.size()) {
			return false;
		}


		// Get the two lines
		// First vertex is between before
		Vector2 vertexBefore = null;

		// If index is 0, get the last corner instead
		if (index == 0) {
			vertexBefore = mCorners.get(mCorners.size() - 1);
		} else {
			vertexBefore = mCorners.get(index - 1);
		}

		// Vertex of index
		Vector2 vertexIndex = mCorners.get(index);

		// Vertex after index
		Vector2 vertexAfter = null;

		// If index is the last, wrap and use first
		if (index == mCorners.size() - 1) {
			vertexAfter = mCorners.get(0);
		} else {
			vertexAfter = mCorners.get(index + 1);
		}


		boolean intersects = false;
		// Using shape because the chain is looped, i.e. first and last is the same vertex
		for (int i = 0; i < mCorners.size(); ++i) {
			// Skip checking index before and the current index, these are the lines
			// we are checking with... If index is 0 we need to wrap it
			if (i == index || i == index - 1 || (index == 0 && i == mCorners.size() - 1)) {
				continue;
			}


			/** @TODO can be optimized if necessary. Swap instead of getting
			 * new vertexes all the time. */
			Vector2 lineA = mCorners.get(i);
			Vector2 lineB = mCorners.get(Geometry.computeNextIndex(mCorners, i));


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
		Vector2 oldPos = Pools.obtain(Vector2.class);
		oldPos.set(mCorners.get(index));
		mCorners.get(index).set(newPos);

		if (intersectionExists(index)) {
			mCorners.get(index).set(oldPos);
			Pools.free(oldPos);
			throw new PolygonComplexException();
		}

		try {
			readjustFixtures();
		} catch (PolygonCornerTooCloseException e) {
			mCorners.get(index).set(oldPos);
			Pools.free(oldPos);
			throw e;
		}

		Pools.free(oldPos);

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
			for (Body body : mCornerBodies) {
				body.getWorld().destroyBody(body);
			}
			mCornerBodies.clear();
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
		if (mCorners.size() >= 3) {
			List<Vector2> triangles = null;
			if (mCorners.size() == 3) {
				triangles = new ArrayList<Vector2>(mCorners);
				Geometry.makePolygonCounterClockwise(triangles);
			} else {
				triangles = mEarClippingTriangulator.computeTriangles(mCorners);
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
		// Edge
		else if (mCorners.size() == 2) {
			EdgeShape edge = new EdgeShape();
			edge.set(mCorners.get(0), mCorners.get(1));
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = edge;
			fixtureDef.density = 0f;
			addFixture(fixtureDef);
		}
	}

	/**
	 * Creates a body for the specific point
	 * @param corner the corner to create a body for
	 */
	private void createBodyCorner(Vector2 corner) {
		Body body = mWorld.createBody(new BodyDef());
		body.createFixture(Config.Editor.getPickingShape(), 0f);
		body.setTransform(corner, 0f);
		HitWrapper hitWrapper = new HitWrapper(this, true);
		body.setUserData(hitWrapper);
		mCornerBodies.add(body);
	}

	/**
	 * Creates all body corners, will only have an effect if
	 * no body corners have been created yet
	 */
	private void createBodyCorners() {
		if (mCornerBodies.size() == 0 && mEditorActive) {
			for (Vector2 corner : mCorners) {
				createBodyCorner(corner);
			}
		}
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
	/** An array with all corner positions of the vector */
	private ArrayList<Vector2> mCorners = new ArrayList<Vector2>();
	/** All bodies for the corners, used for picking */
	private ArrayList<Body> mCornerBodies = new ArrayList<Body>();
	/** Ear clipping triangulator */
	private static EarClippingTriangulator mEarClippingTriangulator = new EarClippingTriangulator();
}
