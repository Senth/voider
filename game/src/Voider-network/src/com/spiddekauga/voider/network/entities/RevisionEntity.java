package com.spiddekauga.voider.network.entities;

import java.util.Date;

/**
 * Revision information wrapper
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class RevisionEntity implements IEntity, Comparable<RevisionEntity> {
	/** The revision */
	public int revision;
	/** Date the revision was created */
	public Date date = new Date();

	@Override
	public int compareTo(RevisionEntity o) {
		return date.compareTo(o.date);
	}
}
