package com.spiddekauga.voider.resources;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.JsonLoader.JsonParameter;
import com.spiddekauga.voider.utils.ObjectCrypter;

/**
 * Loads Json objects (that has been encrypted)
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 * @param <StoredType> what type of object is stored in the json file
 */
public class JsonLoader<StoredType> extends AsynchronousAssetLoader<StoredType, JsonParameter<StoredType>> {

	/**
	 * Constructor which takes a file handle resolver (where to load
	 * resources from) and what type of object is stored in the json
	 * file.
	 * @param resolver where to load resources from
	 * @param type what type of object is stored in the json file
	 */
	public JsonLoader (FileHandleResolver resolver, Class<StoredType> type) {
		super(resolver);
		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
		mStoredType = type;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, JsonParameter<StoredType> parameter) {
		mStoredObject = null;
		FileHandle file = resolve(fileName);

		if (!file.exists()) {
			throw new ResourceNotFoundException(fileName);
		}

		byte[] encryptedJson = file.readBytes();
		String jsonString = null;
		try {
			jsonString = (String) mCrypter.decrypt(encryptedJson);
		} catch (Exception e) {
			throw new ResourceCorruptException(fileName);
		}

		Json json = new Json();
		mStoredObject = json.fromJson(mStoredType, jsonString);
	}

	@Override
	public StoredType loadSync(AssetManager manager, String fileName, JsonParameter<StoredType> parameter) {
		return mStoredObject;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.assets.loaders.AssetLoader#getDependencies(java.lang.String, com.badlogic.gdx.assets.AssetLoaderParameters)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, JsonParameter<StoredType> parameter) {
		return null;
	}

	/** The actual object that was stored */
	StoredType mStoredObject = null;

	/** Parameters for the loader, not used
	 * @param <StoredType> the stored type*/
	static public class JsonParameter<StoredType> extends AssetLoaderParameters<StoredType> {}

	/** Type of object stored in the json file */
	Class<StoredType> mStoredType;

	/** Decrypter */
	ObjectCrypter mCrypter;
}
