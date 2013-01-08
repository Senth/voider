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
		super.write(json);

		/** @TODO save points */
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		/** @TODO read points */
	}

	/**
	 * Add another corner position to the back of the array
	 * @param corner a new corner that will be placed at the back
	 * @return index of the new corner
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 */
	public int addCorner(Vector2 corner) throws PolygonComplexException {
		mCorners.add(corner.cpy());

		// Make sure no intersection exists
		if (intersectionExists(mCorners.size() - 1)) {
			mCorners.remove(mCorners.size() - 1);
			throw new PolygonComplexException();
		}

		readjustFixtures();

		if (mEditorActive) {
			createBodyCorner(corner);
		}

		return mCorners.size() - 1;
	}

	/**
	 * Removes a corner position
	 * @param corner the corner to remove
	 */
	public void removeCorner(Vector2 corner) {
		Vector2 removeCorner = null;
		int i = 0;
		while (removeCorner == null && i < mCorners.size()) {
			if (mCorners.get(i).equals(corner)) {
				removeCorner = mCorners.get(i);
			} else {
				++i;
			}
		}

		if (removeCorner == null) {
			Gdx.app.error("Terrain", "Could not find the corner to remove");
			return;
		}
		mCorners.remove(i);

		if (mEditorActive) {
			removeBodyCorner(i);
		}

		readjustFixtures();
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

			readjustFixtures();
		}
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
	 * Moves a corner, identifying the corner from the original position
	 * @param originalPos the original position of the corner
	 * @param newPos the new position of the corner
	 * @return index of the currently moving corner, -1 if none was found
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 */
	public int moveCorner(Vector2 originalPos, Vector2 newPos) throws PolygonComplexException {
		Vector2 corner = null;
		int i = 0;
		while (corner == null && i < mCorners.size()) {
			if (mCorners.get(i).equals(originalPos)) {
				corner = mCorners.get(i);
			} else {
				++i;
			}
		}

		if (corner == null) {
			Gdx.app.error("Terrain", "Could not find the corner to move");
			return -1;
		}

		Vector2 oldPos = Pools.obtain(Vector2.class);
		oldPos.set(corner);
		corner.set(newPos);

		if (intersectionExists(i)) {
			corner.set(oldPos);
			Pools.free(oldPos);
			throw new PolygonComplexException();
		} else {
			Pools.free(oldPos);
		}


		if (mEditorActive) {
			mCornerBodies.get(i).setTransform(newPos, 0f);
		}

		readjustFixtures();

		return i;
	}

	/**
	 * Moves a corner, identifying the corner from index
	 * @param index index of the corner to move
	 * @param newPos new position of the corner
	 * @throws PolygonComplexException thrown when the adding corner would make the
	 * polygon an complex polygon, i.e. intersect itself.
	 */
	public void moveCorner(int index, Vector2 newPos) throws PolygonComplexException {
		Vector2 oldPos = Pools.obtain(Vector2.class);
		oldPos.set(mCorners.get(index));
		mCorners.get(index).set(newPos);

		if (intersectionExists(index)) {
			mCorners.get(index).set(oldPos);
			Pools.free(oldPos);
			throw new PolygonComplexException();
		} else {
			Pools.free(oldPos);
		}

		if (mEditorActive) {
			mCornerBodies.get(index).setTransform(newPos, 0f);
		}

		readjustFixtures();
	}

	/**
	 * Exception class for when trying to create a new, or move an existing corner
	 * and this makes the polygon complex, i.e. it intersects with itself.
	 */
	public class PolygonComplexException extends Exception {
		/** for serialization */
		private static final long serialVersionUID = -2564535357356811708L;}

	/**
	 * Readjust fixtures, this makes all the fixtures convex
	 */
	private void readjustFixtures() {
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
			triangleVertices[0] = Pools.obtain(Vector2.class);
			triangleVertices[1] = Pools.obtain(Vector2.class);
			triangleVertices[2] = Pools.obtain(Vector2.class);


			// Add the fixtures
			for (int triangle = 0; triangle < cTriangles; ++triangle) {
				for (int vertex = 0; vertex < triangleVertices.length; ++vertex) {
					int offset = triangle * 3;
					triangleVertices[vertex] = triangles.get(offset + vertex);
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
	 * Removes a corner body of the specified index
	 * @param index corner body to remove
	 */
	private void removeBodyCorner(int index) {
		Body removedBody = mCornerBodies.remove(index);
		if (removedBody != null) {
			mWorld.destroyBody(removedBody);
		}
	}

	/** An array with all corner positions of the vector */
	private ArrayList<Vector2> mCorners = new ArrayList<Vector2>();
	/** All bodies for the corners, used for picking */
	private ArrayList<Body> mCornerBodies = new ArrayList<Body>();
	/** Ear clipping triangulator */
	private static EarClippingTriangulator mEarClippingTriangulator = new EarClippingTriangulator();
}
