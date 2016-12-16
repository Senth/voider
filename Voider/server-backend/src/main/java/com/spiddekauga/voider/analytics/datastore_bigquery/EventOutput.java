package com.spiddekauga.voider.analytics.datastore_bigquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;

/**
 * Combines multiple analytics events from a mapper to a lilst

 */
public class EventOutput extends Output<Event, List<Event>> {
	private static class EventOutputWriter extends OutputWriter<Event> {
		@Override
		public void write(Event value) throws IOException {
			mEvents.add(value);
		}

		/**
		 * @return list of all events
		 */
		private List<Event> toList() {
			return mEvents;
		}

		private List<Event> mEvents = new ArrayList<>();
		private static final long serialVersionUID = 2034749618016148725L;
	}

	@Override
	public List<? extends OutputWriter<Event>> createWriters(int numShards) {
		List<EventOutputWriter> writers = new ArrayList<>();
		for (int i = 0; i < numShards; i++) {
			writers.add(new EventOutputWriter());
		}
		return writers;
	}

	@Override
	public List<Event> finish(Collection<? extends OutputWriter<Event>> writers) throws IOException {
		List<Event> events = new ArrayList<>();
		for (OutputWriter<Event> writer : writers) {
			EventOutputWriter eventWriter = (EventOutputWriter) writer;
			events.addAll(eventWriter.toList());
		}

		return events;
	}

	private static final long serialVersionUID = 3742005962312016190L;
}
