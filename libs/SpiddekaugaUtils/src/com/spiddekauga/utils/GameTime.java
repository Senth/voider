package com.spiddekauga.utils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Measures the total game time of the game. If a frame is longer than 0.1s
 * it will clamp the delta time to 0.1s
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameTime {
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

	/** Total time elapsed since start of game, in seconds */
	@Tag(105) private float mTotalTimeElapsed = 0;
	/** Current delta time, in seconds */
	@Tag(106) private float mDeltaTime = 0;

	// Static methods
	/**
	 * Updates the global game time, call once per fram
	 * @param deltaTime elapsed time since last frame
	 */
	public static void updateGlobal(float deltaTime) {
		mTotalGlobalTimeElapsed += deltaTime;
	}

	/**
	 * @return elapsed global time since the game/program started, in seconds.
	 */
	public static float getTotalGlobalTimeElapsed() {
		return mTotalGlobalTimeElapsed;
	}

	/** Total global time elapsed since start of game */
	private static float mTotalGlobalTimeElapsed = 0;
	/** Maximum time between frames, useful when debugging or lagging so the game doesn't bug out. */
	private static float FRAME_LENGTH_MAX = 0.1f;
}
