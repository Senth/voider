package com.spiddekauga.voider.network.analytics;

import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Analytics event in a scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class AnalyticsEventEntity implements IEntity {
	/** Scene this event belongs to */
	public UUID sceneId;
	/** Event time */
	public Date time;
	/** Name of the event */
	public String name;
	/** Event data */
	public String data;
}
