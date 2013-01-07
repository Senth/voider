package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
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
		/** @TODO render the terrain */
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
	 */
	public int addCorner(Vector2 corner) {
		mCorners.add(corner.cpy());
		readjustFixtures();

		if (mEditorActive) {
			createBodyCorner(corner);
		}

		return mCorners.size - 1;
	}

	/**
	 * Removes a corner position
	 * @param corner the corner to remove
	 */
	public void removeCorner(Vector2 corner) {
		Vector2 removeCorner = null;
		int i = 0;
		while (removeCorner == null && i < mCorners.size) {
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
		mCorners.removeIndex(i);

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
		if (index >= 0 && index < mCorners.size) {
			mCorners.removeIndex(index);

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
		for (int i = 0; i < mCorners.size; ++i) {
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
		if (mCorners.size <= 3 || index < 0 || index >= mCorners.size) {
			return false;
		}


		// Get the two lines
		// First vertex is between before
		Vector2 vertexBefore = null;

		// If index is 0, get the last corner instead
		if (index == 0) {
			vertexBefore = mCorners.get(mCorners.size - 1);
		} else {
			vertexBefore = mCorners.get(index - 1);
		}

		// Vertex of index
		Vector2 vertexIndex = mCorners.get(index);

		// Vertex after index
		Vector2 vertexAfter = null;

		// If index is the last, wrap and use first
		if (index == mCorners.size - 1) {
			vertexAfter = mCorners.get(0);
		} else {
			vertexAfter = mCorners.get(index + 1);
		}

		ChainShape shape = null;
		// Not a chainshape, somethings wrong, abort
		if (!(getDef().getFixtureDef().shape instanceof ChainShape)) {
			Gdx.app.error("StaticTerrainActor", "Shape is not a chainshape when checking intersection");
			return false;
		} else {
			shape = (ChainShape) getDef().getFixtureDef().shape;
		}


		Vector2 lineA = Pools.obtain(Vector2.class);
		Vector2 lineB = Pools.obtain(Vector2.class);

		boolean intersects = false;

		// Using shape because the chain is looped, i.e. first and last is the same vertex
		for (int i = 0; i < mCorners.size; ++i) {
			// Skip checking index before and the current index, these are the lines
			// we are checking with... If index is 0 we need to wrap it
			if (i == index || i == index - 1 || (index == 0 && i == mCorners.size - 1)) {
				continue;
			}


			/** @TODO can be optimized if necessary */
			shape.getVertex(i, lineA);
			shape.getVertex(i+1, lineB);


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

		Pools.free(lineA);
		Pools.free(lineB);

		return intersects;
	}

	/**
	 * Moves a corner, identifying the corner from the original position
	 * @param originalPos the original position of the corner
	 * @param newPos the new position of the corner
	 * @return index of the currently moving corner, -1 if none was found
	 */
	public int moveCorner(Vector2 originalPos, Vector2 newPos) {
		Vector2 corner = null;
		int i = 0;
		while (corner == null && i < mCorners.size) {
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

		corner.set(newPos);

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
	 */
	public void moveCorner(int index, Vector2 newPos) {
		mCorners.get(index).set(newPos);

		if (mEditorActive) {
			mCornerBodies.get(index).setTransform(newPos, 0f);
		}

		readjustFixtures();
	}

	/**
	 * Readjust fixtures, this makes all the fixtures convex
	 */
	private void readjustFixtures() {
		// Destroy previous fixture
		@SuppressWarnings("unchecked")
		ArrayList<Fixture> fixtures = (ArrayList<Fixture>) getBody().getFixtureList().clone();
		for (Fixture fixture : fixtures) {
			getBody().destroyFixture(fixture);
		}

		FixtureDef fixtureDef = getDef().getFixtureDef();
		if (fixtureDef.shape != null) {
			fixtureDef.shape.dispose();
			fixtureDef.shape = null;
		}


		// Create the new fixture
		// Chain
		if (mCorners.size >= 3) {
			ChainShape chain = new ChainShape();
			chain.createLoop(mCorners.toArray(Vector2.class));
			fixtureDef.shape = chain;
			getBody().createFixture(fixtureDef);
		}
		// Edge
		else if (mCorners.size == 2) {
			EdgeShape edge = new EdgeShape();
			edge.set(mCorners.get(0), mCorners.get(1));
			fixtureDef.shape = edge;
			getBody().createFixture(fixtureDef);
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
		body.setUserData(this);
		mCornerBodies.add(body);
	}

	/**
	 * Removes a corner body of the specified index
	 * @param index corner body to remove
	 */
	private void removeBodyCorner(int index) {
		Body removedBody = mCornerBodies.removeIndex(index);
		if (removedBody != null) {
			mWorld.destroyBody(removedBody);
		}
	}

	/** An array with all corner positions of the vector */
	private Array<Vector2> mCorners = new Array<Vector2>();
	/** All bodies for the corners, used for picking */
	private Array<Body> mCornerBodies = new Array<Body>();
}
