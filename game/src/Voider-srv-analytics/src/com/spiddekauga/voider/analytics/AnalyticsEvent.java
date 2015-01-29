package com.spiddekauga.voider.analytics;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;


/**
 * Event information for BigQuery
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsEvent implements Serializable {
	/**
	 * Create a new analytics event
	 * @param key event key
	 * @param sceneKey parent scene key
	 * @param time when the event was fired, relative to when the scene was started
	 * @param type event type
	 * @param name name of the event
	 * @param data optional data
	 */
	public AnalyticsEvent(Key key, Key sceneKey, double time, int type, String name, String data) {
		this.key = key;
		this.sceneKey = sceneKey;
		this.time = time;
		this.type = type;
		this.name = name;
		this.data = data;
	}

	/**
	 * @return event key
	 */
	Key getKey() {
		return key;
	}

	/**
	 * @return the parent scene key
	 */
	Key getSceneKey() {
		return sceneKey;
	}

	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	private Key key;
	private Key sceneKey;
	private double time;
	private String name;
	private String data;
	private int type;

	private static final long serialVersionUID = 2963369048107681654L;
}
