package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.spiddekauga.voider.repo.resource.ShaderLoader.ShaderParameter;

/**
 * Loads a shader program
 */
public class ShaderLoader extends AsynchronousAssetLoader<ShaderProgram, ShaderParameter> {
/** Vertex shader */
private String mVertexShader = null;
/** Fragment shader */
private String mFragmentShader = null;

/**
 * @param resolver
 */
public ShaderLoader(FileHandleResolver resolver) {
	super(resolver);
}

@Override
public void loadAsync(AssetManager manager, String fileName, FileHandle file, ShaderParameter parameter) {
	String vertexPath = fileName + ".vsh";
	String fragmentPath = fileName + ".fsh";

	FileHandle vertexFile = resolve(vertexPath);
	FileHandle fragmentFile = resolve(fragmentPath);

	if (vertexFile.exists() && fragmentFile.exists()) {
		mVertexShader = vertexFile.readString();
		mFragmentShader = fragmentFile.readString();
	}
}

@Override
public ShaderProgram loadSync(AssetManager manager, String fileName, FileHandle file, ShaderParameter parameter) {
	if (mFragmentShader != null && mVertexShader != null) {
		return new ShaderProgram(mVertexShader, mFragmentShader);
	} else {
		return null;
	}
}

@SuppressWarnings("rawtypes")
@Override
public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ShaderParameter parameter) {
	return null;
}

/** Parameter for the loader, not used */
static public class ShaderParameter extends AssetLoaderParameters<ShaderProgram> {
}
}
