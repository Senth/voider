package com.spiddekauga.voider.utils.event;

import com.spiddekauga.voider.network.misc.NetworkConfig;

/**
 * Contains update information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UpdateEvent extends GameEvent {
	/** Latest client version */
	public final String newestVersion;
	/** The change log message */
	public final String changeLog;
	/** Download URL for the client */
	public final String downloadUrl;

	/**
	 * What kind of update event this is
	 * @param type
	 * @param latestClientVersion
	 * @param changeLog
	 */
	public UpdateEvent(final EventTypes type, final String latestClientVersion, final String changeLog) {
		super(type);

		this.newestVersion = latestClientVersion;

		// Split change log
		String[] splits = changeLog.split(NetworkConfig.SPLIT_TOKEN);

		// ChangeLog
		if (splits.length >= 1) {
			this.changeLog = splits[0];
		} else {
			this.changeLog = null;
		}

		// Download URL
		if (splits.length == 2) {
			downloadUrl = splits[1];
		} else {
			downloadUrl = null;
		}
	}
}
