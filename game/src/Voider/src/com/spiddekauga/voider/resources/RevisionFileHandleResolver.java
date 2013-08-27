package com.spiddekauga.voider.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/**
 * Class for getting a file handle for a resource that has revisions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class RevisionFileHandleResolver implements FileHandleResolver {
	@Override
	public FileHandle resolve(String fileName) {
		FileHandle regularFile = Gdx.files.external(fileName);
		if (regularFile.exists()) {
			return regularFile;
		}
		// Check for revision
		else {
			// Get directory
			// Find position of last slash (/)
			//			int dirPosition = fileName.lastIndexOf('/');
			//			FileHandle dir = Gdx.files.external(fileName.substring(0, dirPosition));
			//			FileHandle[] files = dir.list();
			FileHandle[] files = regularFile.parent().list();

			FileHandle correctFile = null;
			int i = 0;
			while (correctFile != null && i < fileName.length()) {
				if (files[i].nameWithoutExtension().equals(regularFile.nameWithoutExtension())) {
					correctFile = files[i];
				}
				++i;
			}

			return correctFile;
		}
	}
}
