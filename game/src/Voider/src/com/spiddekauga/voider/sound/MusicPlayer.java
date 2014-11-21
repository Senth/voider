package com.spiddekauga.voider.sound;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingSoundRepo;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Singleton class for playing music.
 * @note This class doesn't load the music, this must be done by the appropriate scene. If
 *       the music isn't loaded nothing will happen.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MusicPlayer {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private MusicPlayer() {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.SOUND_MASTER_VOLUME_CHANGED, mVolumeChangeListener);
		eventDispatcher.connect(EventTypes.SOUND_MUSIC_VOLUME_CHANGED, mVolumeChangeListener);
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
	 * @param music the music to start to play. If same music piece as current or if null
	 *        is passed nothing will happen.
	 * @param interpolation type of interpolation
	 */
	public void play(Music music, Interpolations interpolation) {
		mPaused = null;
		if (music != mNext && (music != mCurrent || mInterpolation != null)) {
			// Finish current interpolation before we start playing
			if (mInterpolation != null) {
				if (mCurrent != null && mCurrent.getTrack() != null) {
					mCurrent.getTrack().stop();
				}

				mCurrent = mNext;
			}

			mNext = music;
			mInterpolation = interpolation;
			interpolate();
		}
	}

	/**
	 * Stops / Pauses the current playing music
	 */
	public void stop() {
		stop(Interpolations.NONE);
	}

	/**
	 * Stops / Pauses the current playing music with the specified interpolation
	 * @param interpolation the interpolation to use for stopping the music
	 */
	public void stop(Interpolations interpolation) {
		if (mNext != null) {
			if (mCurrent != null && mCurrent.getTrack() != null) {
				mCurrent.getTrack().stop();
			}
			mCurrent = mNext;
			mNext = null;
		}

		if (mCurrent != null) {
			mPaused = mCurrent;
			mInterpolation = interpolation;
		}
	}

	/**
	 * Resumes the stopped or paused music (if it's still loaded). Only does something if
	 * {@link #stop()} has been called and {@link #play(Music)} hasn't been called after
	 * that.
	 */
	public void resume() {
		resume(Interpolations.NONE);
	}

	/**
	 * Resumes the stopped or paused music (if it's still loaded). Only does something if
	 * {@link #stop()} has been called and {@link #play(Music)} hasn't been called after
	 * that.
	 * @param interpolation the interpolation to start the track with
	 */
	public void resume(Interpolations interpolation) {
		if (mPaused != null) {
			play(mPaused, interpolation);
		}
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

	/** Listens to when the master or music volume is changed */
	private IEventListener mVolumeChangeListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			if (mInterpolation == null && mCurrent != null) {
				float volume = mSoundRepo.getMusicVolumeOut();
				mCurrent.getTrack().setVolume(volume);
			}
		}
	};

	/** Current interpolation */
	private Interpolations mInterpolation = null;
	private Music mCurrent = null;
	private Music mNext = null;
	private Music mPaused = null;
	private SettingSoundRepo mSoundRepo = SettingRepo.getInstance().sound();

	private static MusicPlayer mInstance = null;
}
