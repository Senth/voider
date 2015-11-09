package com.spiddekauga.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Measures how long things take.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Profiler {

	/**
	 * Start measuring the time. This automatically resets the profiler
	 */
	public void start() {
		reset();
		mStartTime = System.currentTimeMillis();
	}

	/**
	 * Stop measuring the time
	 */
	public void stop() {
		mEndTime = System.currentTimeMillis();
	}

	/**
	 * Add an intermediate stop
	 * @param name the name of the point
	 */
	public void addIntermediateTime(String name) {
		mIntermediateTimes.add(new TimeWrapper(name));
	}

	/**
	 * Get string with all information (used for printing)
	 * @param header optional header for the information (can be null)
	 * @return string with all information
	 */
	public String toString(String header) {
		StringBuilder stringBuilder = new StringBuilder();

		// Header
		if (header != null) {
			stringBuilder.append(header).append('\n');
		}


		// Total time
		long totalTime = mEndTime - mStartTime;
		stringBuilder.append("Total Time: ").append(totalTime).append("ms\n");


		// Intermediate times
		long previousTime = mStartTime;
		for (TimeWrapper intermediateTime : mIntermediateTimes) {
			// Time
			long diffTime = intermediateTime.time - previousTime;
			previousTime = intermediateTime.time;

			String diffTimeString = Strings.padRight(Long.toString(diffTime) + "ms", mPadding);
			stringBuilder.append(diffTimeString);

			// Percentage
			double percentageTime = ((double) diffTime) / totalTime;
			String percentageTimeString = String.format("%.2f", percentageTime);
			stringBuilder.append(percentageTimeString).append("%    ");

			// Name
			stringBuilder.append(intermediateTime.name).append("\n");
		}


		return stringBuilder.toString();
	}

	/**
	 * Same as {@link #toString(String)} with null as the header argument
	 */
	@Override
	public String toString() {
		return toString(null);
	}

	/**
	 * Reset the profiler
	 */
	public void reset() {
		mStartTime = 0;
		mEndTime = 0;
		mIntermediateTimes.clear();
	}

	/**
	 * Change the padding (total number of characters) in the intermediate times
	 * @param padding total number of characters in intermediate times before the name
	 */
	public void setPadding(int padding) {
		mPadding = padding;
	}

	/**
	 * Wrapper for time and name
	 */
	private static class TimeWrapper {
		private TimeWrapper(String name) {
			this.name = name;
		}

		private String name;
		private long time = System.currentTimeMillis();
	}

	private int mPadding = 10;
	private long mStartTime;
	private long mEndTime;
	private List<TimeWrapper> mIntermediateTimes = new ArrayList<>();
}
