package com.spiddekauga.voider.repo.misc;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.misc.SettingRepo.IconSizes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.version.Version;
import com.spiddekauga.voider.version.VersionContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Local repository for settings (both client settings and user settings)
 */
class SettingLocalRepo {
private static SettingLocalRepo mInstance = null;
/** Network setting repository */
SettingNetworkLocalRepo network = new SettingNetworkLocalRepo();
/** Display setting repository */
SettingDisplayLocalRepo display = new SettingDisplayLocalRepo();
/** Sound setting repository */
SettingSoundLocalRepo sound = new SettingSoundLocalRepo();
/** general setting repository */
SettingGeneralLocalRepo general = new SettingGeneralLocalRepo();
private EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
private SettingUserPrefsGateway mUserPrefsGateway = new SettingUserPrefsGateway();
private SettingClientPrefsGateway mClientPrefsGateway = new SettingClientPrefsGateway();
/**
 * Private constructor to enforce singleton usage
 */
private SettingLocalRepo() {
	// Does nothing
}

/**
 * @return instance of this class
 */
public static SettingLocalRepo getInstance() {
	if (mInstance == null) {
		mInstance = new SettingLocalRepo();
	}
	return mInstance;
}

/**
 * General Settings
 */
class SettingGeneralLocalRepo {
	private VersionContainer mVersionContainer = null;

	/**
	 * Hidden constructor
	 */
	private SettingGeneralLocalRepo() {
		ResourceCacheFacade.load(null, InternalNames.TXT_CHANGELOG);
		ResourceCacheFacade.finishLoading();
		mVersionContainer = ResourceCacheFacade.get(InternalNames.TXT_CHANGELOG);
	}

	/**
	 * @return DateTime format the user uses.
	 */
	String getDateTime() {
		return mUserPrefsGateway.getDateTime();
	}

	/**
	 * Sets DateTime format that the user uses
	 * @param dateTime format of the date time
	 */
	void setDateTime(String dateTime) {
		mUserPrefsGateway.setDateTime(dateTime);
	}

	/**
	 * Updates the client version to the latest client version
	 */
	void updateLastUsedVersion() {
		mUserPrefsGateway.updateLastUsedVersion(mVersionContainer.getLatest().getVersion());
	}

	/**
	 * @return version container which has information about all versions
	 */
	VersionContainer getVersions() {
		return mVersionContainer;
	}

	/**
	 * @return all versions since we last logged in
	 */
	List<Version> getVersionsSinceLastUsed() {
		return mVersionContainer.getVersionsAfter(getLastUsedVersion());
	}

	/**
	 * @return the last client version this client used
	 */
	Version getLastUsedVersion() {
		String versionString = mUserPrefsGateway.getLastUsedVersion();
		Version version = null;

		if (versionString != null) {
			version = mVersionContainer.getVersion(versionString);
		}

		if (version == null) {
			version = mVersionContainer.getLatest();
		}

		return version;
	}

	/**
	 * @return current client version
	 */
	Version getCurrentVersion() {
		return mVersionContainer.getLatest();
	}

	/**
	 * Set the latest message of the day date from several MOTD
	 * @param motds all new message of the day
	 */
	void setLatestMotdDate(Iterable<Motd> motds) {
		Motd latestMotd = null;
		Date latestDate = new Date(0);

		for (Motd motd : motds) {
			if (motd.created.after(latestDate)) {
				latestDate = motd.created;
				latestMotd = motd;
			}
		}

		if (latestMotd != null) {
			setLatestMotdDate(latestMotd);
		}
	}

	/**
	 * Set the latest message of the day date
	 * @param motd message of the day
	 */
	void setLatestMotdDate(Motd motd) {
		Date previousDate = mUserPrefsGateway.getLatestMotdDate();
		if (motd.created.after(previousDate)) {
			mUserPrefsGateway.setLatestMotdDate(motd.created);
		}
	}

	/**
	 * Filter out MOTDs that have been displayed already
	 * @param motds all MOTDs to parse
	 * @return list of all filtered MOTDs
	 */
	List<Motd> filterMotds(Iterable<Motd> motds) {
		List<Motd> filteredMotds = new ArrayList<>();
		Iterator<Motd> it = motds.iterator();
		Date previousDate = mUserPrefsGateway.getLatestMotdDate();

		while (it.hasNext()) {
			Motd motd = it.next();

			if (motd.created.after(previousDate)) {
				filteredMotds.add(motd);
			}
		}

		return filteredMotds;
	}

	/**
	 * Check if there are new terms since last startup.
	 * @return true if new terms exist
	 */
	boolean isTermsNew() {
		// Check that terms are loaded...
		if (ResourceCacheFacade.isLoaded(InternalNames.TXT_TERMS)) {
			String terms = ResourceCacheFacade.get(InternalNames.TXT_TERMS);

			long termsLengthPrev = mUserPrefsGateway.getTermsLength();
			if (termsLengthPrev != 0) {
				if (terms.length() != termsLengthPrev) {
					return true;
				}
			} else {
				mUserPrefsGateway.setTermsLength(terms.length());
			}

		} else {
			Config.Debug.debugException("Terms not loaded when calling isTermsNew()");
		}
		return false;
	}

	/**
	 * Accept terms
	 */
	void acceptTerms() {
		// Check that terms are loaded...
		if (ResourceCacheFacade.isLoaded(InternalNames.TXT_TERMS)) {
			String terms = ResourceCacheFacade.get(InternalNames.TXT_TERMS);
			mUserPrefsGateway.setTermsLength(terms.length());
		} else {
			Config.Debug.debugException("Terms not loaded when calling acceptTerms()");
		}
	}
}

/**
 * Sound settings
 */
class SettingSoundLocalRepo {
	/**
	 * Hidden constructor
	 */
	private SettingSoundLocalRepo() {
		// Does nothing
	}

	/**
	 * Get the real sound effects out volume.
	 * @return sound effects volume multiplied with the master volume
	 */
	float getEffectsVolumeOut() {
		return getEffectsVolume() * getMasterVolume();
	}

	/**
	 * @return sound effects volume in range [0,1]
	 */
	float getEffectsVolume() {
		return mClientPrefsGateway.getEffectsVolume();
	}

	/**
	 * @return master volume in range [0,1]
	 */
	float getMasterVolume() {
		return mClientPrefsGateway.getMasterVolume();
	}

	/**
	 * Set the master volume
	 * @param volume in range [0,1]
	 */
	void setMasterVolume(float volume) {
		mClientPrefsGateway.setMasterVolume(volume);
		mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_MASTER_VOLUME_CHANGED));
	}

	/**
	 * Set the sound effects volume
	 * @param volume in range [0,1]
	 */
	void setEffectsVolume(float volume) {
		mClientPrefsGateway.setEffectsVolume(volume);
		mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_EFFECTS_VOLUME_CHANGED));
	}

	/**
	 * Get the real music out volume
	 * @return music volume multiplied with the master volume
	 */
	float getMusicVolumeOut() {
		return getMusicVolume() * getMasterVolume();
	}

	/**
	 * @return music volume in range [0,1]
	 */
	float getMusicVolume() {
		return mClientPrefsGateway.getMusicVolume();
	}

	/**
	 * Set the music volume
	 * @param volume in range [0,1]
	 */
	void setMusicVolume(float volume) {
		mClientPrefsGateway.setMusicVolume(volume);
		mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_MUSIC_VOLUME_CHANGED));
	}

	/**
	 * Get the real UI out volume
	 * @return UI volume multiplied with the master volume
	 */
	float getUiVolumeOut() {
		return getUiVolume() * getMasterVolume();
	}

	/**
	 * @return UI effects volume in range [0,1]
	 */
	float getUiVolume() {
		return mClientPrefsGateway.getUiVolume();
	}

	/**
	 * Set the UI effects volume
	 * @param volume in range [0,1]
	 */
	void setUiVolume(float volume) {
		mClientPrefsGateway.setUiVolume(volume);
		mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_UI_VOLUME_CHANGED));
	}
}

/**
 * Display settings
 */
class SettingDisplayLocalRepo {
	/**
	 * Toggles fullscreen mode
	 */
	void toggleFullscreen() {
		mClientPrefsGateway.setFullscreen(!mClientPrefsGateway.isFullscreen());
		updateScreenSize();
	}	/**
	 * Sets if game should start in fullscreen mode
	 * @param fullscreen true for fullscreen
	 */
	void setFullscreen(boolean fullscreen) {
		mClientPrefsGateway.setFullscreen(fullscreen);
		updateScreenSize();
	}

	/**
	 * @return true if game should start in fullscreen mode
	 */
	boolean isFullscreen() {
		return mClientPrefsGateway.isFullscreen();
	}

	/**
	 * Sets the startup resolution of the game
	 * @param resolution
	 */
	void setResolutionWindowed(Resolution resolution) {
		mClientPrefsGateway.setResolutionWindowed(resolution);
		updateScreenSize();
	}

	/**
	 * @return startup resolution of the game
	 */
	Resolution getResolutionWindowed() {
		return mClientPrefsGateway.getResolutionWindowed();
	}

	/**
	 * Sets the fullscreen resolution of the game
	 * @param resolution
	 */
	void setResolutionFullscreen(Resolution resolution) {
		mClientPrefsGateway.setResolutionFullscreen(resolution);
		updateScreenSize();
	}

	/**
	 * @return fullscreen resolution of the game
	 */
	Resolution getResolutionFullscreen() {
		return mClientPrefsGateway.getResolutionFullscreen();
	}



	/**
	 * Set icon/UI size
	 * @param iconSize set the icon size
	 */
	void setIconSize(IconSizes iconSize) {
		if (iconSize != getIconSize()) {
			InternalNames[] oldSizes = InternalDeps.getDependencies(InternalDeps.UI_ALL);

			mClientPrefsGateway.setIconSize(iconSize);

			InternalNames[] newSizes = InternalDeps.getDependencies(InternalDeps.UI_ALL);

			ResourceCacheFacade.replace(oldSizes, newSizes);
			ResourceCacheFacade.finishLoading();

			SceneSwitcher.reloadUi();
		}
	}

	/**
	 * @return current iconSize
	 */
	IconSizes getIconSize() {
		return mClientPrefsGateway.getIconSize();
	}

	/**
	 * Update screen size
	 */
	private void updateScreenSize() {
		// Fullscreen
		if (isFullscreen()) {
			Resolution resolution = getResolutionFullscreen();
			Gdx.graphics.setDisplayMode(resolution.getWidth(), resolution.getHeight(), true);
		}
		// Windowed
		else {
			Resolution resolution = getResolutionWindowed();
			Gdx.graphics.setDisplayMode(resolution.getWidth(), resolution.getHeight(), false);
		}
	}
}

/**
 * Network settings
 */
class SettingNetworkLocalRepo {
	/**
	 * @return true if bug reports should be sent anonymously by default
	 */
	boolean isBugReportSentAnonymously() {
		return mUserPrefsGateway.isBugReportSentAnonymously();
	}

	/**
	 * Set if bug reports should be sent anonymously by default
	 * @param anonymously true if they should be sent anonymously
	 */
	void setBugReportSendAnonymously(boolean anonymously) {
		mUserPrefsGateway.setBugReportSendAnonymously(anonymously);
	}

	/**
	 * @return true if we are allowed to use mobile data connection
	 */
	boolean isMobileDataAllowed() {
		return mClientPrefsGateway.isNetworkWifiOnly();
	}

	/**
	 * Sets if we're allowed to use mobile data connections
	 * @param allow true if we're allowed to use mobile data
	 */
	void setMobileDataAllowed(boolean allow) {
		mClientPrefsGateway.setNetworkWifiOnly(allow);
	}
}
}
