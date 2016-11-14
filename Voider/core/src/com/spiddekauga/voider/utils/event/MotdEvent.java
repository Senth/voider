package com.spiddekauga.voider.utils.event;

import com.spiddekauga.voider.network.misc.Motd;

import java.util.ArrayList;

/**
 * Message of the day event
 */
public class MotdEvent extends GameEvent {

/** All current or new message of the day */
public final ArrayList<Motd> motds;

/**
 * Creates a new message of the day event
 * @param type valid MOTD types
 * @param motds all message of the days
 */
public MotdEvent(EventTypes type, ArrayList<Motd> motds) {
	super(type);
	this.motds = motds;
}
}
