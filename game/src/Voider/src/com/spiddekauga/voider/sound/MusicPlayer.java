package com.spiddekauga.voider.sound;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo.SoundSettingLocalRepo;

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
	public static MusicPlayer getInstance() {
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
	 * @param music the music to start to play. If null it will stop the music. If same
	 *        music piece as current nothing will happen.
	 * @param interpolation type of interpolation
	 */
	public void play(Music music, Interpolations interpolation) {
		if (music != mCurrent && mNext == null) {
			mNext = music;
			mInterpolation = interpolation;
		}
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
			float volume = mSoundRepo.getMusicVolumeOut();
			com.badlogic.gdx.audio.Music currentTrack = mCurrent != null ? mCurrent.getTrack() : null;
			com.badlogic.gdx.audio.Music nextTrack = mNext != null ? mNext.getTrack() : null;

			boolean done = method.interpolate(currentTrack, nextTrack, volume);

			// Set next as current
			if (done) {
				mInterpolation = null;
				mCurrent = mNext;
				mNext = null;
				Gdx.app.debug("MusicPlayer", "Interpolation done");
			}
		}
	}

	/** Current interpolation */
	private Interpolations mInterpolation = null;
	private Music mCurrent = null;
	private Music mNext = null;
	private SoundSettingLocalRepo mSoundRepo = SettingLocalRepo.getInstance().sound;

	private static MusicPlayer mInstance = null;
}
