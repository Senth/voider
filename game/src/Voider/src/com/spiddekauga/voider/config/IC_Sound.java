package com.spiddekauga.voider.config;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.utils.IniClass;

/**
 * Sound/Music settings
 * @author Matteus Magnusson <matteus.magnusso@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class IC_Sound extends IniClass {
	public IC_Music music;

	public class IC_Music extends IniClass {
		protected float fadeTime;

		private IC_Music(Ini ini, Section section) {
			super(ini, section);
		}

		public float getFadeTime() {
			return fadeTime;
		}
	}


	IC_Sound(Ini ini, Section section) {
		super(ini, section);
	}
}
