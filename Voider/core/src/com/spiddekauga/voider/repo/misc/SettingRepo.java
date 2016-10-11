package com.spiddekauga.voider.repo.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_General;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.Repo;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo.SettingDisplayLocalRepo;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo.SettingGeneralLocalRepo;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo.SettingNetworkLocalRepo;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo.SettingSoundLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.repo.user.UserRepo;
import com.spiddekauga.voider.resources.DensityBuckets;
import com.spiddekauga.voider.version.Version;
import com.spiddekauga.voider.version.VersionContainer;

/**
 * Setting repository
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SettingRepo extends Repo {
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
	 * Info settings
	 */
	public class SettingInfoRepo {
		/**
		 * Updates the client version to the latest client version
		 */
		public void updateClientVersion() {
			mLocalRepo.updateLastUsedVersion();
		}

		/**
		 * @return true if this client has been updated since last login from the current
		 *         user
		 */
		public boolean isClientVersionNewSinceLastLogin() {
			return mLocalRepo.getLastUsedVersion() != mLocalRepo.getCurrentVersion();
		}

		/**
		 * @return version container which has information about all versions
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
		 * @return current (latest) client version
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

		private SettingGeneralLocalRepo mLocalRepo = SettingLocalRepo.getInstance().general;
	}

	/**
	 * Date setting repository. Access through SettingRepo,
	 */
	public class SettingDateRepo {
		/**
		 * Hidden constructor
		 */
		private SettingDateRepo() {
			// Does nothing
		}

		/**
		 * Sets the date time format if it has been changed, and updates the server
		 * version.
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
		 * Get first or second part of the date time string
		 * @param datePart true if we want to get the date part, false for the time part
		 * @return specific split part of the date time string
		 */
		private String getFormatPart(boolean datePart) {
			String dateTime = mGeneralLocalRepo.getDateTime();
			if (!dateTime.isEmpty()) {
				int lastSpaceIndex = dateTime.lastIndexOf(' ');

				if (lastSpaceIndex != -1) {
					// Date part
					if (datePart) {
						return dateTime.substring(0, lastSpaceIndex);
					}
					// Time part
					else {
						return dateTime.substring(lastSpaceIndex + 1);
					}
				}
			}

			return "";
		}

		/**
		 * @return time format
		 */
		private String getTimeFormat() {
			return getFormatPart(false);
		}

		/**
		 * Set the date format (not time)
		 * @param format
		 */
		public void setDateFormat(String format) {
			setDateTimeFormat(format + " " + getTimeFormat());
		}

		/**
		 * @return get date format
		 */
		public String getDateFormat() {
			return getFormatPart(true);
		}

		/**
		 * Set if the date should use 24 hours or AM/PM
		 * @param time24h true to use 24hr format, false to use AM/PM
		 */
		public void set24h(boolean time24h) {
			String timeFormat = null;
			IC_General icGeneral = ConfigIni.getInstance().setting.general;
			if (time24h) {
				timeFormat = icGeneral.getTime24hFormat();
			} else {
				timeFormat = icGeneral.getTimeAmPmFormat();
			}
			setDateTimeFormat(getDateFormat() + " " + timeFormat);
		}

		/**
		 * @return true if 24hr are used, false if AM/PM is used
		 */
		public boolean is24h() {
			IC_General icGeneral = ConfigIni.getInstance().setting.general;
			return icGeneral.getTime24hFormat().equals(getTimeFormat());
		}

		/**
		 * Convert a date to human-readable date and time string using the internal date
		 * time format
		 * @param date the date to format
		 * @return human-readable date time string
		 */
		public String getDateTime(Date date) {
			if (mDateTimeFormatter == null) {
				mDateTimeFormatter = new SimpleDateFormat(mGeneralLocalRepo.getDateTime());
			} else if (!mDateTimeFormatter.toPattern().equals(mGeneralLocalRepo.getDateTime())) {
				mDateTimeFormatter.applyPattern(mGeneralLocalRepo.getDateTime());
			}

			return mDateTimeFormatter.format(date);
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

			if (mDateFormatter == null) {
				mDateFormatter = new SimpleDateFormat(getDateFormat());
			} else if (!mDateFormatter.toPattern().equals(getDateFormat())) {
				mDateFormatter.applyPattern(getDateFormat());
			}

			return mDateFormatter.format(date);
		}

		/**
		 * Convert a date to human-readable time string using the internal time format
		 * @param date the date to format
		 * @return human-readable time string
		 */
		public String getTime(Date date) {
			if (mTimeFormatter == null) {
				mTimeFormatter = new SimpleDateFormat(getTimeFormat());
			} else if (!mTimeFormatter.toPattern().equals(getTimeFormat())) {
				mTimeFormatter.applyPattern(getTimeFormat());
			}

			return mTimeFormatter.format(date);
		}

		private SimpleDateFormat mDateTimeFormatter = null;
		private SimpleDateFormat mDateFormatter = null;
		private SimpleDateFormat mTimeFormatter = null;
		private SettingGeneralLocalRepo mGeneralLocalRepo = SettingLocalRepo.getInstance().general;
	}

	/**
	 * Sound settings repository. Access through SettingRepo.
	 */
	public class SettingSoundRepo {
		/**
		 * Hidden constructor
		 */
		private SettingSoundRepo() {
			// Does nothing
		}

		/**
		 * Set the master volume
		 * @param volume in range [0,1]
		 */
		public void setMasterVolume(float volume) {
			mSoundLocalRepo.setMasterVolume(volume);
		}

		/**
		 * @return master volume in range [0,1]
		 */
		public float getMasterVolume() {
			return mSoundLocalRepo.getMasterVolume();
		}

		/**
		 * Set the sound effects volume
		 * @param volume in range [0,1]
		 */
		public void setEffectsVolume(float volume) {
			mSoundLocalRepo.setEffectsVolume(volume);
		}

		/**
		 * @return sound effects volume in range [0,1]
		 */
		public float getEffectsVolume() {
			return mSoundLocalRepo.getEffectsVolume();
		}

		/**
		 * Get the real sound effects out volume.
		 * @return sound effects volume multiplied with the master volume
		 */
		public float getEffectsVolumeOut() {
			return mSoundLocalRepo.getEffectsVolumeOut();
		}

		/**
		 * Set the music volume
		 * @param volume in range [0,1]
		 */
		public void setMusicVolume(float volume) {
			mSoundLocalRepo.setMusicVolume(volume);
		}

		/**
		 * @return music volume in range [0,1]
		 */
		public float getMusicVolume() {
			return mSoundLocalRepo.getMusicVolume();
		}

		/**
		 * Get the real music out volume
		 * @return music volume multiplied with the master volume
		 */
		public float getMusicVolumeOut() {
			return mSoundLocalRepo.getMusicVolumeOut();
		}

		/**
		 * Set the UI effects volume
		 * @param volume in range [0,1]
		 */
		public void setUiVolume(float volume) {
			mSoundLocalRepo.setUiVolume(volume);
		}

		/**
		 * @return UI effects volume in range [0,1]
		 */
		public float getUiVolume() {
			return mSoundLocalRepo.getUiVolume();
		}

		/**
		 * Get the real UI out volume
		 * @return UI volume multiplied with the master volume
		 */
		public float getUiVolumeOut() {
			return mSoundLocalRepo.getUiVolumeOut();
		}

		private SettingSoundLocalRepo mSoundLocalRepo = SettingLocalRepo.getInstance().sound;
	}

	/**
	 * Display settings
	 */
	public class SettingDisplayRepo {
		/**
		 * Sets if game should start in fullscreen mode
		 * @param fullscreen true for fullscreen
		 */
		public void setFullscreen(boolean fullscreen) {
			mLocalRepo.setFullscreen(fullscreen);
		}

		/**
		 * @return true if game should start in fullscreen mode
		 */
		public boolean isFullscreen() {
			return mLocalRepo.isFullscreen();
		}

		/**
		 * Sets the startup resolution of the game (in windowed mode)
		 * @param resolution
		 */
		public void setResolutionWindowed(Resolution resolution) {
			mLocalRepo.setResolutionWindowed(resolution);
		}

		/**
		 * @return startup resolution of the game (in windowed mode)
		 */
		public Resolution getResolutionWindowed() {
			return mLocalRepo.getResolutionWindowed();
		}

		/**
		 * Sets the fullscreen resolution of the game
		 * @param resolution
		 */
		public void setResolutionFullscreen(Resolution resolution) {
			mLocalRepo.setResolutionFullscreen(resolution);
		}

		/**
		 * @return fullscreen resolution of the game
		 */
		public Resolution getResolutionFullscreen() {
			return mLocalRepo.getResolutionFullscreen();
		}

		/**
		 * Toggles fullscreen mode
		 */
		public void toggleFullscreen() {
			mLocalRepo.toggleFullscreen();
		}

		/**
		 * Set icon/UI size
		 * @param iconSize set the icon size
		 */
		public void setIconSize(IconSizes iconSize) {
			mLocalRepo.setIconSize(iconSize);
		}

		/**
		 * @return current iconSize
		 */
		public IconSizes getIconSize() {
			return mLocalRepo.getIconSize();
		}

		private SettingDisplayLocalRepo mLocalRepo = SettingLocalRepo.getInstance().display;
	}

	/**
	 * Network settings
	 */
	public class SettingNetworkRepo {
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
		 * Sets if we're allowed to use mobile data connections
		 * @param allow true if we're allowed to use mobile data
		 */
		public void setMobileDataAllowed(boolean allow) {
			mLocalRepo.setMobileDataAllowed(allow);
		}

		/**
		 * @return true if we are allowed to use mobile data connection
		 */
		public boolean isMobileDataAllowed() {
			return mLocalRepo.isMobileDataAllowed();
		}

		private SettingNetworkLocalRepo mLocalRepo = SettingLocalRepo.getInstance().network;
	}

	/**
	 * Debug settings
	 */
	public class SettingDebugRepo {
		/**
		 * Clears the database, files and settings for the current logged in account and
		 * then logs out the user
		 */
		public void clearData() {
			ResourceLocalRepo.removeAll();
			UserRepo.getInstance().clearUserData();
			User.getGlobalUser().logout();
		}
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
		LARGE(DensityBuckets.XHIGH),

		;

		private IconSizes(DensityBuckets densityBucket) {
			mDensityBucket = densityBucket;
			mName = name().toLowerCase().replace("_", " ");
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

		/**
		 * Get the enum from a name
		 * @param name name returned from {@link #toString()}.
		 * @return enum with the specified name, null if not found
		 */
		public static IconSizes fromName(String name) {
			return mNameToIconSize.get(name);
		}

		private String mName;
		/** Appropriate density bucket to load */
		private DensityBuckets mDensityBucket;

		private static Map<String, IconSizes> mNameToIconSize = new HashMap<>();

		static {
			for (IconSizes iconSize : IconSizes.values()) {
				mNameToIconSize.put(iconSize.mName, iconSize);
			}
		}
	}

	private SettingNetworkRepo mNetwork = new SettingNetworkRepo();
	private SettingInfoRepo mInfo = new SettingInfoRepo();
	private SettingDisplayRepo mDisplay = new SettingDisplayRepo();
	private SettingDateRepo mDate = new SettingDateRepo();
	private SettingSoundRepo mSound = new SettingSoundRepo();
	private SettingDebugRepo mDebug = new SettingDebugRepo();
	private static SettingRepo mInstance = null;
}
