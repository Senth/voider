package com.spiddekauga.voider.analytics.datastore_bigquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;

/**
 * Combines multiple analytics scenes from a mapper to a list

 */
public class SceneOutput extends Output<Scene, List<Scene>> {
	private static class SceneOutputWriter extends OutputWriter<Scene> {
		@Override
		public void write(Scene value) throws IOException {
			mScenes.add(value);
		}

		/**
		 * @return list of all scenes
		 */
		private List<Scene> toList() {
			return mScenes;
		}

		private List<Scene> mScenes = new ArrayList<>();
		private static final long serialVersionUID = 550777081118427573L;
	}

	@Override
	public List<? extends OutputWriter<Scene>> createWriters(int numShards) {
		List<SceneOutputWriter> writers = new ArrayList<>();
		for (int i = 0; i < numShards; i++) {
			writers.add(new SceneOutputWriter());
		}
		return writers;
	}

	@Override
	public List<Scene> finish(Collection<? extends OutputWriter<Scene>> writers) throws IOException {
		List<Scene> scenes = new ArrayList<>();
		for (OutputWriter<Scene> writer : writers) {
			SceneOutputWriter sceneWriter = (SceneOutputWriter) writer;
			scenes.addAll(sceneWriter.toList());
		}

		return scenes;
	}

	private static final long serialVersionUID = 8010321817665869480L;
}
