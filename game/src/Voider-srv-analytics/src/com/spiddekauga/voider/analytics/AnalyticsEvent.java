package com.spiddekauga.voider.analytics;

import java.io.Serializable;


/**
 * Event information for BigQuery
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("unused")
public class AnalyticsEvent implements Serializable {
	/**
	 * Create a new analytics event
	 * @param time when the event was fired, relative to when the scene was started
	 * @param type event type
	 * @param name name of the event
	 * @param data optional data
	 */
	public AnalyticsEvent(double time, int type, String name, String data) {
		this.time = time;
		this.type = type;
		this.name = name;
		this.data = data;
	}

	private double time;
	private String name;
	private String data;
	private int type;

	private static final long serialVersionUID = 2963369048107681654L;
}
