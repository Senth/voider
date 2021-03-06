package com.spiddekauga.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;

import java.util.LinkedList;

/**
 * A virtual scroller, this scroller both scrolls and emulates scrolls when the user "flings".
 */
public class Scroller {
private final static Pool<PointerTime> mPointerPool = com.badlogic.gdx.utils.Pools.get(PointerTime.class);
private final float mStartSpeedMin;
private final float mStartSpeedMax;
private final float mStopSpeed;
private final float mFriction;
private final ScrollAxis mScrollAxis;
private final float MEASURE_TIME = 0.15f;
private Vector2 mScrollOrigin = new Vector2();
private Vector2 mScrollCurrent = new Vector2();
private boolean mScrollByHand = false;
private boolean mScrollByEmulate = false;
private Vector2 mDirection = new Vector2();
private float mSpeed = 0.0f;
private LinkedList<PointerTime> mRecentPointerLocation = new LinkedList<PointerTime>();


/**
 * Creates a scroller with various parameters
 * @param startSpeedMin minimum speed when the emulation starts
 * @param startSpeedMax the maximum speed the emulated scroller is allowed to have
 * @param stopSpeed under this speed the scroller is stopped
 * @param friction how fast/slow the scroller shall stop, this is linear friction
 * @param scrollAxis the axis(es) the scroller shall scroll on
 */
public Scroller(float startSpeedMin, float startSpeedMax, float stopSpeed, float friction, ScrollAxis scrollAxis) {
	mStartSpeedMin = startSpeedMin;
	mStartSpeedMax = startSpeedMax;
	mStopSpeed = stopSpeed;
	mFriction = friction;
	mScrollAxis = scrollAxis;
}

/**
 * Call this when the user first presses the screen/button.
 * @param x the X-coordinate hit
 * @param y the Y-coordinate hit
 */
public void touchDown(int x, int y) {
	mScrollOrigin.set(x, y);
	mScrollCurrent.set(mScrollOrigin);
	mScrollByHand = true;
	mScrollByEmulate = false;

	savePointerLocation();
}

/**
 * Adds the current pointer location to the front of the list.
 */
private void savePointerLocation() {
	PointerTime pointerTime = mPointerPool.obtain();
	pointerTime.set(mScrollCurrent, SceneSwitcher.getGameTime().getTotalTimeElapsed());
	mRecentPointerLocation.addFirst(pointerTime);
}

/**
 * Call this when the user drags the cursor/finger over the screen
 * @param x the current X-coordinate
 * @param y the current Y-coordinate
 * @see #getCurrentScroll() for getting the current scroll coordinate
 */
public void touchDragged(int x, int y) {
	switch (mScrollAxis) {
	case X:
		mScrollCurrent.x = x;
		break;

	case Y:
		mScrollCurrent.y = y;
		break;

	case ALL:
		mScrollCurrent.set(x, y);
		break;
	}

	savePointerLocation();
}

/**
 * Call this when the user lifts the finger/button. Now the scroller tests whether to initiate the
 * emulation, i.e. if the speed isn't below the minimum start speed.
 * @param x the current X-coordinate
 * @param y the current Y-coordinate
 * @see #getCurrentScroll() for getting the current scroll coordinate. Both when emulating and not.
 */
public void touchUp(int x, int y) {
	mScrollByHand = false;

	switch (mScrollAxis) {
	case X:
		mScrollCurrent.x = x;
		break;

	case Y:
		mScrollCurrent.y = y;
		break;

	case ALL:
		mScrollCurrent.set(x, y);
		break;
	}

	savePointerLocation();
	removeOldPointerLocations();


	// Calculate if we shall emulate scrolling...
	float distance = 0.0f;
	float time = SceneSwitcher.getGameTime().getTotalTimeElapsed() - mRecentPointerLocation.getLast().time;
	switch (mScrollAxis) {
	case X:
		distance = mScrollCurrent.x - mRecentPointerLocation.getLast().position.x;
		break;

	case Y:
		distance = mScrollCurrent.y - mRecentPointerLocation.getLast().position.x;
		break;

	case ALL: {
		Vector2 velocity = new Vector2(mScrollCurrent).sub(mRecentPointerLocation.getLast().position);
		distance = velocity.len();
		break;
	}
	}

	// Division by zero check
	if (time == 0.0f) {
		return;
	}

	mSpeed = distance / time;
	if (mSpeed < 0) {
		mSpeed = -mSpeed;
	}

	// Above minimum start speed
	if (mSpeed >= mStartSpeedMin) {
		// Clamp if above max speed
		if (mSpeed >= mStartSpeedMax) {
			mSpeed = mStartSpeedMax;
		}

		// Create the direction vector (of normal length)
		mDirection.set(mScrollCurrent).sub(mRecentPointerLocation.getLast().position);
		mDirection.nor();

		mScrollByEmulate = true;
	}
}

/**
 * Removes the last pointer locations that were longer than #MEASURE_TIME seconds ago
 */
private void removeOldPointerLocations() {
	float currentTime = SceneSwitcher.getGameTime().getTotalTimeElapsed();
	while (!mRecentPointerLocation.isEmpty() && currentTime - mRecentPointerLocation.getLast().time > MEASURE_TIME) {
		mPointerPool.free(mRecentPointerLocation.removeLast());
	}
}

/**
 * @return the current scrolling coordinates. Depending what scroll axis(es) the scroller has been
 * set to it will only "change" those coordinates which are affected. E.g. if the scroller only
 * shall scroll on the X-axis x will be either the last touch x-coordinate or emulated x-coordinate,
 * y will always be the same y as it had in touchDown.
 */
public Vector2 getCurrentScroll() {
	return mScrollCurrent;
}

/**
 * @return start scroll coordinates
 */
public Vector2 getOriginScroll() {
	return mScrollOrigin;
}

/**
 * @return true if we're either scrolling by hand or emulating a scroll
 */
public boolean isScrolling() {
	return mScrollByHand || mScrollByEmulate;
}

/**
 * @return true if we're scrolling by hand
 */
public boolean isScrollingByHand() {
	return mScrollByHand;
}

/**
 * @return true if we're emulating scroll
 */
public boolean isScrollingEmulated() {
	return mScrollByEmulate;
}

/**
 * Updates the scrolling, does nothing unless we're emulating a scroll
 * @param deltaTime time elapsed since last frame
 */
public void update(float deltaTime) {
	if (mScrollByEmulate) {
		// Decrease speed with friction
		mSpeed -= mFriction * deltaTime;

		if (mSpeed > mStopSpeed) {
			Vector2 velocity = new Vector2(mDirection);
			velocity.scl(mSpeed * deltaTime);

			mScrollCurrent.add(velocity);
		}
		// Too low speed to continue
		else {
			mScrollByEmulate = false;
		}
	}
}

/**
 * Stops the scroller if it is active
 */
public void stop() {
	mScrollByEmulate = false;
}

/**
 * Enumeration for which axis to use the scroller for
 */
public enum ScrollAxis {
	/** Scrolls only in x-axis */
	X,
	/** Scrolls only in y-axis */
	Y,
	/** Scrolls in all axis */
	ALL
}

/**
 * Wrapper for pointer location time
 */
public static class PointerTime {
	/** Position of the pointer */
	Vector2 position = new Vector2();
	/** When the pointer was at the position */
	float time = 0.0f;

	/**
	 * Default constructor
	 */
	public PointerTime() {
		// Does nothing
	}

	/**
	 * Sets position and time of the pointer
	 * @param position the pointer position
	 * @param time when the pointer was at this position
	 */
	void set(Vector2 position, float time) {
		this.position.set(position);
		this.time = time;
	}

	/**
	 * Sets position and time of the pointer
	 * @param x the x-coordinate of the pointer
	 * @param y the y-coordinate of the pointer
	 * @param time when the pointer was at this position
	 */
	void set(float x, float y, float time) {
		position.set(x, y);
		this.time = time;
	}
}
}
