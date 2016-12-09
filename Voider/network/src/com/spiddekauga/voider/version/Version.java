package com.spiddekauga.voider.version;

import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.network.entities.IEntity;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Contains game version information about the client
 */
public class Version implements IEntity {
private static final int VERSION_PARTS = 3;
private static Logger mLogger = Logger.getLogger(Version.class.getSimpleName());
private String mChanges = null;
private Date mDate = new Date();
private boolean mUpdateRequired = true;
private boolean mHotfix = false;
private String mVersion;
private int[] mVersionParts;

/**
 * Creates a new Version object
 * @param versionString string representation of the version (e.g. "0.6.0")
 */
Version(String versionString) {
	mVersion = versionString;
	mVersionParts = toVersionParts(versionString);
}

/**
 * Convert a string version to integer version parts
 * @param versionString the version string to convert (e.g. "0.6.0")
 * @return array with the appropriate gameVersion parts, null if invalid gameVersion string
 */
private static int[] toVersionParts(String versionString) {
	String[] versions = versionString.split("\\.");
	if (versions.length == VERSION_PARTS) {
		int[] versionParts = new int[VERSION_PARTS];
		try {
			for (int i = 0; i < VERSION_PARTS; ++i) {
				versionParts[i] = Integer.parseInt(versions[i]);
			}
			return versionParts;
		} catch (NumberFormatException e) {
			mLogger.severe("Version string couldn't convert string to number: " + Strings.exceptionToString(e));
		}
	} else {
		mLogger.severe("Version string isn't in the correct format: " + versionString);
	}
	return null;
}

/**
 * Hidden default constructor for kryo
 */
@SuppressWarnings("unused")
private Version() {
	// Does nothing
}

/**
 * @return full gameVersion string
 */
public String getVersion() {
	return mVersion;
}

/**
 * @return gameVersion date
 */
public Date getDate() {
	return mDate;
}

/**
 * Set the date of the gameVersion
 * @param date
 */
public void setDate(Date date) {
	mDate = date;
}

/**
 * @return true if an update is required
 */
boolean isUpdateRequired() {
	return mUpdateRequired;
}

/**
 * Call this to set if update is required. Automatically sets hotfix to false if this is set to
 * true
 * @param updateRequired set as true if an update is required. If set to true hotfix is
 * automatically set to false
 */
void setUpdateRequired(boolean updateRequired) {
	mUpdateRequired = updateRequired;
	if (mUpdateRequired) {
		mHotfix = false;
	}
}

/**
 * Set if this gameVersion is a hotfix (only server changes). Automatically sets update required to
 * false if this is set to true.
 * @param serverHotfix true if this is a server hotfix
 */
void setServerHotfix(boolean serverHotfix) {
	mHotfix = serverHotfix;
	if (mHotfix) {
		mUpdateRequired = false;
	}
}

/**
 * Append a line to the change log
 * @param line a line containing the change log
 */
void addChangeLine(String line) {
	if (mChanges == null) {
		mChanges = line;
	} else {
		mChanges += "\n" + line;
	}
}

/**
 * @return the change log message
 */
public String getChangeLog() {
	return mChanges;
}

/**
 * Set the changes
 * @param changes all lines containing the changes
 */
public void setChangeLog(String changes) {
	mChanges = changes;
}

/**
 * Check if this instance (version) is newer than olderVersion
 * @param olderVersion the older version
 * @return true if this instance (version) is earlier than the specified (older) version. False if
 * olderVersion is newer than this version.
 */
public boolean isNewerThan(Version olderVersion) {
	return olderVersion.isOlderThan(this);
}

/**
 * Check if this instance (version) is older than newerVersion
 * @param newerVersion the newer version
 * @return true if this instance (version) is older than the specified (newer) version. False if
 * newerVersion is older than this version.
 */
public boolean isOlderThan(Version newerVersion) {
	if (equals(newerVersion)) {
		return false;
	}

	for (int i = 0; i < VERSION_PARTS; ++i) {
		if (mVersionParts[i] > newerVersion.mVersionParts[i]) {
			return false;
		} else if (mVersionParts[i] < newerVersion.mVersionParts[i]) {
			return true;
		}
	}

	return false;
}

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((mVersion == null) ? 0 : mVersion.hashCode());
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
	Version other = (Version) obj;
	if (mVersion == null) {
		if (other.mVersion != null) {
			return false;
		}
	} else if (!mVersion.equals(other.mVersion)) {
		return false;
	}
	return true;
}
}
