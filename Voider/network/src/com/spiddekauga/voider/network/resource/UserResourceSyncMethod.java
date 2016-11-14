package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Method for syncing user resources (with revisions)
 */
public class UserResourceSyncMethod implements IMethodEntity {
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
