package com.spiddekauga.voider.sound;

import java.util.LinkedList;
import java.util.Queue;

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
	 * {@link #play(Music, MusicInterpolations)} with play(music, Interpolations.NONE)
	 * @param music the music to start to play
	 */
	public void play(Music music) {
		play(music, MusicInterpolations.NONE);
	}

	/**
	 * Starts to play this track with the specified interpolation
	 * @param music the music to start to play. If same music piece as current or if null
	 *        is passed nothing will happen.
	 * @param interpolation type of interpolation
	 */
	public void play(Music music, MusicInterpolations interpolation) {
		mQueue.clear();
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
	 * Queues a track to play after the current has fully finished playing. This only
	 * works if the current track isn't set to loop or isn't playing at all. A call to
	 * {@link #play(Music)} or {@link #play(Music, MusicInterpolations)} clears the queue.
	 * @param music the track to push to the queue
	 * @param interpolation type of interpolation to queue with
	 */
	public void queue(Music music, MusicInterpolations interpolation) {
		boolean queue = false;
		boolean play = false;

		// Cannot queue after loopable tracks
		if (mCurrent == null) {
			if (mNext == null) {
				if (mQueue.isEmpty()) {
					play = true;
				} else {
					queue = true;
				}
			} else if (!mNext.isLoop()) {
				queue = true;
			}
		} else {
			if (mNext == null) {
				if (!mCurrent.isLoop()) {
					queue = true;
				}
			} else {
				if (!mNext.isLoop()) {
					queue = true;
				}
			}
		}

		if (queue) {
			mQueue.add(new MusicQueue(music, interpolation));
		} else if (play) {
			play(music, interpolation);
		}
	}

	/**
	 * Queues a track to play after the current has fully finished playing. This only
	 * works if the current track isn't set to loop or isn't playing at all. Uses no
	 * interpolation between tracks. A call to {@link #play(Music)} or
	 * {@link #play(Music, MusicInterpolations)} clears the queue.
	 * @param music the track to push to the queue
	 */
	public void queue(Music music) {
		queue(music, MusicInterpolations.NONE);
	}

	/**
	 * Stops / Pauses the current playing music
	 */
	public void stop() {
		stop(MusicInterpolations.NONE);
	}

	/**
	 * Stops / Pauses the current playing music with the specified interpolation
	 * @param interpolation the interpolation to use for stopping the music
	 */
	public void stop(MusicInterpolations interpolation) {
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
		resume(MusicInterpolations.NONE);
	}

	/**
	 * Resumes the stopped or paused music (if it's still loaded). Only does something if
	 * {@link #stop()} has been called and {@link #play(Music)} hasn't been called after
	 * that.
	 * @param interpolation the interpolation to start the track with
	 */
	public void resume(MusicInterpolations interpolation) {
		if (mPaused != null) {
			play(mPaused, interpolation);
		}
	}

	/**
	 * Updates the music player
	 */
	public void update() {
		updateQueue();
		interpolate();
	}

	/**
	 * Check if current track has finished, take next in queue then
	 */
	private void updateQueue() {
		if (!mQueue.isEmpty() && isCurrentDone()) {
			MusicQueue queueElement = mQueue.remove();
			mNext = queueElement.music;
			mInterpolation = queueElement.interpolation;
			interpolate();

			if (queueElement.music.isLoop()) {
				mQueue.clear();
			}
		}
	}

	/**
	 * @return true if the current track has finished playing and there is no next track
	 */
	private boolean isCurrentDone() {
		return (mCurrent == null || mCurrent.getTrack().isPlaying()) && mNext == null;
	}

	/**
	 * @return true if any music is currently playing
	 */
	public boolean isPlaying() {
		return mCurrent != null;
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

	/**
	 * Music Queue class
	 */
	private class MusicQueue {
		/**
		 * Set the music to play with an interpolation
		 * @param music
		 * @param interpolation
		 */
		private MusicQueue(Music music, MusicInterpolations interpolation) {
			this.music = music;
			this.interpolation = interpolation;
		}

		private Music music;
		private MusicInterpolations interpolation;
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
	private MusicInterpolations mInterpolation = null;
	private Music mCurrent = null;
	private Music mNext = null;
	private Music mPaused = null;
	private Queue<MusicQueue> mQueue = new LinkedList<>();
	private SettingSoundRepo mSoundRepo = SettingRepo.getInstance().sound();

	private static MusicPlayer mInstance = null;
}
