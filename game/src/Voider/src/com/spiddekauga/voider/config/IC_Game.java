package com.spiddekauga.voider.config;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.utils.IniClass;

/**
 * Game settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class IC_Game extends IniClass {
	private float layerTopSpeed;
	private float layerBottomSpeed;


	IC_Game(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	public float getLayerTopSpeed() {
		return layerTopSpeed;
	}

	public float getLayerBottomSpeed() {
		return layerBottomSpeed;
	}
}
