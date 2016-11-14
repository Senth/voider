package com.spiddekauga.voider.network.stat;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for synchronizing stats
 */
public class StatSyncMethod implements IMethodEntity {
/** Stats to sync */
public StatSyncEntity syncEntity = null;

@Override
public MethodNames getMethodName() {
	return MethodNames.STAT_SYNC;
}

}
