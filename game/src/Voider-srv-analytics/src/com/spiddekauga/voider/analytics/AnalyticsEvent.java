package com.spiddekauga.voider.analytics;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.appengine.api.datastore.Key;


/**
 * Event information for BigQuery
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties({ "sceneKey" })
public class AnalyticsEvent implements Serializable {
	/**
	 * Create a new analytics event
	 * @param sceneKey parent scene key
	 * @param time when the event was fired, relative to when the scene was started
	 * @param type event type
	 * @param name name of the event
	 * @param data optional data
	 */
	public AnalyticsEvent(Key sceneKey, double time, int type, String name, String data) {
		this.sceneKey = sceneKey;
		this.time = time;
		this.type = type;
		this.name = name;
		this.data = data;
	}

	/**
	 * @return the parent scene key
	 */
	public Key getSceneKey() {
		return sceneKey;
	}

	private Key sceneKey;
	private double time;
	private String name;
	private String data;
	private int type;

	private static final long serialVersionUID = 2963369048107681654L;
}
