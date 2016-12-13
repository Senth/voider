package com.spiddekauga.voider.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;

/**
 * Combines multiple analytics scenes from a mapper to a list

 */
public class SceneOutput extends Output<AnalyticsScene, List<AnalyticsScene>> {
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
		private static final long serialVersionUID = 550777081118427573L;
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
	public List<AnalyticsScene> finish(Collection<? extends OutputWriter<AnalyticsScene>> writers) throws IOException {
		List<AnalyticsScene> scenes = new ArrayList<>();
		for (OutputWriter<AnalyticsScene> writer : writers) {
			SceneOutputWriter sceneWriter = (SceneOutputWriter) writer;
			scenes.addAll(sceneWriter.toList());
		}

		return scenes;
	}

	private static final long serialVersionUID = 8010321817665869480L;
}
