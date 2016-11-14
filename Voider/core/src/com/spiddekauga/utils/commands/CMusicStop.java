package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.sound.MusicPlayer;

/**
 * Stops playing music
 */
public class CMusicStop extends CRun {
private static MusicPlayer mMusicPlayer = MusicPlayer.getInstance();
private MusicInterpolations mInterpolation = MusicInterpolations.NONE;

/**
 * Stop playing music with no interpolation
 */
public CMusicStop() {

}

/**
 * Stop playing music with the specified interpolation
 * @param interpolation
 */
public CMusicStop(MusicInterpolations interpolation) {
	mInterpolation = interpolation;
}

@Override
public boolean execute() {
	mMusicPlayer.stop(mInterpolation);
	return true;
}
}
