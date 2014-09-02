package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for syncing user resources (with revisions)
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class UserResourcesSyncMethod implements IMethodEntity {
	/** All resource revisions that were uploaded */
	public ArrayList<ResourceRevisionEntity> resources = new ArrayList<>();
	/** Resources to remove */
	public ArrayList<UUID> resourceToRemove = null;
	/** Last sync date */
	public Date lastSync = null;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.USER_RESOURCES_SYNC;
	}
}
