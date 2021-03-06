package com.spiddekauga.voider.analytics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;

/**
 * Analytics scene data for BigQuery

 */
public class AnalyticsScene implements Serializable {
	/**
	 * Creates a scene without any events.
	 * @param key datastore key, not stored
	 * @param sessionKey parent session datastore key, not stored
	 * @param startTime
	 * @param length
	 * @param name
	 * @param loadTime
	 * @param dropout
	 */
	public AnalyticsScene(Key key, Key sessionKey, Date startTime, double length, String name, double loadTime, boolean dropout) {
		this.key = key;
		this.sessionKey = sessionKey;
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
	 * Adds another event to this scene
	 * @param event
	 */
	void addEvent(AnalyticsEvent event) {
		events.add(event);
	}

	/**
	 * @return all events
	 */
	public List<AnalyticsEvent> getEvents() {
		return events;
	}

	/**
	 * @return datastore key
	 */
	Key getKey() {
		return key;
	}

	/**
	 * @return parent session key
	 */
	Key getSessionKey() {
		return sessionKey;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (key == null) {
			return false;
		} else {
			return key.equals(obj);
		}
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the length
	 */
	public double getLength() {
		return length;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the loadTime
	 */
	public double getLoadTime() {
		return loadTime;
	}

	/**
	 * @return the dropout
	 */
	public boolean isDropout() {
		return dropout;
	}

	private Key sessionKey;
	private Key key;
	private long startTime;
	private double length;
	private String name;
	private double loadTime;
	private boolean dropout;
	private List<AnalyticsEvent> events = new ArrayList<>();

	private static final long serialVersionUID = -8495944865811931868L;
}
