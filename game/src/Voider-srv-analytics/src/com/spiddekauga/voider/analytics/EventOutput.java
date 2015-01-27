package com.spiddekauga.voider.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;

/**
 * Combines multiple analytics events from a mapper to a lilst
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class EventOutput extends Output<AnalyticsEvent, AnalyticsScene> {
	/**
	 * Create event output
	 * @param scene all events are from this scene
	 */
	public EventOutput(AnalyticsScene scene) {
		mScene = scene;
	}

	private static class EventOutputWriter extends OutputWriter<AnalyticsEvent> {
		@Override
		public void write(AnalyticsEvent value) throws IOException {
			mEvents.add(value);
		}

		/**
		 * @return list of all events
		 */
		private List<AnalyticsEvent> toList() {
			return mEvents;
		}

		private List<AnalyticsEvent> mEvents = new ArrayList<>();
	}

	@Override
	public List<? extends OutputWriter<AnalyticsEvent>> createWriters(int numShards) {
		List<EventOutputWriter> writers = new ArrayList<>();
		for (int i = 0; i < numShards; i++) {
			writers.add(new EventOutputWriter());
		}
		return writers;
	}

	@Override
	public AnalyticsScene finish(Collection<? extends OutputWriter<AnalyticsEvent>> writers) throws IOException {
		ArrayList<AnalyticsEvent> events = new ArrayList<>();
		for (OutputWriter<AnalyticsEvent> writer : writers) {
			EventOutputWriter eventWriter = (EventOutputWriter) writer;
			events.addAll(eventWriter.toList());
		}

		mScene.setEvents(events);

		return mScene;
	}

	private AnalyticsScene mScene;
}
