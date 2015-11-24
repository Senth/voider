package com.spiddekauga.voider.version;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all the versions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class VersionContainer {
	/**
	 * Add another version. These should be added in order of latest -> earliest
	 * @param version add another version
	 */
	void add(Version version) {
		mVersions.add(version);
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

	private List<Version> mVersions = new ArrayList<>();
}
