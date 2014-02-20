package com.spiddekauga.voider.resources;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.ResourceLocalRepo;
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
	private static void init() {
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

		// Update revision
		if (resource instanceof IResourceRevision) {
			oldRevision = ((IResourceRevision) resource).getRevision();


			RevisionInfo revisionInfo = ResourceLocalRepo.getRevisionLatest(resource.getId());
			int nextRevision = 1;
			if (revisionInfo != null) {
				nextRevision = revisionInfo.revision + 1;
			}
			((IResourceRevision) resource).setRevision(nextRevision);
		}


		Kryo kryo = Pools.kryo.obtain();
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		kryo.writeObject(output, resource);
		output.close();

		try {
			byte[] encryptedDef = mCrypter.encrypt(byteOut.toByteArray());

			//			if (Gdx.app != null) {
			//				Gdx.app.debug("ResourceSaver", "Encrypted (" + resource.getClass().getSimpleName() + ") " + resource.getId().toString());
			//			}


			String filePath = ResourceLocalRepo.getFilepath(resource);
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
			ResourceLocalRepo.add(resource);


			// Copy to revision directory
			if (resource instanceof IResourceRevision) {
				String revisionPath = ResourceLocalRepo.getRevisionFilepath((IResourceRevision) resource);
				FileHandle revisionFile = Gdx.files.external(revisionPath);
				saveFile.copyTo(revisionFile);
			}

			if (Gdx.app != null) {
				Gdx.app.debug("ResourceSaver", "Saved resource (" + resource.getClass().getSimpleName() + ") " + filePath);
			}

		} catch (Exception e) {
			ResourceLocalRepo.remove(resource.getId());

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
	 * Removes ALL resources! BEWARE DRAGONS!
	 */
	public static void clearResources() {
		String resourcePath = ResourceLocalRepo.getFilepath(UUID.randomUUID());
		FileHandle resource = Gdx.files.external(resourcePath);
		FileHandle parent = resource.parent();

		if (parent.exists() && parent.isDirectory()) {
			parent.deleteDirectory();
		}
	}

	/** Private constructor to enforce that no instance exist */
	private ResourceSaver() {
		// Does nothing
	}

	/** Crypter used for encrypting/decrypting files */
	private static ObjectCrypter mCrypter = null;

	static {
		init();
	}
}
