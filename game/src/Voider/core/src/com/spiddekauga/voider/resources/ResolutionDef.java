package com.spiddekauga.voider.resources;

import com.badlogic.gdx.Gdx;

/**
 * What type of resolution the internal resources are used for
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ResolutionDef {
	/**
	 * @param min minimum height
	 * @param max maximum height
	 */
	ResolutionDef(int min, int max) {
		mHeightMin = min;
		mHeightMax = max;
	}

	/**
	 * @return true if current resolution is within this range
	 */
	boolean isResolutionWithinRange() {
		int height = Gdx.graphics.getHeight();

		return mHeightMin <= height && height <= mHeightMax;
	}

	private int mHeightMin;
	private int mHeightMax;

	// Common resolution resources
	/** 120p files used for UI */
	static final ResolutionDef RES_ALL = new ResolutionDef(0, Integer.MAX_VALUE);
	/** 480p backgrounds */
	static final ResolutionDef RES_480 = new ResolutionDef(0, 600);
	/** 720p backgrounds */
	static final ResolutionDef RES_720 = new ResolutionDef(601, 900);
	/** 1080p backgrounds */
	static final ResolutionDef RES_1080 = new ResolutionDef(901, Integer.MAX_VALUE);
}
