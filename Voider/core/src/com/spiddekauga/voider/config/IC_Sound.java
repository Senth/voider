package com.spiddekauga.voider.config;

import com.spiddekauga.utils.IniClass;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 * Sound/Music configuration values
 */
@SuppressWarnings("javadoc")
public class IC_Sound extends IniClass {
public IC_Music music;
public IC_Effect effect;

IC_Sound(Ini ini, Section section) {
	super(ini, section);
}

/**
 * Internal music configuration values
 */
public class IC_Music extends IniClass {
	protected float fadeTime;

	private IC_Music(Ini ini, Section section) {
		super(ini, section);
	}

	public float getFadeTime() {
		return fadeTime;
	}
}

/**
 * Internal sound effects configuration values
 */
public class IC_Effect extends IniClass {
	protected float fadeTime;
	protected float lowHealthTime;
	protected float lowHealthPercent;

	private IC_Effect(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	public float getLowHealthTime() {
		return lowHealthTime;
	}

	public float getFadeTime() {
		return fadeTime;
	}

	/**
	 * @return [0, 1] range
	 */
	public float getLowHealthPercent() {
		return lowHealthPercent;
	}
}
}
