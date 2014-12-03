package com.spiddekauga.utils;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;

/**
 * Window resolution
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Resolution {
	/**
	 * Creates a resolution out of a string.
	 * @param resolution should be in format "1280x720"
	 * @throws IllegalArgumentException if the string is in a wrong format or null.
	 */
	public Resolution(String resolution) {
		if (resolution == null) {
			throw new IllegalArgumentException("String resolution is null");
		} else if (resolution.isEmpty()) {
			throw new IllegalArgumentException("String resolution is empty");
		}

		String[] split = resolution.split("x");
		if (split.length != 2) {
			throw new IllegalArgumentException("String resolution is not in a valid format: " + resolution);
		}

		try {
			mWidth = Integer.parseInt(split[0]);
			mHeight = Integer.parseInt(split[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Resolution is not in a valid format: " + resolution);
		}
	}

	/**
	 * Creates a resolution from a DisplayMode
	 * @param displayMode
	 */
	public Resolution(DisplayMode displayMode) {
		mWidth = displayMode.width;
		mHeight = displayMode.height;
	}

	/**
	 * Creates a custom resolution
	 * @param width
	 * @param height
	 */
	public Resolution(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	@Override
	public String toString() {
		return mWidth + "x" + mHeight;
	}

	/**
	 * @return resolution width
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * @return resolution height
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * @return all available screen resolutions in the game
	 */
	public static Resolution[] getAvailableResolutions() {
		Set<Resolution> resolutions = new HashSet<>();

		for (DisplayMode displayMode : Gdx.graphics.getDisplayModes()) {
			resolutions.add(new Resolution(displayMode));
		}

		// TODO sort

		Resolution[] availableResolutions = new Resolution[resolutions.size()];
		resolutions.toArray(availableResolutions);
		return availableResolutions;
	}


	private int mWidth = 0;
	private int mHeight = 0;
}
