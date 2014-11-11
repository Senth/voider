package com.spiddekauga.voider.config;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.utils.IniClass;

/**
 * Default user settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class IC_Setting extends IniClass {
	public IC_Sound sound;

	/**
	 * Default user sound settings
	 */
	public class IC_Sound extends IniClass {
		protected float effects;
		protected float ui;
		protected float master;
		protected float music;

		private IC_Sound(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public float getEffects() {
			return effects;
		}

		public float getUi() {
			return ui;
		}

		public float getMaster() {
			return master;
		}

		public float getMusic() {
			return music;
		}
	}

	IC_Setting(Ini ini, Section classSection) {
		super(ini, classSection);
	}
}
