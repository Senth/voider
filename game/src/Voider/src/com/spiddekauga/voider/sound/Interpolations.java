package com.spiddekauga.voider.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Sound.IC_Music;

/**
 * All different interpolations for music and sound effects
 * @author Matteus Magnusson <matteus.magnusso@spiddekauga.com>
 */
public enum Interpolations {
	/** No interpolation */
	NONE,
	/** Fade in. If music is playing it will stop the music and fade in this. @see CROSSFADE */
	FADE_IN,
	/** Fades out the currently playing music. Usually used together with stop. */
	FADE_OUT,
	/** Fades out the currently playing music/sound while fading in the new. @see FADE_OUT_FADE_IN */
	CROSSFADE,
	/** First fades out the currently playing music entirely and then fades in the new music @see CROSSFADE */
	FADE_OUT_FADE_IN,

	;

	/**
	 * @return Interpolation method to use for this type
	 */
	IInterpolationMethod getMethod() {
		return mInterpolationMethod;
	}

	private IInterpolationMethod mInterpolationMethod = null;


	// Create interpolation methods
	static {
		// NONE
		NONE.mInterpolationMethod = new IInterpolationMethod() {
			@Override
			public boolean interpolate(Music current, Music next, float maxVolume) {
				if (current != null && current.isPlaying()) {
					current.stop();
				}

				if (next != null && !next.isPlaying()) {
					next.play();
				}

				return true;
			}
		};

		// FADE_IN
		FADE_IN.mInterpolationMethod = new IInterpolationMethod() {
			@Override
			public boolean interpolate(Music current, Music next, float maxVolume) {
				// Current
				if (current != null && current.isPlaying()) {
					current.stop();
				}

				// Next
				if (next != null) {
					// Start at 0 volume
					if (!next.isPlaying()) {
						next.setVolume(0);
						next.play();
					}
					// Continue to fade in
					else {
						float interpolatedVolume = getInterpolationValue(maxVolume);
						float volume = next.getVolume() + interpolatedVolume;
						setVolume(next, volume, maxVolume);
					}
				}

				return next == null || next.getVolume() == maxVolume;
			}
		};

		// FADE_OUT
		FADE_OUT.mInterpolationMethod = new IInterpolationMethod() {
			@Override
			public boolean interpolate(Music current, Music next, float maxVolume) {
				// Current
				if (current != null && current.isPlaying()) {
					// Fade out
					if (current.getVolume() > 0) {
						float interpolatedVolume = getInterpolationValue(maxVolume);
						float volume = current.getVolume() - interpolatedVolume;
						setVolume(current, volume, maxVolume);
					}

					// Stop
					if (current.getVolume() == 0) {
						current.stop();
					}
				}

				// Next - Start playing
				if (next != null && (current == null || !current.isPlaying())) {
					next.setVolume(maxVolume);
					next.play();
				}


				return (current == null || !current.isPlaying());
			}
		};

		// CROSSFADE
		CROSSFADE.mInterpolationMethod = new IInterpolationMethod() {
			@Override
			public boolean interpolate(Music current, Music next, float maxVolume) {
				float interpolatedVolume = getInterpolationValue(maxVolume);

				// Current
				if (current != null && current.isPlaying()) {
					// Fade out
					if (current.getVolume() > 0) {
						float volume = current.getVolume() - interpolatedVolume;
						setVolume(current, volume, maxVolume);
					}

					// Stop
					if (current.getVolume() == 0) {
						current.stop();
					}
				}

				// Next - Fade in
				if (next != null) {
					// Start playing
					if (!next.isPlaying()) {
						next.setVolume(0);
						next.play();
					}
					// Continue to fade in
					else {
						float volume = next.getVolume() + interpolatedVolume;
						setVolume(next, volume, maxVolume);
					}
				}

				return (current == null || !current.isPlaying()) && (next == null || next.getVolume() == maxVolume);
			}
		};

		// FADE_OUT_FADE_IN
		FADE_OUT_FADE_IN.mInterpolationMethod = new IInterpolationMethod() {
			@Override
			public boolean interpolate(Music current, Music next, float maxVolume) {
				// Current
				if (current != null && current.isPlaying()) {
					// Fade out
					if (current.getVolume() > 0) {
						float interpolatedVolume = getInterpolationValue(maxVolume);
						float volume = current.getVolume() - interpolatedVolume;
						setVolume(current, volume, maxVolume);
					}

					// Stop
					if (current.getVolume() == 0) {
						current.stop();
					}
				}

				// Next - Start playing
				if (next != null && (current == null || !current.isPlaying())) {
					// Start playing
					if (!next.isPlaying()) {
						next.setVolume(0);
						next.play();
					}
					// Continue to fade in
					else {
						float interpolatedVolume = getInterpolationValue(maxVolume);
						float volume = next.getVolume() + interpolatedVolume;
						setVolume(next, volume, maxVolume);
					}
				}

				return (current == null || !current.isPlaying()) && (next == null || next.getVolume() == maxVolume);
			}
		};
	}

	/**
	 * Set the volume of the music and clamp it
	 * @param music the music to set the volume for
	 * @param volume the new volume
	 * @param maxVolume maximum volume
	 */
	private static void setVolume(Music music, float volume, float maxVolume) {
		float value = volume;

		if (volume < 0) {
			value = 0;
		} else if (volume > maxVolume) {
			value = maxVolume;
		}

		music.setVolume(value);
	}

	/**
	 * How much we interpolate with depending on the time
	 * @param maxVolume maximum volume to interpolate to
	 * @return amount to interpolate
	 */
	private static float getInterpolationValue(float maxVolume) {
		IC_Music icMusic = ConfigIni.getInstance().sound.music;

		float value = maxVolume * icMusic.getFadeTime() * Gdx.graphics.getDeltaTime();
		return value;
	}
}
