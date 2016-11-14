package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IEntity;

import java.util.Date;

/**
 * Revision information wrapper
 */
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
