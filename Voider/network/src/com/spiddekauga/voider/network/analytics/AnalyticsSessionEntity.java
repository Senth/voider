package com.spiddekauga.voider.network.analytics;

import com.spiddekauga.voider.network.entities.IEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Session analytics
 */
public class AnalyticsSessionEntity implements IEntity {
/** Session id */
public UUID sessionId;
/** Start time of the session */
public Date startTime;
/** End time of the session */
public Date endTime;
/** Screen size */
public String screenSize;
/** All scenes in this session */
public ArrayList<AnalyticsSceneEntity> scenes = new ArrayList<>();
}
