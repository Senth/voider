package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for syncing user resources (with revisions)
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserResourceSyncMethod implements IMethodEntity {
	private static final long serialVersionUID = 1L;
	/** All resource revisions that were uploaded */
	public ArrayList<ResourceRevisionEntity> resources = new ArrayList<>();
	/** Resources to remove */
	public ArrayList<UUID> resourceToRemove = null;
	/** Last sync date */
	public Date lastSync = null;
	/** Fix conflicts. True keep local, false keep server, null do nothing */
	public Boolean conflictKeepLocal = null;
	/** Conflicts to fix */
	public HashMap<UUID, ResourceConflictEntity> conflictsToFix = null;

	/**
	 * @return true if local conflicts should be kept
	 */
	public boolean keepLocalConflicts() {
		return conflictKeepLocal != null && conflictKeepLocal;
	}

	/**
	 * @return true if server conflicts should be kept
	 */
	public boolean keepServerConflicts() {
		return conflictKeepLocal != null && !conflictKeepLocal;
	}

	@Override
	public MethodNames getMethodName() {
		return MethodNames.USER_RESOURCE_SYNC;
	}
}
