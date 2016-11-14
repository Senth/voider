package com.spiddekauga.voider.utils.event;

import com.spiddekauga.voider.version.Version;

import java.util.List;

/**
 * Contains update information
 */
public class UpdateEvent extends GameEvent {
/** Latest client version */
public final Version newestVersion;
/** All versions */
public final List<Version> newVersions;
/** Download URL for the client */
public final String downloadUrl;

/**
 * What kind of update event this is
 * @param type
 * @param newVersions all new versions
 * @param downloadUrl where we can download the url
 */
public UpdateEvent(EventTypes type, List<Version> newVersions, String downloadUrl) {
	super(type);

	this.newestVersion = newVersions.get(0);
	this.newVersions = newVersions;
	this.downloadUrl = downloadUrl;
}
}
