package com.spiddekauga.voider.version;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all the versions
 */
public class VersionContainer {
private List<Version> mVersions = new ArrayList<>();

/**
 * Add another gameVersion. These should be added in order of latest -> earliest
 * @param version add another gameVersion
 */
void add(Version version) {
	mVersions.add(version);
}

/**
 * Check if the specified game version is the latest
 * @param version check if this version is the latest game version
 * @return true if the specified game version is the latest
 */
public boolean isLatest(Version version) {
	return version.equals(getLatest());
}

/**
 * Get the latest gameVersion
 * @return latest gameVersion, null if none exists
 */
public Version getLatest() {
	if (!mVersions.isEmpty()) {
		return mVersions.get(0);
	} else {
		return null;
	}
}

/**
 * Get the specified game version from a string
 * @param versionString game version string (usually "0.6.0")
 * @return the full game version represented by versionString, null if not found
 */
public Version getVersion(String versionString) {
	Version findVersion = new Version(versionString);

	for (Version version : mVersions) {
		if (version.equals(findVersion)) {
			return version;
		}
	}

	return null;
}

/**
 * Check if an update is needed from the specified gameVersion
 * @param fromVersion check if there exists newer versions of the game than this, and that they are
 * required to go online
 * @return true if at least one of the later versions needs an update to go online
 */
public boolean isUpdateRequired(Version fromVersion) {
	for (Version version : mVersions) {
		if (version.equals(fromVersion)) {
			return false;
		} else if (version.isUpdateRequired()) {
			return true;
		}
	}

	return false;
}

/**
 * Get all versions after the specified game version
 * @param afterVersion get all newer game version after this version
 * @return all versions after the specified game version
 */
public List<Version> getVersionsAfter(Version afterVersion) {
	List<Version> versions = new ArrayList<>();

	for (Version version : mVersions) {
		if (version.isNewerThan(afterVersion)) {
			versions.add(version);
		} else {
			break;
		}
	}

	return versions;
}

/**
 * Get all versions
 * @return all versions
 */
public List<Version> getAll() {
	return mVersions;
}
}
