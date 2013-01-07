package com.spiddekauga.voider.game;

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
	public static void update(float deltaTime) {
		mTotalTimeElapsed += deltaTime;
	}

	/**
	 * @return elapsed time since the game/program started
	 */
	public static float getTotalTimeElapsed() {
		return mTotalTimeElapsed;
	}

	/**
	 * Private constructor to make sure no instance is created
	 */
	private GameTime() {
		// Does nothing
	}

	/** Total time elapsed since start of game */
	private static float mTotalTimeElapsed = 0f;
}
