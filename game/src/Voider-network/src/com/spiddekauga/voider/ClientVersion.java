package com.spiddekauga.voider;


/**
 * All client versions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public enum ClientVersion {
	// @formatter:off
	V0_4_0("Initial version."),
	V0_5_0_TEST(true, new String [] {"This is a very very very very ver long message with no absolute information whatsoever but it is a good way to test if wrapping works OK",
	"Another point"}),

	;
	// @formatter:on

	/**
	 * If client update is necessary
	 * @param updateNeeded
	 * @param changeLogPoints all changes made in this version
	 */
	private ClientVersion(boolean updatedNeeded, String... changeLogPoints) {
		mUpdatedNeeeded = updatedNeeded;

		StringBuilder stringBuilder = new StringBuilder();

		for (String point : changeLogPoints) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("\n");
			}

			stringBuilder.append("* ");
			stringBuilder.append(point);
		}

		mChangeLog = stringBuilder.toString();
	}

	/**
	 * No update is needed for this version
	 * @param changeLogPoints all changes made in this version
	 */
	private ClientVersion(String... changeLogPoints) {
		this(false, changeLogPoints);
	}

	/**
	 * Check if a client update is needed from the specified version
	 * @param version ordinal of the version
	 * @return true if any version from [version] (not including) to the latest version
	 *         needs updating.
	 * @throws IllegalArgumentException if version is below 0 or higher than the latest
	 *         version.
	 * @see #isLatestVersion(int)
	 */
	public static boolean isUpdateNeeded(int version) {
		if (version < 0 || version >= values().length) {
			throw new IllegalArgumentException("Version (" + version + ") out of bounds");
		}

		for (int i = version + 1; i < values().length; ++i) {
			ClientVersion clientVersion = values()[i];

			if (clientVersion.mUpdatedNeeeded) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if the version is the latest version
	 * @param version
	 * @return true if version is the latest version, false otherwise
	 * @throws IllegalArgumentException if version is below 0 or higher than the latest
	 *         version.
	 * @see #isUpdateNeeded(int) if an update is needed from version
	 */
	public static boolean isLatestVersion(int version) {
		if (version < 0 || version >= values().length) {
			throw new IllegalArgumentException("Version (" + version + ") out of bounds");
		}

		return version == values().length - 1;
	}

	/**
	 * @return change-log for this version
	 */
	public String getChangeLog() {
		return toString() + "\n" + "-------------------------\n" + mChangeLog;
	}

	/**
	 * Get change-log from the specified version
	 * @param version
	 * @return all change logs from the specified version
	 * @throws IllegalArgumentException if version is below 0 or higher than the latest
	 *         version.
	 */
	public static String getChangeLogs(int version) {
		if (version < 0 || version >= values().length) {
			throw new IllegalArgumentException("Version (" + version + ") out of bounds");
		}

		StringBuilder changeLogs = new StringBuilder();

		for (int i = version + 1; i < values().length; ++i) {
			// Add delimiter between changes
			if (changeLogs.length() > 0) {
				changeLogs.append("\n\n");
			}

			ClientVersion clientVersion = values()[i];

			changeLogs.append(clientVersion.getChangeLog());
		}

		return changeLogs.toString();
	}

	/**
	 * @return latest version
	 */
	public static ClientVersion getLatest() {
		int lastIndex = ClientVersion.values().length - 1;
		return ClientVersion.values()[lastIndex];
	}

	@Override
	public String toString() {
		return name().replace('_', '.');
	}

	private boolean mUpdatedNeeeded;
	private String mChangeLog;
}
