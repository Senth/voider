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
	private double multiplierDecrement;
	private float multiplierCollisionCooldown;
	private float invulnerableTimeOnShipLost;


	IC_Game(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	public float getLayerTopSpeed() {
		return layerTopSpeed;
	}

	public float getLayerBottomSpeed() {
		return layerBottomSpeed;
	}

	public double getMultiplierDecrement() {
		return multiplierDecrement;
	}

	public float getMultiplierCollisionCooldown() {
		return multiplierCollisionCooldown;
	}

	public float getInvulnerableTimeOnShipLost() {
		return invulnerableTimeOnShipLost;
	}
}
