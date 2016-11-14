package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.AbsoluteFileHandleResolver;
import com.spiddekauga.voider.version.VersionContainer;

import org.ini4j.Ini;

/**
 * Loads internal resource (images, skins, fonts, etc).
 */
class ResourceInternalLoader extends ResourceLoader<InternalNames, Object> {
/**
 * @param assetManager
 */
ResourceInternalLoader(AssetManager assetManager) {
	super(assetManager);

	FileHandleResolver internalFileHandleResolver = null;
	if (Config.File.USE_EXTERNAL_RESOURCES) {
		internalFileHandleResolver = new AbsoluteFileHandleResolver();
	} else {
		internalFileHandleResolver = new InternalFileHandleResolver();
	}
	mAssetManager.setLoader(ShaderProgram.class, new ShaderLoader(internalFileHandleResolver));
	mAssetManager.setLoader(Skin.class, new SkinLoader(internalFileHandleResolver));
	mAssetManager.setLoader(Ini.class, new IniLoader(internalFileHandleResolver));
	mAssetManager.setLoader(Music.class, new MusicLoader(internalFileHandleResolver));
	mAssetManager.setLoader(Sound.class, new SoundLoader(internalFileHandleResolver));
	mAssetManager.setLoader(TextureAtlas.class, new TextureAtlasLoader(internalFileHandleResolver));
	mAssetManager.setLoader(String.class, new TextLoader(internalFileHandleResolver));
	mAssetManager.setLoader(VersionContainer.class, new VersionLoader(internalFileHandleResolver));
}

@Override
protected Class<?> getType(InternalNames identifier) {
	return identifier.getType();
}

@SuppressWarnings("rawtypes")
@Override
protected AssetLoaderParameters getParameters(InternalNames identifier) {
	return identifier.getParameters();
}

@Override
protected String getFilepath(InternalNames identifier) {
	return identifier.getFilePath();
}

/**
 * Replaces one resource with another one, immediately loads the new resource
 * @param oldIdentifier the old resource to replace
 * @param newIdentifier replaces the old resource
 */
@SuppressWarnings("unchecked")
void replace(InternalNames oldIdentifier, InternalNames newIdentifier) {
	LoadedResource loadedResource = mLoadedResources.remove(oldIdentifier);

	if (loadedResource != null) {
		// Unload old
		String oldFilepath = getFilepath(oldIdentifier);
		mAssetManager.unload(oldFilepath);

		// Load new
		String newFilepath = getFilepath(newIdentifier);
		mAssetManager.load(newFilepath, newIdentifier.getType(), getParameters(newIdentifier));
		mAssetManager.finishLoading();

		// Replace
		loadedResource.resource = mAssetManager.get(newFilepath);
		mLoadedResources.put(newIdentifier, loadedResource);
	} else {
		Gdx.app.error(ResourceInternalLoader.class.getSimpleName(), "Cannot find resource: " + oldIdentifier);
	}
}
}
