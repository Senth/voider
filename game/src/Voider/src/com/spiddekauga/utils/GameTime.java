package com.spiddekauga.utils;

/**
 * Measures the total game time of the game.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameTime {
	/**
	 * Updates the game time, call once per frame
	 * @param deltaTime elapsed time since last frame
	 */
	public void update(float deltaTime) {
		mTotalTimeElapsed += deltaTime;
	}

	/**
	 * @return elapsed time since the game/program started
	 */
	public float getTotalTimeElapsed() {
		return mTotalTimeElapsed;
	}

	/** Total time elapsed since start of game */
	private float mTotalTimeElapsed = 0f;
}
