package com.spiddekauga.voider.server.util;

import com.google.appengine.api.utils.SystemProperty;

/**
 * All different servers builds
 */
public enum Builds {
	/** Development server */
	DEV("voider-dev", "https://voider-dev.appspot.com/", null, "Voider-beta"),
	/** Nightly server */
	NIGHTLY("voider-nightly", "https://voider-nightly.appspot.com/", null, null),
	/** Beta server */
	BETA("voider-beta", "https://voider-beta.appspot.com/", null, "Voider-beta"),
	/** Release server */
	RELEASE("voider-thegame", "http://voider-game.com/", "https://voider-thegame.appspot.com/", null),;

private static final String DOWNLOAD_URL_PREFIX = "https://storage.googleapis.com/voider-shared/app/";
private static final String DESKTOP_SUFFIX = ".jar";
private String mAppId;
private String mUrl;
private String mAppspotUrl;
private String mDownloadName;

/**
 * @param appId application id of the buildType
 * @param url URL for this app
 * @param appspotUrl appspot internal URL used for this app, set to null if same as url
 * @param downloadName for downloading stuff, null to not use
 */
private Builds(String appId, String url, String appspotUrl, String downloadName) {
	mAppId = appId;
	mUrl = url;
	mDownloadName = downloadName;

	if (appspotUrl == null) {
		mAppspotUrl = url;
	} else {
		mAppspotUrl = appspotUrl;
	}
}

/**
 * @return get the current buildType, null if none was found
 */
public static Builds getCurrent() {
	for (Builds build : Builds.values()) {
		if (build.isCurrent()) {
			return build;
		}
	}
	return null;
}

/**
 * @return true if this is the current server
 */
public boolean isCurrent() {
	return SystemProperty.applicationId.get().equals(mAppId);
}

/**
 * @return download URL for desktop client
 */
public String getDownloadDesktopUrl() {
	if (mDownloadName != null) {
		return DOWNLOAD_URL_PREFIX + mDownloadName + DESKTOP_SUFFIX;
	} else {
		return null;
	}
}

/**
 * @return URL for this app
 */
public String getUrl() {
	return mUrl;
}

/**
 * @return Appspot internal URL for this app
 */
public String getAppspotUrl() {
	return mAppspotUrl;
}
}
