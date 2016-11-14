package com.spiddekauga.voider.network.analytics;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**

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

	@Override
	public MethodNames getMethodName() {
		return MethodNames.ANALYTICS;
	}
}
