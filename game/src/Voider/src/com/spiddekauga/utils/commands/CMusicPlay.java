package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.sound.MusicPlayer;

/**
 * Play a music track
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CMusicPlay extends Command {
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

	@Override
	public boolean undo() {
		return false;
	}

	private Music mMusic;
	private MusicInterpolations mInterpolation;
	private static MusicPlayer mMusicPlayer = MusicPlayer.getInstance();
}
