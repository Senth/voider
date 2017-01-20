package com.spiddekauga.voider.analytics.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;

/**
 * Combines multiple analytics sessions from a mapper to a list

 */
public class SessionOutput extends Output<Session, List<Session>> {
	private static class SessionOutputWriter extends OutputWriter<Session> {

		@Override
		public void write(Session value) throws IOException {
			mSessions.add(value);
		}

		/**
		 * @return list of all analytics sessions (thread-safe)
		 */
		List<Session> toList() {
			return mSessions;
		}


		private List<Session> mSessions = new ArrayList<>();
		private static final long serialVersionUID = 6950115683562076356L;
	}

	@Override
	public List<? extends OutputWriter<Session>> createWriters(int numShards) {
		List<SessionOutputWriter> writers = new ArrayList<>();
		for (int i = 0; i < numShards; i++) {
			writers.add(new SessionOutputWriter());
		}
		return writers;
	}

	@Override
	public List<Session> finish(Collection<? extends OutputWriter<Session>> writers) throws IOException {
		ArrayList<Session> sessions = new ArrayList<>();
		for (OutputWriter<Session> writer : writers) {
			SessionOutputWriter sessionWriter = (SessionOutputWriter) writer;
			sessions.addAll(sessionWriter.toList());
		}
		return sessions;
	}

	private static final long serialVersionUID = -5685614536060471733L;
}
