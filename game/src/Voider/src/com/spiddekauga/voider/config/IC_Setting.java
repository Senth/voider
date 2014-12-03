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
	public IC_Network network;
	public IC_General general;
	public IC_Display display;

	/**
	 * Display settings
	 */
	public class IC_Display extends IniClass {
		protected boolean fullscreen;
		protected int resolutionWidth;
		protected int resolutionHeight;

		private IC_Display(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public boolean isFullscreen() {
			return fullscreen;
		}

		public int getResolutionWidth() {
			return resolutionWidth;
		}

		public int getResolutionHeight() {
			return resolutionHeight;
		}
	}

	/**
	 * General settings
	 */
	public class IC_General extends IniClass {
		protected String[] dateFormats;
		protected String time24hFormat;
		protected String timeAmPmFormat;

		private IC_General(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public String[] getDateFormats() {
			return dateFormats;
		}

		public String getTime24hFormat() {
			return time24hFormat;
		}

		public String getTimeAmPmFormat() {
			return timeAmPmFormat;
		}
	}

	/**
	 * Default network settings
	 */
	public class IC_Network extends IniClass {
		protected boolean autoConnectByDefault;

		private IC_Network(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public boolean isAutoConnectByDefault() {
			return autoConnectByDefault;
		}
	}

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
