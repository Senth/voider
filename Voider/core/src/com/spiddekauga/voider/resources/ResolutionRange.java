package com.spiddekauga.voider.resources;


/**
 * What type of resolution the internal resources are used for

 */
public class ResolutionRange {
	/**
	 * @param min minimum height
	 * @param max maximum height
	 * @param optimal optimal/real height
	 */
	public ResolutionRange(int min, int max, int optimal) {
		mMin = min;
		mMax = max;
		mOptimal = optimal;
	}

	/**
	 * Checks if the height is within range
	 * @param height
	 * @return true if the height is within this resolution def's range
	 */
	public boolean isWithinRange(int height) {
		return mMin <= height && height <= mMax;
	}

	/**
	 * How far away the height is from the optimal height
	 * @param height
	 * @return difference between height and optimal height, 0 if equal to the optimal
	 *         height, always positive
	 */
	public int getOptimalDifference(int height) {
		if (height > mOptimal) {
			return height - mOptimal;
		} else {
			return mOptimal - height;
		}
	}

	private int mMin;
	private int mMax;
	private int mOptimal;


	// Common resolution resources
	/** 120p files used for UI */
	static final ResolutionRange RES_ALL = new ResolutionRange(0, Integer.MAX_VALUE, 0);
	/** 720p backgrounds */
	static final ResolutionRange RES_720 = new ResolutionRange(0, 900, 720);
	/** 1080p backgrounds */
	static final ResolutionRange RES_1080 = new ResolutionRange(901, Integer.MAX_VALUE, 1080);
}
