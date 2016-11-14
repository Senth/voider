package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IEntity;

import java.util.UUID;

/**
 * Wrapper for conflicting resources
 */
public class ResourceConflictEntity implements IEntity {
/** Resource id */
public UUID resourceId = null;
/** From what revision the conflict began */
public int fromRevision;


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
