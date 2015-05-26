package com.spiddekauga.voider.network.resource;

import java.util.Date;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Revision information wrapper
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RevisionEntity implements IEntity, Comparable<RevisionEntity> {
	private static final long serialVersionUID = 1L;
	/** The revision */
	public int revision;
	/** Date the revision was created */
	public Date date = new Date();

	@Override
	public int compareTo(RevisionEntity o) {
		return date.compareTo(o.date);
	}
}
