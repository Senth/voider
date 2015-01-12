package com.spiddekauga.voider.network.analytics;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class AnalyticsMethod implements IMethodEntity {
	/** All sessions (with scenes and events) */
	public ArrayList<AnalyticsSessionEntity> sessions = new ArrayList<>();

	@Override
	public MethodNames getMethodName() {
		return MethodNames.ANALYTICS;
	}
}
