package com.spiddekauga.voider.repo.resource;

import org.ini4j.Ini;

import com.badlogic.gdx.Gdx;
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

/**
 * Loads internal resource (images, skins, fonts, etc).
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
	}

	@Override
	protected String getFilepath(InternalNames identifier) {
		return identifier.getFilePath();
	}

	@Override
	protected Class<?> getType(InternalNames identifier) {
		return identifier.getType();
	}

	@Override
	protected synchronized void reload(InternalNames identifier) {
		// Reload the actual asset
		if (isLoaded(identifier)) {
			String filepath = getFilepath(identifier);
			int cRefs = mAssetManager.getReferenceCount(filepath);
			mAssetManager.setReferenceCount(filepath, 1);
			mAssetManager.unload(filepath);
			mAssetManager.load(filepath, identifier.getType());
			mAssetManager.finishLoading();
			mAssetManager.setReferenceCount(filepath, cRefs);

			// Update existing loaded resource
			LoadedResource loadedResource = mLoadedResources.get(identifier);
			if (loadedResource != null) {
				loadedResource.resource = mAssetManager.get(filepath);
			}
		}
	}

	/**
	 * Replaces one resource with another one, immediately loads the new resource
	 * @param oldIdentifier the old resource to replace
	 * @param newIdentifier replaces the old resource
	 */
	void replace(InternalNames oldIdentifier, InternalNames newIdentifier) {
		LoadedResource loadedResource = mLoadedResources.remove(oldIdentifier);

		if (loadedResource != null) {
			// Unload old
			String oldFilepath = getFilepath(oldIdentifier);
			int cRefs = mAssetManager.getReferenceCount(oldFilepath);
			mAssetManager.setReferenceCount(oldFilepath, 1);
			mAssetManager.unload(oldFilepath);

			// Load new
			String newFilepath = getFilepath(newIdentifier);
			mAssetManager.load(newFilepath, newIdentifier.getType());
			mAssetManager.finishLoading();
			mAssetManager.setReferenceCount(newFilepath, cRefs);

			// Replace
			loadedResource.resource = mAssetManager.get(newFilepath);
			mLoadedResources.put(newIdentifier, loadedResource);
		} else {
			Gdx.app.error(ResourceInternalLoader.class.getSimpleName(), "Cannot find resource: " + oldIdentifier);
		}
	}
}
