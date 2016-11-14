package com.spiddekauga.voider.network.analytics;

import com.spiddekauga.voider.network.entities.IEntity;

import java.util.Date;
import java.util.UUID;

/**
 * Analytics event in a scene
 */
public class AnalyticsEventEntity implements IEntity {
/** Scene this event belongs to */
public UUID sceneId;
/** Event time */
public Date time;
/** Name of the event */
public String name;
/** Event data */
public String data;
/** Event type */
public AnalyticsEventTypes type;
}
