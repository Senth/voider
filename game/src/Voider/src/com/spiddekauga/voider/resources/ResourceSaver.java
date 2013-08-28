package com.spiddekauga.voider.resources;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.JsonWrapper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.ObjectCrypter;

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
		String filename = resource.getId().toString();

		save(resource, filename);

		// Check if the resource is a definition which contains revision numbering
		if (resource instanceof IResourceRevision) {
			saveRevision((IResourceRevision)resource, filename);
		}
	}

	/**
	 * Encrypts and saves the object at the appropriate place
	 * If an object already exist with the same filename, the existing object
	 * will be moved filename.bak
	 * @param resource the object to encrypt and save
	 * @param filename the filename of the object to save
	 */
	private static void save(IResource resource, String filename) {
		assert(mCrypter != null);

		Json json = new JsonWrapper();
		String jsonString = json.toJson(resource);
		try {
			byte[] encryptedDef = mCrypter.encrypt(jsonString);

			Gdx.app.debug("ResourceSaver", "Encrypted " + filename);

			String relativePath = ResourceNames.getDirPath(resource.getClass());
			relativePath += filename;
			FileHandle saveFile = Gdx.files.external(relativePath);

			// Save the file
			saveFile.writeBytes(encryptedDef, false);

			Gdx.app.debug("ResourceSaver", "Saved resource " + filename);

		} catch (Exception e) {
			Gdx.app.error("ResourceSaver", "Could not encrypt message. Your file has not been saved!");
		}
	}

	/**
	 * Saves a copy of the current file as a revision. This will just copy the current
	 * resource from the current directory, so be sure to use
	 * @param resource the resource to save as a revision
	 * @param filename the name of the file, i.e. its uuid. But not its revision filename
	 */
	private static void saveRevision(IResourceRevision resource, String filename) {
		String revisionPath = ResourceNames.getRevisionFileName(resource);
		String originalPath = ResourceNames.getDirPath(resource.getClass()) + filename;

		// Copy from the original location
		FileHandle fileToCopy = Gdx.files.external(originalPath);
		FileHandle copyDestination = Gdx.files.external(revisionPath);

		if (fileToCopy.exists()) {
			fileToCopy.copyTo(copyDestination);
		} else {
			Gdx.app.error("ResourceSaver", "Could not save the revision as the original file wasn't found.");
		}
	}

	/**
	 * Removes all resource of the specified type! BEWARE DRAGONS!
	 * @param resourceType removes all the resources of this type from the folder
	 */
	public static void clearResources(Class<? extends IResource> resourceType) {
		try {
			String relativePath = ResourceNames.getDirPath(resourceType);
			FileHandle folder = Gdx.files.external(relativePath);

			if (folder.exists()) {
				folder.deleteDirectory();
			}
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("ResourceSaver", "Could not clear resources of the type: " + e.getMessage());
		}
	}

	/** Private constructor to enfore that no instance exist */
	private ResourceSaver() {
		// Does nothing
	}

	/** Crypter used for encrypting/decrypting files */
	private static ObjectCrypter mCrypter = null;
}
