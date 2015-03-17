package com.spiddekauga.voider.utils.event;

import java.util.ArrayList;

import com.spiddekauga.voider.network.misc.Motd;

/**
 * Message of the day event
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MotdEvent extends GameEvent {

	/**
	 * Creates a new message of the day event
	 * @param type valid MOTD types
	 * @param motds all message of the days
	 */
	public MotdEvent(EventTypes type, ArrayList<Motd> motds) {
		super(type);
		this.motds = motds;
	}

	/** All current or new message of the day */
	public final ArrayList<Motd> motds;
}
