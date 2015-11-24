package com.spiddekauga.voider.version;

import java.util.Date;
import java.util.logging.Logger;

import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Contains version information about the client
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Version implements IEntity {
	/**
	 * Creates a new version object
	 * @param versionString
	 */
	public Version(String versionString) {
		mVersion = versionString;
		mVersionParts = toVersionParts(versionString);
	}

	/**
	 * Hidden default constructor for kryo
	 */
	@SuppressWarnings("unused")
	private Version() {
		// Does nothing
	}

	/**
	 * @return full version string
	 */
	public String getVersion() {
		return mVersion;
	}

	/**
	 * Set the date of the version
	 * @param date
	 */
	public void setDate(Date date) {
		mDate = date;
	}

	/**
	 * @return version date
	 */
	public Date getDate() {
		return mDate;
	}

	/**
	 * Call this to set if update is required. Automatically sets hotfix to false if this
	 * is set to true
	 * @param updateRequired
	 */
	public void setUpdateRequired(boolean updateRequired) {
		mUpdateRequired = updateRequired;
		if (mUpdateRequired) {
			mHotfix = false;
		}
	}

	/**
	 * @return true if an update is required
	 */
	public boolean isUpdateRequired() {
		return mUpdateRequired;
	}

	/**
	 * Set if this version is a hotfix (only server changes). Automatically sets update
	 * required to false if this is set to true.
	 * @param serverHotfix true if this is a server hotfix
	 */
	public void setServerHotfix(boolean serverHotfix) {
		mHotfix = serverHotfix;
		if (mHotfix) {
			mUpdateRequired = false;
		}
	}

	/**
	 * @return true if this is a server hotfix
	 */
	public boolean isServerHotfix() {
		return mHotfix;
	}

	/**
	 * Set the changes
	 * @param changes all lines containing the changes
	 */
	public void setChangeLog(String changes) {
		mChanges = changes;
	}

	/**
	 * Append a line to the change log
	 * @param line a line containing the change log
	 */
	public void addChangeLine(String line) {
		if (mChanges != null) {
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
	 * Convert a string version to version parts
	 * @param versionString
	 * @return array with the appropriate version parts, null if invalid version string
	 */
	static int[] toVersionParts(String versionString) {
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
	 * Check if this version is earlier than another version
	 * @param otherVersion
	 * @return true if this version is earlier than the specified version
	 */
	public boolean isEarlierThan(Version otherVersion) {
		for (int i = 0; i < VERSION_PARTS; ++i) {
			if (mVersionParts[i] > otherVersion.mVersionParts[i]) {
				return false;
			} else if (mVersionParts[i] < otherVersion.mVersionParts[i]) {
				return true;
			}
		}
		// Is equal
		return false;
	}

	/**
	 * Check if this version is earlier than another version
	 * @param otherVersion
	 * @return true if this version is earlier than the specified version
	 */
	public boolean isLaterThan(Version otherVersion) {
		for (int i = 0; i < VERSION_PARTS; ++i) {
			if (mVersionParts[i] < otherVersion.mVersionParts[i]) {
				return false;
			} else if (mVersionParts[i] > otherVersion.mVersionParts[i]) {
				return true;
			}
		}
		// Is equal
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

	private String mChanges = null;
	private Date mDate = new Date();
	private boolean mUpdateRequired = true;
	private boolean mHotfix = false;
	private String mVersion;

	private int[] mVersionParts;

	private static final int VERSION_PARTS = 3;
	private static Logger mLogger = Logger.getLogger(Version.class.getSimpleName());
}
