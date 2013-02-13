package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.Config;

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
	 * Tests to pick an actor from the current touch. The hit body is
	 * set to mHitBody
	 */
	protected void testPick() {
		mHitBodies.clear();
		mHitBody = null;
		float testSize = 0.0001f;
		mWorld.QueryAABB(getCallback(), mTouchCurrent.x - testSize, mTouchCurrent.y - testSize, mTouchCurrent.x + testSize, mTouchCurrent.y + testSize);
		filterPick(mHitBodies);
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

	/** Camera of the tool, used to get world coordinates of the click */
	private Camera mCamera;
	/** World used for picking */
	private World mWorld;

	// Temp variables
	/** All bodies that were hit during the pick */
	protected static ArrayList<Body> mHitBodies = new ArrayList<Body>();
}
