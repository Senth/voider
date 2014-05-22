package com.spiddekauga.voider.resources;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.ResourceCorruptException;
import com.spiddekauga.voider.repo.ResourceNotFoundException;
import com.spiddekauga.voider.utils.Pools;

/**
 * Synchronously loads resources saved as Kryo objects (that has been encrypted).
 * @param <StoredType> The stored type in the JSON file
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class KryoLoaderSync<StoredType> extends SynchronousAssetLoader<StoredType, KryoParameters<StoredType>> {
	/**
	 * Constructor which takes a file handle resolver (where to load resources
	 * from) and what type of object is stored in the json file.
	 * @param resolver where to load resources from
	 * @param type what type of object is stored in the json file
	 */
	public KryoLoaderSync(FileHandleResolver resolver, Class<StoredType> type) {
		super(resolver);

		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
		mStoredType = type;
	}

	@Override
	public StoredType load(AssetManager assetManager, String fileName, FileHandle file, KryoParameters<StoredType> parameter) {
		if (!file.exists()) {
			throw new ResourceNotFoundException(fileName);
		}

		byte[] encryptedKryo = file.readBytes();
		byte[] decryptedKryo;
		try {
			decryptedKryo = mCrypter.decrypt(encryptedKryo, byte[].class);
		} catch (Exception e) {
			throw new ResourceCorruptException(fileName);
		}

		Input input = new Input(decryptedKryo);
		Kryo kryo = Pools.kryo.obtain();
		StoredType storedObject = kryo.readObject(input, mStoredType);
		Pools.kryo.free(kryo);
		return storedObject;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, KryoParameters<StoredType> parameter) {
		return null;
	}

	/** Type of object stored in the json file */
	Class<StoredType> mStoredType;

	/** Decrypter */
	ObjectCrypter mCrypter;
}