package com.spiddekauga.voider.resources;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.JsonWrapper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.ObjectCrypter;

/**
 * Loads JSON objects (that has been encrypted) synchronously.
 *  * @param <StoredType> The stored type in the JSON file
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class JsonLoaderSync<StoredType> extends SynchronousAssetLoader<StoredType, JsonParameter<StoredType>> {
	/**
	 * Constructor which takes a file handle resolver (where to load resources
	 * from) and what type of object is stored in the json file.
	 * @param resolver where to load resources from
	 * @param type what type of object is stored in the json file
	 */
	public JsonLoaderSync(FileHandleResolver resolver, Class<StoredType> type) {
		super(resolver);

		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
		mStoredType = type;
	}

	@Override
	public StoredType load(AssetManager assetManager, String fileName, FileHandle file, JsonParameter<StoredType> parameter) {
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

		Json json = new JsonWrapper();
		return json.fromJson(mStoredType, jsonString);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, JsonParameter<StoredType> parameter) {
		return null;
	}

	/** Type of object stored in the json file */
	Class<StoredType> mStoredType;

	/** Decrypter */
	ObjectCrypter mCrypter;
}
