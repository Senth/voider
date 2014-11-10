package com.spiddekauga.voider.sound;

/**
 * Singleton class for playing music.
 * @note This class doesn't load the music, this must be done by the appropriate scene. If
 *       the music isn't loaded nothing will happen.
 * @author Matteus Magnusson <matteus.magnusso@spiddekauga.com>
 */
public class MusicPlayer {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private MusicPlayer() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public MusicPlayer getInstance() {
		if (mInstance == null) {
			mInstance = new MusicPlayer();
		}
		return mInstance;
	}

	/**
	 * Starts to play this music track. This is same as calling
	 * {@link #play(Music, Interpolations)} with play(music, Interpolations.NONE)
	 * @param music the music to start to play
	 */
	public void play(Music music) {
		play(music, Interpolations.NONE);
	}

	/**
	 * Starts to play this track with the specified interpolation
	 * @param music the music to start to play, if null it will stop the music
	 * @param interpolation type of interpolation
	 */
	public void play(Music music, Interpolations interpolation) {
		// TODO Get next music piece
	}

	/**
	 * Stops / Pauses the current playing music
	 */
	public void stop() {
		play(null, Interpolations.NONE);
	}

	/**
	 * Stops / Pauses the current playing music with the specified interpolation
	 * @param interpolation the interpolation to use for stopping the music
	 */
	public void stop(Interpolations interpolation) {
		play(null, interpolation);
	}

	/**
	 * Updates the music player
	 */
	public void update() {
		interpolate();
	}

	/**
	 * Interpolates the music
	 */
	private void interpolate() {
		if (mInterpolation != null) {
			IInterpolationMethod method = mInterpolation.getMethod();
			// TODO Get volume
			boolean done = method.interpolate(mCurrent, mNext, 0.5f);

			if (done) {
				mInterpolation = null;

				// TODO unload current piece?

				// Set next as current
				mCurrent = mNext;
				mNext = null;
			}
		}
	}

	/** Current interpolation */
	private Interpolations mInterpolation = null;
	private com.badlogic.gdx.audio.Music mCurrent = null;
	private com.badlogic.gdx.audio.Music mNext = null;

	private static MusicPlayer mInstance = null;
}
