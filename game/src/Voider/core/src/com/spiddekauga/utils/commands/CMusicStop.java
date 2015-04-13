package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.sound.MusicPlayer;

/**
 * Stops playing music
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CMusicStop extends CRun {
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

	private MusicInterpolations mInterpolation = MusicInterpolations.NONE;
	private static MusicPlayer mMusicPlayer = MusicPlayer.getInstance();
}
