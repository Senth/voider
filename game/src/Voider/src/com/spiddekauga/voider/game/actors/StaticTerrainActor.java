package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Actor;

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
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);
	}

	/**
	 * Add another corner position to the back of the array
	 * @param corner a new corner that will be placed at the back
	 */
	public void addCorner(Vector2 corner) {
		mCorners.add(corner);
		readjustFixtures();

		if (mEditorActive) {
			createBodyCorner(corner);
		}
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
			mCornerBodies.removeIndex(i);
		}
	}

	/**
	 * Moves a corner
	 * @param originalPos the original position of the corner
	 * @param newPos the new position of the corner
	 */
	public void moveCorner(Vector2 originalPos, Vector2 newPos) {
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
			return;
		}

		corner.set(newPos);

		if (mEditorActive) {
			mCornerBodies.get(i).setTransform(newPos, 0f);
		}
	}

	/**
	 * Readjust fixtures, this makes all the fixtures convex
	 */
	private void readjustFixtures() {
		/** @TODO readjustFixtures() */
	}

	/**
	 * Creates a body for the specific point
	 * @param corner the corner to create a body for
	 */
	private void createBodyCorner(Vector2 corner) {
		Body body = mWorld.createBody(new BodyDef());
		body.createFixture(Config.Editor.PICKING_CIRCLE_SHAPE, 0f);
		body.setTransform(corner, 0f);
		mCornerBodies.add(body);
	}

	/** An array with all corner positions of the vector */
	private Array<Vector2> mCorners = new Array<Vector2>();
	/** All bodies for the corners, used for picking */
	private Array<Body> mCornerBodies = new Array<Body>();
}
