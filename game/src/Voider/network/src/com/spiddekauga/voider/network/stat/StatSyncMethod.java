package com.spiddekauga.voider.network.stat;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for synchronizing stats
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class StatSyncMethod implements IMethodEntity {
	private static final long serialVersionUID = 1L;
	/** Stats to sync */
	public StatSyncEntity syncEntity = null;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.STAT_SYNC;
	}

}
