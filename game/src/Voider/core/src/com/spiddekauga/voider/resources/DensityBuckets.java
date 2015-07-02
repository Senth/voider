package com.spiddekauga.voider.resources;

import com.badlogic.gdx.Gdx;


/**
 * Available density buckets for Voider
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum DensityBuckets {
	/** Medium DPI */
	MEDIUM(0, 239),
	/** High DPI */
	HIGH(239, 319),
	/** Extra high DPI */
	XHIGH(319, Integer.MAX_VALUE),


	;

	/**
	 * @param minDpi minimum DPI
	 * @param maxDpi maximum DPI
	 */
	private DensityBuckets(int minDpi, int maxDpi) {
		mDensityMin = minDpi / DPI_PER_DENSITY;
		mDensityMax = maxDpi / DPI_PER_DENSITY;
	}

	/**
	 * @return true current DPI is within this range
	 */
	public boolean isCurrent() {
		float density = Gdx.graphics.getDensity();
		return mDensityMin < density && density <= mDensityMax;
	}

	private float mDensityMin;
	private float mDensityMax;

	private static final int DPI_PER_DENSITY = 160;
}
