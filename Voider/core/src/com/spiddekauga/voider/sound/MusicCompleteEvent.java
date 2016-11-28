package com.spiddekauga.voider.sound;

/**
 * Posted when a music piece has finished playing. Fired for non-loopable pieces, never fired when
 * stop is called.
 */
public class MusicCompleteEvent {
private Music mMusic;

/**
 * New music complete event
 * @param music the music that finished playing
 */
MusicCompleteEvent(Music music) {
	mMusic = music;
}

/**
 * @return the music piece that finished playing.
 */
public Music getMusic() {
	return mMusic;
}
}
