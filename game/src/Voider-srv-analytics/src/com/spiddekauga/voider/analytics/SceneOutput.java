package com.spiddekauga.voider.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;

/**
 * Combines multiple analytics scenes from a mapper to a list
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SceneOutput extends Output<AnalyticsScene, AnalyticsSession> {
	/**
	 * Create scene output.
	 * @param session all scenes are from this session
	 */
	public SceneOutput(AnalyticsSession session) {
		mSession = session;
	}

	private static class SceneOutputWriter extends OutputWriter<AnalyticsScene> {
		@Override
		public void write(AnalyticsScene value) throws IOException {
			mScenes.add(value);
		}

		/**
		 * @return list of all scenes
		 */
		private List<AnalyticsScene> toList() {
			return mScenes;
		}

		private List<AnalyticsScene> mScenes = new ArrayList<>();
	}

	@Override
	public List<? extends OutputWriter<AnalyticsScene>> createWriters(int numShards) {
		List<SceneOutputWriter> writers = new ArrayList<>();
		for (int i = 0; i < numShards; i++) {
			writers.add(new SceneOutputWriter());
		}
		return writers;
	}

	@Override
	public AnalyticsSession finish(Collection<? extends OutputWriter<AnalyticsScene>> writers) throws IOException {
		ArrayList<AnalyticsScene> scenes = new ArrayList<>();
		for (OutputWriter<AnalyticsScene> writer : writers) {
			SceneOutputWriter sceneWriter = (SceneOutputWriter) writer;
			scenes.addAll(sceneWriter.toList());
		}

		mSession.setScenes(scenes);

		return mSession;
	}

	private AnalyticsSession mSession;
}
