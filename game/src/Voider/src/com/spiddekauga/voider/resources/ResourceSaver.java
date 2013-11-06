package com.spiddekauga.voider.resources;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.ObjectCrypter;
import com.spiddekauga.voider.utils.Pools;

/**
 * Saves resources into the appropriate place. The saver is also responsible
 * for encrypting the resources.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceSaver {

	/**
	 * Initializes the class with default parameters
	 */
	public static void init() {
		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
	}

	/**
	 * Saves a resource that has a unique id
	 * @param resource the resource to save
	 */
	public static void save(IResource resource) {
		assert(mCrypter != null);

		Date oldDate = null;
		int oldRevision = -1;

		// Update date
		if (resource instanceof Def) {
			oldDate = ((Def) resource).getDate();
			((Def) resource).updateDate();
		}

		// Update date
		if (resource instanceof IResourceRevision) {
			oldRevision = ((IResourceRevision) resource).getRevision();

			int nextRevision = ResourceDatabase.getLatestRevisionNumber(resource.getId());
			nextRevision++;
			((IResourceRevision) resource).setRevision(nextRevision);
		}


		Kryo kryo = Pools.kryo.obtain();
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		kryo.writeObject(output, resource);
		output.close();

		try {
			byte[] encryptedDef = mCrypter.encrypt(byteOut.toByteArray());

			if (Gdx.app != null) {
				Gdx.app.debug("ResourceSaver", "Encrypted (" + resource.getClass().getSimpleName() + ") " + resource.getId().toString());
			}

			ResourceDatabase.addSavedResource(resource);

			String filePath = ResourceDatabase.getFilePath(resource);
			FileHandle saveFile = Gdx.files.external(filePath);

			// Create parent paths
			@SuppressWarnings("unchecked")
			ArrayList<FileHandle> directoriesToCreate = Pools.arrayList.obtain();
			FileHandle currentDir = saveFile.parent();
			currentDir.mkdirs();
			while (!currentDir.exists()) {
				directoriesToCreate.add(currentDir);
				currentDir = currentDir.parent();
			}

			for (int i = directoriesToCreate.size() - 1; i >= 0; --i) {
				Gdx.app.debug("ResourceSaver", "Mkdir: " + directoriesToCreate.get(i).path());
				directoriesToCreate.get(i).mkdirs();
			}

			// Save the file
			saveFile.writeBytes(encryptedDef, false);


			// Create link (or copy for now)
			if (resource instanceof IResourceRevision) {
				String latestPath = ResourceDatabase.getFilePath(resource.getId(), -1);
				FileHandle latestCopy = Gdx.files.external(latestPath);
				saveFile.copyTo(latestCopy);
			}

			if (Gdx.app != null) {
				Gdx.app.debug("ResourceSaver", "Saved resource (" + resource.getClass().getSimpleName() + ") " + filePath);
			}

		} catch (Exception e) {
			ResourceDatabase.removeSavedResource(resource);

			if (resource instanceof IResourceRevision) {
				((IResourceRevision) resource).setRevision(oldRevision);
			}

			if (resource instanceof Def) {
				((Def) resource).setDate(oldDate);
			}

			Gdx.app.error("ResourceSaver", "Could not save the resource!\n" + Strings.stackTraceToString(e));
		}
		Pools.kryo.free(kryo);
	}

	/**
	 * Removes all resource of the specified type! BEWARE DRAGONS!
	 * @param resourceType removes all the resources of this type from the folder
	 */
	public static void clearResources(Class<? extends IResource> resourceType) {
		ResourceDatabase.removeAllOf(resourceType);
	}

	/** Private constructor to enfore that no instance exist */
	private ResourceSaver() {
		// Does nothing
	}

	/** Crypter used for encrypting/decrypting files */
	private static ObjectCrypter mCrypter = null;
}
