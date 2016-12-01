package com.spiddekauga.voider.settings;

import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_General;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.Repo;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.repo.user.UserRepo;
import com.spiddekauga.voider.resources.DensityBuckets;
import com.spiddekauga.voider.settings.SettingLocalRepo.SettingDisplayLocalRepo;
import com.spiddekauga.voider.settings.SettingLocalRepo.SettingGeneralLocalRepo;
import com.spiddekauga.voider.settings.SettingLocalRepo.SettingNetworkLocalRepo;
import com.spiddekauga.voider.settings.SettingLocalRepo.SettingSoundLocalRepo;
import com.spiddekauga.voider.version.Version;
import com.spiddekauga.voider.version.VersionContainer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Setting repository
 */
public class SettingRepo extends Repo {
private static SettingRepo mInstance = null;
private SettingNetworkRepo mNetwork = new SettingNetworkRepo();
private SettingInfoRepo mInfo = new SettingInfoRepo();
private SettingDisplayRepo mDisplay = new SettingDisplayRepo();
private SettingDateRepo mDate = new SettingDateRepo();
private SettingSoundRepo mSound = new SettingSoundRepo();
private SettingDebugRepo mDebug = new SettingDebugRepo();

/**
 * Private constructor to enforce singleton pattern
 */
private SettingRepo() {
	// Does nothing
}

/**
 * @return instance of this class
 */
public static SettingRepo getInstance() {
	if (mInstance == null) {
		mInstance = new SettingRepo();
	}
	return mInstance;
}

@Override
public void handleWebResponse(IMethodEntity method, IEntity response) {
	// Does nothing

}

/**
 * @return sound settings
 */
public SettingSoundRepo sound() {
	return mSound;
}

/**
 * @return date settings
 */
public SettingDateRepo date() {
	return mDate;
}

/**
 * @return display settings
 */
public SettingDisplayRepo display() {
	return mDisplay;
}

/**
 * @return info settings
 */
public SettingInfoRepo info() {
	return mInfo;
}

/**
 * @return network settings
 */
public SettingNetworkRepo network() {
	return mNetwork;
}

/**
 * @return debug settings
 */
public SettingDebugRepo debug() {
	return mDebug;
}

/**
 * Icon/UI sizes
 */
public enum IconSizes {
	/** Small 32x32 icons */
	SMALL(DensityBuckets.MEDIUM),
	/** Medium 48x48 icons */
	MEDIUM(DensityBuckets.HIGH),
	/** Large 64x64 icons */
	LARGE(DensityBuckets.XHIGH),;

	private static Map<String, IconSizes> mNameToIconSize = new HashMap<>();

	static {
		for (IconSizes iconSize : IconSizes.values()) {
			mNameToIconSize.put(iconSize.mName, iconSize);
		}
	}

	private String mName;
	private DensityBuckets mDensityBucket;

	private IconSizes(DensityBuckets densityBucket) {
		mDensityBucket = densityBucket;
		mName = name().toLowerCase().replace("_", " ");
	}

	/**
	 * Get the enum from a name
	 * @param name name returned from {@link #toString()}.
	 * @return enum with the specified name, null if not found
	 */
	public static IconSizes fromName(String name) {
		return mNameToIconSize.get(name);
	}

	@Override
	public String toString() {
		return mName;
	}

	/**
	 * Convert to density bucket
	 * @return density bucket representation
	 */
	public DensityBuckets toDensityBucket() {
		return mDensityBucket;
	}
}

/**
 * Info settings
 */
public static class SettingInfoRepo {
	private SettingGeneralLocalRepo mLocalRepo = SettingLocalRepo.getInstance().general;

	/**
	 * Updates the client gameVersion to the latest client gameVersion
	 */
	public void updateClientVersion() {
		mLocalRepo.updateLastUsedVersion();
	}

	/**
	 * @return true if this client has been updated since last login from the current user
	 */
	public boolean isClientVersionNewSinceLastLogin() {
		return mLocalRepo.getLastUsedVersion() != mLocalRepo.getCurrentVersion();
	}

	/**
	 * @return gameVersion container which has information about all versions
	 */
	public VersionContainer getVersions() {
		return mLocalRepo.getVersions();
	}

	/**
	 * @return all versions since we last logged in
	 */
	public List<Version> getVersionsSinceLastUsed() {
		return mLocalRepo.getVersionsSinceLastUsed();
	}

	/**
	 * @return current (latest) client gameVersion
	 */
	public Version getCurrentVersion() {
		return mLocalRepo.getCurrentVersion();
	}

	/**
	 * Set the latest message of the day date
	 * @param motd message of the day
	 */
	public void setLatestMotdDate(Motd motd) {
		mLocalRepo.setLatestMotdDate(motd);
	}

	/**
	 * Set the latest message of the day date from several MOTD
	 * @param motds all new message of the day
	 */
	public void setLatestMotdDate(Iterable<Motd> motds) {
		mLocalRepo.setLatestMotdDate(motds);
	}

	/**
	 * Filter out MOTDs that have been displayed already
	 * @param motds all MOTDs to parse
	 * @return all MOTDs after they have been filtered
	 */
	public List<Motd> filterMotds(Iterable<Motd> motds) {
		return mLocalRepo.filterMotds(motds);
	}

	/**
	 * Check if there are new terms since last startup.
	 * @return true if new terms exist
	 */
	public boolean isTermsNew() {
		return mLocalRepo.isTermsNew();
	}

	/**
	 * Accept terms
	 */
	public void acceptTerms() {
		mLocalRepo.acceptTerms();
	}
}

/**
 * Date setting repository. Access through SettingRepo,
 */
public static class SettingDateRepo {
	private SimpleDateFormat mDateTimeFormatter = null;
	private SimpleDateFormat mDateFormatter = null;
	private SimpleDateFormat mTimeFormatter = null;
	private SettingGeneralLocalRepo mGeneralLocalRepo = SettingLocalRepo.getInstance().general;

	/**
	 * Hidden constructor
	 */
	private SettingDateRepo() {
		// Does nothing
	}

	/**
	 * @return true if 24hr are used, false if AM/PM is used
	 */
	public boolean is24h() {
		IC_General icGeneral = ConfigIni.getInstance().setting.general;
		return icGeneral.getTime24hFormat().equals(getFormat(FormatTypes.TIME));
	}

	/**
	 * @return get the formatting to convert a date into a string
	 */
	String getFormat(FormatTypes formatType) {
		String format = "";
		switch (formatType) {
		case DATE:
			String dateTime = mGeneralLocalRepo.getDateTime();
			if (!dateTime.isEmpty()) {
				int firstSpaceIndex = dateTime.indexOf(' ');
				if (firstSpaceIndex != -1) {
					return dateTime.substring(0, firstSpaceIndex);
				}
			}
			break;
		case TIME:
			dateTime = mGeneralLocalRepo.getDateTime();
			if (!dateTime.isEmpty()) {
				int firstSpaceIndex = dateTime.indexOf(' ');
				if (firstSpaceIndex != -1) {
					return dateTime.substring(firstSpaceIndex + 1);
				}
			}
			break;
		}

		return format;
	}

	/**
	 * Set if the date should use 24 hours or AM/PM
	 * @param time24h true to use 24hr format, false to use AM/PM
	 */
	public void set24h(boolean time24h) {
		String timeFormat;
		IC_General icGeneral = ConfigIni.getInstance().setting.general;
		if (time24h) {
			timeFormat = icGeneral.getTime24hFormat();
		} else {
			timeFormat = icGeneral.getTimeAmPmFormat();
		}
		setDateTimeFormat(getFormat(FormatTypes.DATE) + " " + timeFormat);
	}

	/**
	 * Sets the date time format if it has been changed, and updates the server gameVersion.
	 * @param format new date time format.
	 */
	private void setDateTimeFormat(String format) {
		// Skip, not changed
		if (mGeneralLocalRepo.getDateTime().equals(format)) {
			return;
		}

		mGeneralLocalRepo.setDateTime(format);
	}

	/**
	 * Set the date format (not time)
	 */
	public void setDateFormat(String format) {
		setDateTimeFormat(format + " " + getFormat(FormatTypes.TIME));
	}

	/**
	 * Convert a date to human-readable date string using the internal date format
	 * @param date the date to format
	 * @return human-readable date string
	 */
	public String getDate(Date date) {
		if (date == null) {
			return "";
		}

		// Create or update formatter
		String dateFormat = getFormat(FormatTypes.DATE);
		if (mDateFormatter == null) {
			mDateFormatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
		} else if (!mDateFormatter.toPattern().equals(dateFormat)) {
			mDateFormatter.applyPattern(dateFormat);
		}

		return mDateFormatter.format(date);
	}

	/**
	 * Convert a date to human-readable date and time (without seconds) string using the internal
	 * date time format
	 * @param date the date to format
	 * @return human-readable date time string
	 */
	public String getDateTime(Date date) {
		if (date == null) {
			return "";
		}

		// Create or update formatter
		if (mDateTimeFormatter == null) {
			mDateTimeFormatter = new SimpleDateFormat(mGeneralLocalRepo.getDateTime(), Locale.ENGLISH);
		} else if (!mDateTimeFormatter.toPattern().equals(mGeneralLocalRepo.getDateTime())) {
			mDateTimeFormatter.applyPattern(mGeneralLocalRepo.getDateTime());
		}

		return mDateTimeFormatter.format(date);
	}

	/**
	 * Convert a date to human-readable time string using the internal time format (without
	 * seconds)
	 * @param date the date to format
	 * @return human-readable time string
	 * @see #getTimeWithSeconds(Date) to include the seconds
	 */
	public String getTime(Date date) {
		if (date == null) {
			return "";
		}

		// Create or update formatter
		String timeFormat = getFormat(FormatTypes.TIME);
		if (mTimeFormatter == null) {
			mTimeFormatter = new SimpleDateFormat(timeFormat, Locale.ENGLISH);
		} else if (!mTimeFormatter.toPattern().equals(timeFormat)) {
			mTimeFormatter.applyPattern(timeFormat);
		}

		return mTimeFormatter.format(date);
	}

	enum FormatTypes {
		DATE,
		TIME,
	}
}

/**
 * Sound settings repository. Access through SettingRepo.
 */
public static class SettingSoundRepo {
	private SettingSoundLocalRepo mSoundLocalRepo = SettingLocalRepo.getInstance().sound;

	/**
	 * Hidden constructor
	 */
	private SettingSoundRepo() {
		// Does nothing
	}

	/**
	 * @return master volume in range [0,1]
	 */
	public float getMasterVolume() {
		return mSoundLocalRepo.getMasterVolume();
	}

	/**
	 * Set the master volume
	 * @param volume in range [0,1]
	 */
	public void setMasterVolume(float volume) {
		mSoundLocalRepo.setMasterVolume(volume);
	}

	/**
	 * @return sound effects volume in range [0,1]
	 */
	public float getEffectsVolume() {
		return mSoundLocalRepo.getEffectsVolume();
	}

	/**
	 * Set the sound effects volume
	 * @param volume in range [0,1]
	 */
	public void setEffectsVolume(float volume) {
		mSoundLocalRepo.setEffectsVolume(volume);
	}

	/**
	 * Get the real sound effects out volume.
	 * @return sound effects volume multiplied with the master volume
	 */
	public float getEffectsVolumeOut() {
		return mSoundLocalRepo.getEffectsVolumeOut();
	}

	/**
	 * @return music volume in range [0,1]
	 */
	public float getMusicVolume() {
		return mSoundLocalRepo.getMusicVolume();
	}

	/**
	 * Set the music volume
	 * @param volume in range [0,1]
	 */
	public void setMusicVolume(float volume) {
		mSoundLocalRepo.setMusicVolume(volume);
	}

	/**
	 * Get the real music out volume
	 * @return music volume multiplied with the master volume
	 */
	public float getMusicVolumeOut() {
		return mSoundLocalRepo.getMusicVolumeOut();
	}

	/**
	 * @return UI effects volume in range [0,1]
	 */
	public float getUiVolume() {
		return mSoundLocalRepo.getUiVolume();
	}

	/**
	 * Set the UI effects volume
	 * @param volume in range [0,1]
	 */
	public void setUiVolume(float volume) {
		mSoundLocalRepo.setUiVolume(volume);
	}

	/**
	 * Get the real UI out volume
	 * @return UI volume multiplied with the master volume
	 */
	public float getUiVolumeOut() {
		return mSoundLocalRepo.getUiVolumeOut();
	}
}

/**
 * Display settings
 */
public static class SettingDisplayRepo {
	private SettingDisplayLocalRepo mLocalRepo = SettingLocalRepo.getInstance().display;

	/**
	 * @return true if game should start in fullscreen mode
	 */
	public boolean isFullscreen() {
		return mLocalRepo.isFullscreen();
	}

	/**
	 * Sets if game should start in fullscreen mode
	 * @param fullscreen true for fullscreen
	 */
	public void setFullscreen(boolean fullscreen) {
		mLocalRepo.setFullscreen(fullscreen);
	}

	/**
	 * @return startup resolution of the game (in windowed mode)
	 */
	public Resolution getResolutionWindowed() {
		return mLocalRepo.getResolutionWindowed();
	}

	/**
	 * Sets the startup resolution of the game (in windowed mode)
	 */
	public void setResolutionWindowed(Resolution resolution) {
		mLocalRepo.setResolutionWindowed(resolution);
	}

	/**
	 * @return fullscreen resolution of the game
	 */
	public Resolution getResolutionFullscreen() {
		return mLocalRepo.getResolutionFullscreen();
	}

	/**
	 * Sets the fullscreen resolution of the game
	 */
	public void setResolutionFullscreen(Resolution resolution) {
		mLocalRepo.setResolutionFullscreen(resolution);
	}

	/**
	 * Toggles fullscreen mode
	 */
	public void toggleFullscreen() {
		mLocalRepo.toggleFullscreen();
	}

	/**
	 * @return current iconSize
	 */
	public IconSizes getIconSize() {
		return mLocalRepo.getIconSize();
	}

	/**
	 * Set icon/UI size
	 * @param iconSize set the icon size
	 */
	public void setIconSize(IconSizes iconSize) {
		mLocalRepo.setIconSize(iconSize);
	}
}

/**
 * Network settings
 */
public static class SettingNetworkRepo {
	private SettingNetworkLocalRepo mLocalRepo = SettingLocalRepo.getInstance().network;

	/**
	 * @return true if bug reports should be sent anonymously by default
	 */
	public boolean isBugReportSentAnonymously() {
		return mLocalRepo.isBugReportSentAnonymously();
	}

	/**
	 * Set if bug reports should be sent anonymously by default
	 * @param anonymously true if they should be sent anonymously
	 */
	public void setBugReportSendAnonymously(boolean anonymously) {
		mLocalRepo.setBugReportSendAnonymously(anonymously);
	}

	/**
	 * @return true if we are allowed to use mobile data connection
	 */
	public boolean isMobileDataAllowed() {
		return mLocalRepo.isMobileDataAllowed();
	}

	/**
	 * Sets if we're allowed to use mobile data connections
	 * @param allow true if we're allowed to use mobile data
	 */
	public void setMobileDataAllowed(boolean allow) {
		mLocalRepo.setMobileDataAllowed(allow);
	}
}

/**
 * Debug settings
 */
public static class SettingDebugRepo {
	/**
	 * Clears the database, files and settings for the current logged in account and then logs out
	 * the user
	 */
	public void clearData() {
		ResourceLocalRepo.removeAll();
		UserRepo.getInstance().clearUserData();
		User.getGlobalUser().logout();
	}
}
}