package com.spiddekauga.voider.utils.event;

/**
 * Contains update information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UpdateEvent extends GameEvent {
	/** Latest client version */
	public final String latestClientVersion;
	/** The change log message */
	public final String changeLog;

	/**
	 * What kind of update event this is
	 * @param type
	 * @param latestClientVersion
	 * @param changeLog
	 */
	public UpdateEvent(EventTypes type, String latestClientVersion, String changeLog) {
		super(type);

		this.latestClientVersion = latestClientVersion;
		this.changeLog = changeLog;
	}
}
