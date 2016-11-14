package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.repo.resource.IniLoader.IniLoaderParameters;

import org.ini4j.Ini;

import java.io.IOException;

/**
 * Loads INI files
 */
public class IniLoader extends AsynchronousAssetLoader<Ini, IniLoaderParameters> {
/** Stored ini */
Ini mStoredIni = null;


/**
 * @param resolver
 */
public IniLoader(FileHandleResolver resolver) {
	super(resolver);
}

@Override
public void loadAsync(AssetManager manager, String fileName, FileHandle file, IniLoaderParameters parameter) {
	mStoredIni = null;

	if (!file.exists()) {
		throw new ResourceNotFoundException(fileName);
	}

	try {
		mStoredIni = new Ini(file.read());
	} catch (IOException e) {
		e.printStackTrace();
		throw new GdxRuntimeException(e);
	}
}

@Override
public Ini loadSync(AssetManager manager, String fileName, FileHandle file, IniLoaderParameters parameter) {
	return mStoredIni;
}

@SuppressWarnings("rawtypes")
@Override
public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, IniLoaderParameters parameter) {
	return null;
}

/** Parameters for IniLoader */
public static class IniLoaderParameters extends AssetLoaderParameters<Ini> {
}
}
