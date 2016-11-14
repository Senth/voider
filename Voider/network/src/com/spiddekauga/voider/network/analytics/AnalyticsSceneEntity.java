package com.spiddekauga.voider.network.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Scene analytics information

 */
public class AnalyticsSceneEntity implements IEntity {
	/** The session this scene belongs to */
	public UUID sessionId;
	/** Id of this scene */
	public UUID sceneId;
	/** When the scene was entered */
	public Date startTime;
	/** When the scene was exited */
	public Date endTime;
	/** Name of the scene */
	public String name;
	/** Load time of the scene, in seconds */
	public float loadTime;
	/** If the game was quit through this scene */
	public boolean dropout = false;
	/** All events in this scene */
	public ArrayList<AnalyticsEventEntity> events = new ArrayList<>();
}
