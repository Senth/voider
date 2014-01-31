package com.spiddekauga.voider.resources;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.Pools;

/**
 * Asynchronously loads resources saved as Kryo objects (that has been encrypted)
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 * @param <StoredType> what type of object is stored in the json file
 */
public class KryoLoaderAsync<StoredType> extends AsynchronousAssetLoader<StoredType, KryoParameters<StoredType>> {

	/**
	 * Constructor which takes a file handle resolver (where to load
	 * resources from) and what type of object is stored in the json
	 * file.
	 * @param resolver where to load resources from
	 * @param type what type of object is stored in the json file
	 */
	public KryoLoaderAsync (FileHandleResolver resolver, Class<StoredType> type) {
		super(resolver);
		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
		mStoredType = type;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, KryoParameters<StoredType> parameter) {
		mStoredObject = null;

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
		mStoredObject = kryo.readObject(input, mStoredType);
		Pools.kryo.free(kryo);
	}

	@Override
	public StoredType loadSync(AssetManager manager, String fileName, FileHandle file, KryoParameters<StoredType> parameter) {
		return mStoredObject;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.assets.loaders.AssetLoader#getDependencies(java.lang.String, com.badlogic.gdx.assets.AssetLoaderParameters)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, KryoParameters<StoredType> parameter) {
		return null;
	}

	/** The actual object that was stored */
	StoredType mStoredObject = null;

	/** Type of object stored in the json file */
	Class<StoredType> mStoredType;

	/** Decrypter */
	ObjectCrypter mCrypter;
}
