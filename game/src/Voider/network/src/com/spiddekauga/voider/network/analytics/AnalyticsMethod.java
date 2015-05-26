package com.spiddekauga.voider.network.analytics;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsMethod implements IMethodEntity {
	/** All sessions (with scenes and events) */
	public ArrayList<AnalyticsSessionEntity> sessions = new ArrayList<>();
	/** OS this platform uses */
	public String os;
	/** Platform of this device */
	public String platform;
	/** User analytics id, unique to this device and user */
	public UUID userAnalyticsId;

	private static final long serialVersionUID = 1L;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.ANALYTICS;
	}
}
