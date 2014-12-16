package com.spiddekauga.voider;


/**
 * All client versions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public enum ClientVersions {
	// @formatter:off
	V0_4_0("Pre-beta version."),
	V0_4_1(true,
			"Play/Explore",
			"	Explore has been merged into Play as Explore essentially was play online.",
			"	The same Play/Explore screen is now used for loading levels, enemies, and bullets both online and locally.",
			"	You can now search for enemies and bullets online for your levels.",
			"	Searching for levels have new filters: level length and level speed.",
			"	Levels can now be filtered by tags in 'search' too.",
			"Bug Fixes",
			"	Notifications are now displayed correctly after rezising the window.",
			"	Level score and play count are now correctly saved after playing a level.",
			"UI",
			"	Top left icons in Explore now have similar styles for online and local resources.",
			"	Display Settings icon now looks like a desktop monitor.",
			"	Zoom in, out and Reset Zoom now have icons.",
			"	Toggle background in Level Editor now has an icon.",
			"	Play/Stop music in Level Editor now has the correct icons."),
	V0_4_2(false,
			"Quickfix: Can now load/play local levels"),
	V0_4_3(false,
			"Improvements",
			"	Levels need to be at least 30s long to be published (this to avoid publishing empty or unfinished levels).",
			"	Bug Report window",
			"		You can now view the additional information that is sent to the server.",
			"	ChangeLog is now displayed after the client/app has been updated",
			"	Progress bar is now shown when opening editors.",
			"Bug Fixes",
			"	Level Editor",
			"		Testing a level from the editor now uses the correct level speed.",
			"		Level screenshots now have the same resolution independent of the window resolution.",
			"		Opacity now works correctly in level editor and when playing a level",
			"		Selection tool is now selected (correctly) as default tool in the level editor",
			"	ChangeLogs (these messages) are now tabbed correctly",
			"	In-game notifications are now correctly displayed"),

	;
	// @formatter:on

	/**
	 * If client update is necessary
	 * @param updateNeeded
	 * @param changeLogPoints all changes made in this version
	 */
	private ClientVersions(boolean updatedNeeded, String... changeLogPoints) {
		mUpdatedNeeeded = updatedNeeded;

		StringBuilder stringBuilder = new StringBuilder();

		for (String point : changeLogPoints) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("\n");
			}

			// For every tab at the front of the point, indent the point
			int index = 0;
			boolean foundAllTabs = false;
			do {
				if (point.charAt(index) == '\t') {
					stringBuilder.append(TAB_REPLACEMENT);
				} else {
					foundAllTabs = true;
				}
				index++;
			} while (index < point.length() && !foundAllTabs);

			stringBuilder.append("* ");
			stringBuilder.append(point.trim());
		}

		mChangeLog = stringBuilder.toString();
	}

	/**
	 * @return id of the version
	 */
	public int getId() {
		return ordinal();
	}

	/**
	 * No update is needed for this version
	 * @param changeLogPoints all changes made in this version
	 */
	private ClientVersions(String... changeLogPoints) {
		this(false, changeLogPoints);
	}

	/**
	 * Get version from id
	 * @param versionId id gotten from {@link #getId()}
	 * @return the client version from this specific.
	 * @throws IllegalArgumentException if version is below 0 or higher than the latest
	 *         version.
	 */
	public static ClientVersions fromId(int versionId) {
		if (versionId < 0 || versionId >= values().length) {
			throw new IllegalArgumentException("Version (" + versionId + ") out of bounds");
		}

		return values()[versionId];
	}

	/**
	 * Check if a client update is needed from the specified version
	 * @param versionId ordinal of the version
	 * @return true if any version from [version] (not including) to the latest version
	 *         needs updating.
	 * @throws IllegalArgumentException if version is below 0 or higher than the latest
	 *         version.
	 * @see #isLatestVersion(int)
	 */
	public static boolean isUpdateNeeded(int versionId) {
		if (versionId < 0 || versionId >= values().length) {
			throw new IllegalArgumentException("Version (" + versionId + ") out of bounds");
		}

		for (int i = versionId + 1; i < values().length; ++i) {
			ClientVersions clientVersion = values()[i];

			if (clientVersion.mUpdatedNeeeded) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if the version is the latest version
	 * @param versionId
	 * @return true if version is the latest version, false otherwise
	 * @throws IllegalArgumentException if version is below 0 or higher than the latest
	 *         version.
	 * @see #isUpdateNeeded(int) if an update is needed from version
	 */
	public static boolean isLatestVersion(int versionId) {
		if (versionId < 0 || versionId >= values().length) {
			throw new IllegalArgumentException("Version (" + versionId + ") out of bounds");
		}

		return versionId == values().length - 1;
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
	 */
	public static String getChangeLogs(ClientVersions version) {
		return getChangeLogs(version.getId());
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

			ClientVersions clientVersion = values()[i];

			changeLogs.append(clientVersion.getChangeLog());
		}

		return changeLogs.toString();
	}

	/**
	 * @return latest version
	 */
	public static ClientVersions getLatest() {
		int lastIndex = ClientVersions.values().length - 1;
		return ClientVersions.values()[lastIndex];
	}

	@Override
	public String toString() {
		return name().replace('_', '.');
	}

	private static final String TAB_REPLACEMENT = "    ";
	private boolean mUpdatedNeeeded;
	private String mChangeLog;
}
