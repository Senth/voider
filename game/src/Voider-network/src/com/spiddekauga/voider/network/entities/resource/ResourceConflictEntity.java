package com.spiddekauga.voider.network.entities.resource;

import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Wrapper for conflicting resources
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceConflictEntity implements IEntity {
	/** Resource id */
	public UUID resourceId = null;
	/** From what revision the conflict began */
	public int fromRevision;
	/** From what date the conflict began */
	public Date fromDate;
	/** Latest revision date on server */
	public Date latestDate;

	@Override
	public boolean equals(Object o) {
		if (resourceId == null) {
			return false;
		}

		if (o instanceof UUID) {
			return resourceId.equals(o);
		} else if (o instanceof ResourceRevisionEntity) {
			return resourceId.equals(((ResourceRevisionEntity) o).resourceId);
		} else if (o instanceof ResourceConflictEntity) {
			return resourceId.equals(((ResourceConflictEntity) o).resourceId);
		}

		return false;
	}
}
