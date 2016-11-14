package com.spiddekauga.voider.utils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Measures the total game time of the game. If a frame is longer than 0.1s it will clamp the delta
 * time to 0.1s
 */
public class GameTime {
/** Total global time elapsed since start of game */
private static float mTotalGlobalTimeElapsed = 0;
/**
 * Maximum time between frames, useful when debugging or lagging so the game doesn't bug out.
 */
private static float FRAME_LENGTH_MAX = 0.1f;
/** Total time elapsed since start of game, in seconds */
@Tag(105)
private float mTotalTimeElapsed = 0;
/** Current delta time, in seconds */
@Tag(106)
private float mDeltaTime = 0;

/**
 * Updates the global game time, call once per frame
 * @param deltaTime elapsed time since last frame
 */
public synchronized static void updateGlobal(float deltaTime) {
	mTotalGlobalTimeElapsed += deltaTime;
}

// Static methods

/**
 * @return elapsed global time since the game/program started, in seconds.
 */
public synchronized static float getTotalGlobalTimeElapsed() {
	return mTotalGlobalTimeElapsed;
}

/**
 * Updates the game time, call once per frame
 * @param deltaTime elapsed time since last frame
 */
public void update(float deltaTime) {
	if (deltaTime < FRAME_LENGTH_MAX) {
		mDeltaTime = deltaTime;
	} else {
		mDeltaTime = FRAME_LENGTH_MAX;
	}

	mTotalTimeElapsed += mDeltaTime;
}

/**
 * @return current frame delta time, in seconds
 */
public float getDeltaTime() {
	return mDeltaTime;
}

/**
 * @return elapsed time since the scene, in seconds.
 */
public float getTotalTimeElapsed() {
	return mTotalTimeElapsed;
}
}
