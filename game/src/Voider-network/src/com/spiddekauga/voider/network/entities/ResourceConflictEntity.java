package com.spiddekauga.voider.network.entities;

import java.util.Date;
import java.util.UUID;

/**
 * Wrapper for conflicting resources
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceConflictEntity implements IEntity {
	/** Resource id */
	public UUID resourceId;
	/** From what revision the conflict began */
	public int fromRevision;
	/** From what date the conflict began */
	public Date fromDate;
	/** Latest revision date on server */
	public Date latestDate;
}
