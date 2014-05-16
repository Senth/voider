package com.spiddekauga.voider.repo;

import java.io.ByteArrayOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * File gateway for resources
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ResourceFileGateway {
	/**
	 * Default constructor
	 */
	ResourceFileGateway() {
		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
	}

	/**
	 * Saves a resource at the specified location
	 * @param resource the resource to save
	 * @param filePath relative file path from external storage to where the resource
	 * should be saved.
	 * @return true if the resource was saved successfully
	 */
	boolean save(IResource resource, String filePath) {
		Kryo kryo = Pools.kryo.obtain();
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		kryo.writeObject(output, resource);
		output.close();

		boolean success = true;

		try {
			byte[] encryptedDef = mCrypter.encrypt(byteOut.toByteArray());

			FileHandle saveFile = Gdx.files.external(filePath);

			// Create parent paths
			FileHandle parentDir = saveFile.parent();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			// Save the file
			saveFile.writeBytes(encryptedDef, false);
			Gdx.app.debug("ResourceFileGateway", "Saved resource (" + resource.getClass().getSimpleName() + ") " + filePath);

		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.error("ResourceSaver", "Could not save the resource!\n" + Strings.stackTraceToString(e));
			success = false;
		}
		Pools.kryo.free(kryo);

		return success;
	}

	/**
	 * Delete the specified file
	 * @param filePath the file to delete
	 * @return true if the file was deleted successfully, will return true
	 * even if the file does not exist
	 */
	boolean delete(String filePath) {
		FileHandle deleteFile = Gdx.files.external(filePath);

		if (deleteFile.exists()) {
			try {
				return deleteFile.delete();
			} catch (GdxRuntimeException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	/**
	 * Creates a copy of the resource (usually used for creating a revision copy)
	 * @param from relative file path where to copy from
	 * @param to relative file path where to copy to
	 * @return true if copy was successful
	 */
	boolean copy(String from, String to) {
		FileHandle fromFile = Gdx.files.external(from);
		FileHandle toFile = Gdx.files.external(to);

		FileHandle toDir = toFile.parent();

		try {
			if (!toDir.exists()) {
				toDir.mkdirs();
			}

			fromFile.copyTo(toFile);
		} catch (GdxRuntimeException exception) {
			exception.printStackTrace();
			return false;
		}

		return true;
	}

	/** Crypter used for encrypting/decrypting files */
	private ObjectCrypter mCrypter = null;
}
