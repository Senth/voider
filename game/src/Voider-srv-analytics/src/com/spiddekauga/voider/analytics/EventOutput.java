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
public class EventOutput extends Output<AnalyticsEvent, List<AnalyticsEvent>> {
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
		private static final long serialVersionUID = 2034749618016148725L;
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
	public List<AnalyticsEvent> finish(Collection<? extends OutputWriter<AnalyticsEvent>> writers) throws IOException {
		List<AnalyticsEvent> events = new ArrayList<>();
		for (OutputWriter<AnalyticsEvent> writer : writers) {
			EventOutputWriter eventWriter = (EventOutputWriter) writer;
			events.addAll(eventWriter.toList());
		}

		return events;
	}

	private static final long serialVersionUID = 3742005962312016190L;
}
