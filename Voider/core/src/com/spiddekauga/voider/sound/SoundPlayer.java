package com.spiddekauga.voider.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingSoundRepo;

/**
 * Singleton class for playing sound effects
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SoundPlayer {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private SoundPlayer() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static SoundPlayer getInstance() {
		if (mInstance == null) {
			mInstance = new SoundPlayer();
		}
		return mInstance;
	}

	/**
	 * Play a sound. If the sound is loopable it can be stopped by calling
	 * {@link #stop(Sounds)}. Does nothing if it's a loopable sound and it's already
	 * playing.
	 * @param sound the sound to play.
	 */
	public void play(Sounds sound) {
		if (sound.isLoopable()) {
			playLoop(sound);
		} else {
			playRegular(sound);
		}
	}

	/**
	 * Play a regular non-loopable sound
	 * @param sound the sound to play
	 */
	private void playRegular(Sounds sound) {
		Sound track = sound.getTrack();
		track.play(getVolume(sound));
	}

	/**
	 * Play a loopable sound
	 * @param sound the sound to play
	 */
	private void playLoop(Sounds sound) {
		// Never play if already playing...
		if (!sound.isPlaying()) {
			Sound track = sound.getTrack();
			long id = track.loop(getVolume(sound));
			sound.setLoopId(id);

			// Automatically end the loop
			if (!sound.isLoopingForever()) {
				new StopTrackIn(sound, sound.getLoopTime()).start();;
			}

		} else {
			Gdx.app.log("SoundPlayer", "Loop sound is already playing!");
		}
	}

	/**
	 * Stops a loopable sound. Does nothing if it's not playing
	 * @param sound the sound to stop
	 */
	public void stop(Sounds sound) {
		if (sound.isPlaying()) {
			new FadeOutThenStop(sound).start();
		}
	}

	/**
	 * Stop all loopable sounds.
	 */
	public void stopAll() {
		for (Sounds sound : Sounds.values()) {
			stop(sound);
		}
	}

	/**
	 * Get the volume to use for the sound
	 * @param sound
	 * @return volume to use for the sound
	 */
	private float getVolume(Sounds sound) {
		switch (sound.getEffectCategory()) {
		case GAME:
			return mSettingSoundRepo.getEffectsVolumeOut();
		case UI:
			return mSettingSoundRepo.getUiVolumeOut();
		}
		return 0;
	}

	/**
	 * Stop playing a track after x seconds. Polls ever 10ms to check if the sound has
	 * been stopped manually. If it has, the thread will die
	 */
	private class StopTrackIn extends Thread {
		/**
		 * @param sound the sound to stop after x seconds
		 * @param seconds how many seconds until the sound effect is stopped
		 */
		StopTrackIn(Sounds sound, float seconds) {
			mSound = sound;
			mStopInSeconds = seconds;
		}

		@Override
		public void run() {
			float startTime = GameTime.getTotalGlobalTimeElapsed();
			float endTime = startTime + mStopInSeconds;
			while (mSound.isPlaying()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				float currentTime = GameTime.getTotalGlobalTimeElapsed();
				// Stop sound
				if (currentTime >= endTime) {
					SoundPlayer.this.stop(mSound);
				}
			}
		}

		private Sounds mSound;
		private float mStopInSeconds;
	}

	/**
	 * Fade-out then stop track
	 */
	private class FadeOutThenStop extends Thread {
		/**
		 * @param sound the sound to fade-out then stop
		 */
		FadeOutThenStop(Sounds sound) {
			mSound = sound;
			mVolumeMax = getVolume(sound);
		}

		@Override
		public void run() {
			float startTime = GameTime.getTotalGlobalTimeElapsed();
			while (mSound.isPlaying()) {
				float timeDiff = GameTime.getTotalGlobalTimeElapsed() - startTime;
				Sound track = mSound.getTrack();
				if (track == null) {
					mSound.setLoopId(Sounds.INVALID_ID);
					return;
				}

				// Fade
				if (timeDiff < mFadeTime) {
					float scale = 1 - timeDiff / mFadeTime;
					float volume = mVolumeMax * scale;
					track.setVolume(mSound.getLoopId(), volume);
				}
				// Stop
				else {
					track.stop(mSound.getLoopId());
					mSound.setLoopId(Sounds.INVALID_ID);
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private Sounds mSound;
		private float mVolumeMax;
		private float mFadeTime = ConfigIni.getInstance().sound.effect.getFadeTime();
	}

	private SettingSoundRepo mSettingSoundRepo = SettingRepo.getInstance().sound();
	private static SoundPlayer mInstance = null;
}
