package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.sound.MusicPlayer;

/**
 * Play a music track
 */
public class CMusicPlay extends CRun {
private static MusicPlayer mMusicPlayer = MusicPlayer.getInstance();
private Music mMusic;
private MusicInterpolations mInterpolation;

/**
 * Play the specified music
 * @param music
 */
public CMusicPlay(Music music) {
	this(music, null);
}
/**
 * Play the specified music with the specified interpolation
 * @param music
 * @param interpolation
 */
public CMusicPlay(Music music, MusicInterpolations interpolation) {
	mMusic = music;
	mInterpolation = interpolation;
}

@Override
public boolean execute() {
	if (mInterpolation == null) {
		mMusicPlayer.play(mMusic);
	} else {
		mMusicPlayer.play(mMusic, mInterpolation);
	}

	return true;
}
}
