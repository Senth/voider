package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.ResourceRevisionEntity;

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
	public String getMethodName() {
		return "user-resources-sync";
	}
}
