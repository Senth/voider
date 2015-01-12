package com.spiddekauga.voider.network.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Session analytics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class AnalyticsSessionEntity implements IEntity {
	/** Session id */
	public UUID sessionId;
	/** Start time of the session */
	public Date startTime;
	/** End time of the session */
	public Date endTime;
	/** User analytics id */
	public UUID userAnalyticsId;
	/** Platform */
	public String platform;
	/** OS */
	public String os;
	/** Screen width */
	public int screenWidth;
	/** Screen height */
	public int screenHeight;
	/** All scenes in this session */
	public ArrayList<AnalyticsSceneEntity> scenes = new ArrayList<>();
}
