package com.spiddekauga.voider.utils.event;

import java.util.Date;

/**
 * Fired when the server has rewound its database
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ServerRestoreEvent extends GameEvent {
	/** Which date the server restored from */
	public final Date from;
	/** Which date the server restored to */
	public final Date to;

	/**
	 * Creates a new event
	 * @param from which date the server restored from
	 * @param to which date the server restored to
	 */
	public ServerRestoreEvent(Date from, Date to) {
		super(EventTypes.SERVER_RESTORE);

		this.from = from;
		this.to = to;
	}

}
