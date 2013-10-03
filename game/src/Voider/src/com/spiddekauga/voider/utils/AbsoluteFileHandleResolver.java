package com.spiddekauga.voider.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AbsoluteFileHandleResolver implements FileHandleResolver {
	@Override
	public FileHandle resolve(String fileName) {
		return Gdx.files.absolute(fileName);
	}

}
