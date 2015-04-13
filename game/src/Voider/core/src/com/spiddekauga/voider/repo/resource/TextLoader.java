package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.repo.resource.TextLoader.TextLoaderParameters;

/**
 * Loads text files
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TextLoader extends AsynchronousAssetLoader<String, TextLoaderParameters> {
	/**
	 * @param resolver
	 */
	public TextLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, TextLoaderParameters parameter) {
		mText = null;

		if (!file.exists()) {
			throw new GdxRuntimeException("File not found: " + fileName);
		}

		mText = file.readString();
	}

	@Override
	public String loadSync(AssetManager manager, String fileName, FileHandle file, TextLoaderParameters parameter) {
		return mText;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TextLoaderParameters parameter) {
		return null;
	}

	/** Parameters for TextLoader */
	public static class TextLoaderParameters extends AssetLoaderParameters<String> {
	}

	/** Stored text */
	private String mText = null;
}
