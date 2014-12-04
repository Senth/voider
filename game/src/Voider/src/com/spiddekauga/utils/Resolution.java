package com.spiddekauga.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;

/**
 * Window resolution
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Resolution implements Comparable<Resolution> {
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
	public static Resolution[] getFullscreenResolutions() {
		Set<Resolution> resolutionSet = new HashSet<>();

		// Only add the resolution once (skip too low resolutions)
		for (DisplayMode displayMode : Gdx.graphics.getDisplayModes()) {
			Resolution resolution = new Resolution(displayMode);
			if (resolution.getWidth() >= Config.Graphics.WIDTH_DEFAULT && resolution.getHeight() >= Config.Graphics.HEIGHT_DEFAULT) {
				resolutionSet.add(resolution);
			}
		}

		// Sort by resolution
		ArrayList<Resolution> sortedResolutions = new ArrayList<>(resolutionSet);
		Collections.sort(sortedResolutions);

		Resolution[] availableResolutions = new Resolution[resolutionSet.size()];
		sortedResolutions.toArray(availableResolutions);
		return availableResolutions;
	}

	/**
	 * @return all available windowed resolutions in the game
	 */
	public static Resolution[] getWindowedResolutions() {
		Set<Resolution> resolutionSet = new HashSet<>();

		int maxWidth = 0;
		int maxHeight = 0;

		// Only add the resolution once
		for (DisplayMode displayMode : Gdx.graphics.getDisplayModes()) {
			Resolution resolution = new Resolution(displayMode);
			if (resolution.getWidth() >= Config.Graphics.WIDTH_DEFAULT && resolution.getHeight() >= Config.Graphics.HEIGHT_DEFAULT) {
				resolutionSet.add(resolution);

				if (resolution.getWidth() > maxWidth) {
					maxWidth = resolution.getWidth();
				}
				if (resolution.getHeight() > maxHeight) {
					maxHeight = resolution.getHeight();
				}
			}
		}

		// Add custom window resolution
		String[] customResolutionStrings = ConfigIni.getInstance().setting.display.getCustomWindowResolutions();
		for (String resolutionString : customResolutionStrings) {
			try {
				Resolution resolution = new Resolution(resolutionString);

				// Only add if this display is large enough
				if (resolution.getWidth() <= maxWidth && resolution.getHeight() <= maxHeight) {
					resolutionSet.add(resolution);
				}
			} catch (IllegalArgumentException e) {
				Gdx.app.error("Resolution", e.getMessage());
			}
		}

		// Sort by resolution
		ArrayList<Resolution> sortedResolutions = new ArrayList<>(resolutionSet);
		Collections.sort(sortedResolutions);

		Resolution[] availableResolutions = new Resolution[resolutionSet.size()];
		sortedResolutions.toArray(availableResolutions);
		return availableResolutions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mWidth * 100000;
		result = prime * result + mHeight;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Resolution other = (Resolution) obj;
		if (mHeight != other.mHeight) {
			return false;
		}
		if (mWidth != other.mWidth) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Resolution other) {
		if (mWidth < other.mWidth) {
			return 1;
		}
		if (mWidth > other.mWidth) {
			return -1;
		}
		if (mHeight < other.mHeight) {
			return 1;
		}
		if (mHeight > other.mHeight) {
			return -1;
		}

		return 0;
	}


	private int mWidth = 0;
	private int mHeight = 0;
}
