package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.spiddekauga.voider.repo.resource.VersionLoader.VersionLoaderParameters;
import com.spiddekauga.voider.version.VersionContainer;
import com.spiddekauga.voider.version.VersionParser;

/**
 * Loads the changelog
 */
public class VersionLoader extends AsynchronousAssetLoader<VersionContainer, VersionLoaderParameters> {
/** Stored version container */
VersionContainer mVersionContainer = null;

/**
 * @param resolver
 */
public VersionLoader(FileHandleResolver resolver) {
	super(resolver);
}

@Override
public void loadAsync(AssetManager manager, String fileName, FileHandle file, VersionLoaderParameters parameter) {
	VersionParser versionParser = new VersionParser(file.read());
	mVersionContainer = versionParser.parse();
}

@Override
public VersionContainer loadSync(AssetManager manager, String fileName, FileHandle file, VersionLoaderParameters parameter) {
	return mVersionContainer;
}

@SuppressWarnings("rawtypes")
@Override
public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, VersionLoaderParameters parameter) {
	return null;
}

/** Parameters for version loader */
public static class VersionLoaderParameters extends AssetLoaderParameters<VersionContainer> {
}
}
