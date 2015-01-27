package com.spiddekauga.voider.analytics;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.appengine.api.datastore.Key;

/**
 * Analytics scene data for BigQuery
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties({ "key" })
public class AnalyticsScene implements Serializable {
	/**
	 * Creates a scene without any events.
	 * @param key datastore key, not stored
	 * @param startTime
	 * @param length
	 * @param name
	 * @param loadTime
	 * @param dropout
	 */
	public AnalyticsScene(Key key, Date startTime, double length, String name, double loadTime, boolean dropout) {
		this.key = key;
		this.startTime = startTime.getTime();
		this.length = length;
		this.name = name;
		this.loadTime = loadTime;
		this.dropout = dropout;
	}

	/**
	 * Sets the events for this scene
	 * @param events
	 */
	void setEvents(List<AnalyticsEvent> events) {
		this.events = events;
	}

	/**
	 * @return all events
	 */
	List<AnalyticsEvent> getEvents() {
		return events;
	}

	/**
	 * @return datastore key
	 */
	Key getKey() {
		return key;
	}

	private Key key;
	private long startTime;
	private double length;
	private String name;
	private double loadTime;
	private boolean dropout;
	private List<AnalyticsEvent> events = null;

	private static final long serialVersionUID = -8495944865811931868L;
}
