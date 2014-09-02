package com.spiddekauga.voider.repo.resource;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.utils.Pools;

/**
 * File gateway for resources
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
	 * @return true if the resource was saved successfully
	 */
	boolean save(IResource resource) {
		Kryo kryo = Pools.kryo.obtain();
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		kryo.writeObject(output, resource);
		output.close();

		boolean success = true;
		String filePath = getFilepath(resource.getId());

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

			// Copy to revision dir
			if (resource instanceof IResourceRevision) {
				success = copyFromResourceToRevision(resource.getId(), ((IResourceRevision) resource).getRevision());
			}

		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.error("ResourceFileGateway", "Could not save the resource!\n" + Strings.stackTraceToString(e));
			success = false;
		}
		Pools.kryo.free(kryo);

		return success;
	}

	/**
	 * Removes a resource file
	 * @param resourceId the resource to physically remove
	 */
	void remove(UUID resourceId) {
		String filepath = getFilepath(resourceId);
		FileHandle file = Gdx.files.external(filepath);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Removes a revision directory if it exists
	 * @param resourceId the resource to remove all revisions for on the disk
	 */
	void removeRevisionDir(UUID resourceId) {
		String revisionDir = getRevisionDir(resourceId);
		FileHandle dir = Gdx.files.external(revisionDir);
		if (dir.exists() && dir.isDirectory()) {
			dir.deleteDirectory();
		}
	}

	/**
	 * Removes the specified revision and all later ones for the specified resource
	 * @param resourceId the resource to remove revisions from
	 * @param fromRevision remove all revisions from this one
	 */
	void removeRevisions(UUID resourceId, int fromRevision) {
		int revision = fromRevision;

		while (true) {
			String path = getRevisionFilepath(resourceId, revision);
			FileHandle file = Gdx.files.external(path);

			if (!file.exists()) {
				break;
			}

			file.delete();
		}
	}

	/**
	 * Delete the specified file
	 * @param filePath the file to delete
	 * @return true if the file was deleted successfully, will return true even if the file does not exist
	 */
	boolean remove(String filePath) {
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
	private boolean copy(String from, String to) {
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

	/**
	 * Creates a copy from the resource location to a revision directory
	 * @param resourceId the resource to make a copy of
	 * @param revision which revision to create a copy to
	 * @return true if copy was successful
	 */
	boolean copyFromRevisionToResource(UUID resourceId, int revision) {
		String resourceFile = getFilepath(resourceId);
		String revisionFile = getRevisionFilepath(resourceId, revision);

		return copy(revisionFile, resourceFile);
	}

	/**
	 * Creates a copy from the revision directory to a resource location
	 * @param resourceId the resource to make a copy of (and overwrite)
	 * @param revision which revision to make a copy of
	 * @return true if copy was successful
	 */
	boolean copyFromResourceToRevision(UUID resourceId, int revision) {
		String resourceFile = getFilepath(resourceId);
		String revisionFile = getRevisionFilepath(resourceId, revision);

		return copy(resourceFile, revisionFile);
	}

	/**
	 * @param resourceId the resource to get the filepath from
	 * @return filepath of the resource
	 */
	String getFilepath(UUID resourceId) {
		return getDir() + resourceId;
	}

	/**
	 * @param resourceId id of the resource to get resource revision directory for
	 * @return directory where the resource's revisions are located
	 */
	String getRevisionDir(UUID resourceId) {
		return getDir() + resourceId + REVISION_DIR_POSTFIX;
	}

	/**
	 * @param resourceId id of the resource revision to get the filepath from
	 * @param revision the specific revision file to get
	 * @return filepath to the specific revision
	 */
	String getRevisionFilepath(UUID resourceId, int revision) {
		return getRevisionDir(resourceId) + getRevisionFormat(revision);
	}

	/**
	 * @return resource directory
	 */
	String getDir() {
		return Config.File.getUserStorage() + "resources/";
	}

	/**
	 * @param revision the revision to get the format of
	 * @return revision file format from the revision
	 */
	String getRevisionFormat(int revision) {
		return String.format("%010d", revision);
	}

	/** Crypter used for encrypting/decrypting files */
	private ObjectCrypter mCrypter = null;
	/** Revision postfix */
	private static final String REVISION_DIR_POSTFIX = "_revs/";
}
