package com.spiddekauga.voider.version;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all the versions
 */
public class VersionContainer {
private List<Version> mVersions = new ArrayList<>();

/**
 * Add another version. These should be added in order of latest -> earliest
 * @param version add another version
 */
void add(Version version) {
	mVersions.add(version);
}

/**
 * Check if the specified version is the latest
 * @param version
 * @return true if the specified version is the latest
 */
public boolean isLatest(Version version) {
	return version.equals(getLatest());
}

/**
 * Get the latest version
 * @return latest version, null if none exists
 */
public Version getLatest() {
	if (!mVersions.isEmpty()) {
		return mVersions.get(0);
	} else {
		return null;
	}
}

/**
 * Get the specified version from a string
 * @param versionString
 * @return the full version represented by versionString, null if not found
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
 * Check if an update is needed from the specified version
 * @param fromVersion
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
 * Get all versions after the specified version
 * @param afterVersion
 * @return all versions after the specified version
 */
public List<Version> getVersionsAfter(Version afterVersion) {
	List<Version> versions = new ArrayList<>();

	for (Version version : mVersions) {
		if (version.isLaterThan(afterVersion)) {
			versions.addAll(versions);
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
