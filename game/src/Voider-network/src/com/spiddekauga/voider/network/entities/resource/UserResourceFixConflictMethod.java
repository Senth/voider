package com.spiddekauga.voider.network.entities.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for fixing conflicts
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class UserResourceFixConflictMethod implements IMethodEntity {
	/** True if we should sync from client to server */
	public boolean keepClient;
	/** Conflicting resources */
	public HashMap<UUID, ResourceConflictEntity> conflicts;
	/** Last sync date */
	public Date lastSync;


	@Override
	public MethodNames getMethodName() {
		return MethodNames.USER_RESOURCE_FIX_CONFLICT;
	}
}
