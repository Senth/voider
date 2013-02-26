package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;

/**
 * Abstract tool for handling touch events
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class TouchTool extends InputAdapter {
	/**
	 * Constructs a touch tool with a camera
	 * @param camera used for determining where in the world the pointer is
	 * @param world used for picking
	 */
	public TouchTool(Camera camera, World world) {
		mCamera = camera;
		mWorld = world;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {

		// Only do something for the first pointer
		if (pointer == 0) {
			if (mClickTimeLast + Config.Input.DOUBLE_CLICK_TIME > SceneSwitcher.getGameTime().getTotalTimeElapsed()) {
				mClickTimeLast = 0;
				mDoubleClick = true;
			} else {
				mDoubleClick = false;
				mClickTimeLast = SceneSwitcher.getGameTime().getTotalTimeElapsed();
			}

			Scene.screenToWorldCoord(mCamera, x, y, mTouchOrigin, true);
			mTouchCurrent.set(mTouchOrigin);

			down();

			return true;
		}


		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (pointer == 0) {
			Scene.screenToWorldCoord(mCamera, x, y, mTouchCurrent, true);

			dragged();

			return true;
		}


		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (pointer == 0) {
			Scene.screenToWorldCoord(mCamera, x, y, mTouchCurrent, true);

			up();
		}

		return false;
	}

	/**
	 * Activates the tool. This method does nothing by itself except when
	 * overridden. Usually this will make selected actors add extra corners and/or
	 * draw them differently
	 */
	public void activate() {
		// Does nothing
	}

	/**
	 * Deactivates the tool. This method does nothing by itself except when overridden.
	 * Usually this will make selected actors to be drawn regularly again
	 */
	public void deactivate() {
		// Does nothing
	}

	/**
	 * Clears the current tool to the initial state
	 */
	public void clear() {
		// Does nothing
	}

	/**
	 * Tests to pick a body from the current touch. The hit body is
	 * set to mHitBody. Uses the default pick size
	 */
	protected void testPick() {
		testPick(Editor.PICK_SIZE_DEFAULT);
	}

	/**
	 * Tests to pick a body from the current touch. The hit body will be
	 * set to mHitBody
	 * @param halfSize how far the touch shall test
	 */
	protected void testPick(float halfSize) {
		mHitBodies.clear();
		mWorld.QueryAABB(getCallback(), mTouchCurrent.x - halfSize, mTouchCurrent.y - halfSize, mTouchCurrent.x + halfSize, mTouchCurrent.y + halfSize);
		mHitBody = filterPick(mHitBodies);
	}

	/**
	 * Called on touchDown event
	 */
	protected abstract void down();

	/**
	 * Called on touchDragged event
	 */
	protected abstract void dragged();

	/**
	 * Called on touchUp event
	 */
	protected abstract void up();

	/**
	 * To work properly this callback shall set mHitBodies for
	 * all bodies that were hit
	 * @return the callback for testing picking
	 */
	protected abstract QueryCallback getCallback();

	/**
	 * Called to filter the bodies that were hit.
	 * @param hitBodies all the bodies that were hit in the pick
	 * @return hitBody the body that had a prioritized hit
	 */
	protected abstract Body filterPick(ArrayList<Body> hitBodies);

	/** If the player double clicked */
	protected boolean mDoubleClick = false;
	/** Last time the player clicked */
	protected float mClickTimeLast = 0;
	/** Current position of the touch */
	protected Vector2 mTouchCurrent = new Vector2();
	/** Original position of the touch */
	protected Vector2 mTouchOrigin = new Vector2();
	/** Current body that was hit */
	protected Body mHitBody = null;
	/** World used for picking */
	protected World mWorld;

	/** Camera of the tool, used to get world coordinates of the click */
	private Camera mCamera;

	// Temp variables
	/** All bodies that were hit during the pick */
	protected static ArrayList<Body> mHitBodies = new ArrayList<Body>();
}
