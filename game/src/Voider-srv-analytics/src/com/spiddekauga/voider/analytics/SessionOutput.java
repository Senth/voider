package com.spiddekauga.voider.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;

/**
 * Combines multiple analytics sessions from a mapper to a list
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SessionOutput extends Output<AnalyticsSession, List<AnalyticsSession>> {
	private static class SessionOutputWriter extends OutputWriter<AnalyticsSession> {

		@Override
		public void write(AnalyticsSession value) throws IOException {
			mSessions.add(value);
		}

		/**
		 * @return list of all analytics sessions (thread-safe)
		 */
		List<AnalyticsSession> toList() {
			return mSessions;
		}


		private List<AnalyticsSession> mSessions = new ArrayList<>();
		private static final long serialVersionUID = 6950115683562076356L;
	}

	@Override
	public List<? extends OutputWriter<AnalyticsSession>> createWriters(int numShards) {
		List<SessionOutputWriter> writers = new ArrayList<>();
		for (int i = 0; i < numShards; i++) {
			writers.add(new SessionOutputWriter());
		}
		return writers;
	}

	@Override
	public List<AnalyticsSession> finish(Collection<? extends OutputWriter<AnalyticsSession>> writers) throws IOException {
		ArrayList<AnalyticsSession> sessions = new ArrayList<>();
		for (OutputWriter<AnalyticsSession> writer : writers) {
			SessionOutputWriter sessionWriter = (SessionOutputWriter) writer;
			sessions.addAll(sessionWriter.toList());
		}
		return sessions;
	}

	private static final long serialVersionUID = -5685614536060471733L;
}
