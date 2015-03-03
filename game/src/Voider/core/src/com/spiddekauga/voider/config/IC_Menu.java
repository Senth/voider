package com.spiddekauga.voider.config;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.utils.IniClass;
import com.spiddekauga.utils.Strings;

/**
 * Menu variables
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class IC_Menu extends IniClass {
	public IC_Time time;

	/**
	 * Scene time variables
	 */
	public class IC_Time extends IniClass {
		protected float splashScreenTime;
		protected float splashScreenFadeIn;
		protected float splashScreenFadeOut;
		protected float sceneUiFadeIn;
		protected float sceneUiFadeOut;
		protected float sceneEnterTime;
		protected float sceneExitTime;
		/** How many words we can read per minute, helpful for loading text scenes */
		protected float wordsPerMinute;

		private IC_Time(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public float getSplashScreenTime() {
			return splashScreenTime;
		}

		public float getSplashScreenFadeIn() {
			return splashScreenFadeIn;
		}

		public float getSplashScreenFadeOut() {
			return splashScreenFadeOut;
		}

		public float getSceneUiFadeIn() {
			return sceneUiFadeIn;
		}

		public float getSceneUiFadeOut() {
			return sceneUiFadeOut;
		}

		public float getSceneEnterTime() {
			return sceneEnterTime;
		}

		public float getSceneExitTime() {
			return sceneExitTime;
		}

		public float getWordsPerMinute() {
			return wordsPerMinute;
		}

		/**
		 * Calculate how many seconds the text should be displayed. This depends on
		 * {@link #getWordsPerMinute()}.
		 * @param text the text to calculate
		 * @return number of seconds to display this text
		 */
		public float getDisplayTime(String text) {
			if (text != null) {
				float wordsPerSecond = wordsPerMinute / 60;
				if (wordsPerSecond != 0) {
					int cWords = Strings.wordCount(text);
					return cWords / wordsPerSecond;
				}
			}
			return 0;
		}
	}

	IC_Menu(Ini ini, Section classSection) {
		super(ini, classSection);
	}

}
