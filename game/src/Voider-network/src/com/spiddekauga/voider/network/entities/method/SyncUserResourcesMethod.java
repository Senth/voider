package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.ResourceRevisionEntity;

/**
 * Method for syncing user resources (with revisions)
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncUserResourcesMethod implements IMethodEntity {
	/** All resource revisions that were uploaded */
	public ArrayList<ResourceRevisionEntity> resources = new ArrayList<>();

	@Override
	public String getMethodName() {
		return "sync-user-resources";
	}
}
