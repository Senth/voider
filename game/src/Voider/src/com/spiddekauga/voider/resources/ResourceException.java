package com.spiddekauga.voider.resources;

import java.util.UUID;

/**
 * Common class for resource exceptions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ResourceException extends RuntimeException {
	/**
	 * Constructor
	 * @param filename the resource's filename
	 * @param resourceId UUID of the resource, if null the constructor
	 * will try to get the UUID from the filename
	 */
	public ResourceException(String filename, UUID resourceId) {
		super(filename != null ? filename : (resourceId != null ? resourceId.toString() : "null"));

		mResourceId = resourceId;

		if (filename != null) {
			if (mResourceId == null) {
				mResourceId = getUuidFromFilename(filename);
			}

			mRevision = getRevisionFromFilename(filename);
		}
	}

	/**
	 * Get UUID from the filename
	 * @param filename the filename to get the UUID from
	 * @return UUID if a UUID was found in the filename, otherwise null.
	 */
	private static UUID getUuidFromFilename(String filename) {
		// Use bruteforce approach if the path ever changes

		String iteratingFilename = filename;

		while (iteratingFilename.length() >= UUID_STRING_LENGTH) {
			String partFilename = iteratingFilename.substring(0, UUID_STRING_LENGTH);

			try {
				return UUID.fromString(partFilename);
			} catch (IllegalArgumentException e) {
				iteratingFilename = iteratingFilename.substring(1);
			}
		}

		return null;
	}

	/**
	 * Get revision from the filename
	 * @param filename the filename to get the revision from
	 * @return revision if a revision was found, -1 otherwise.
	 */
	private static int getRevisionFromFilename(String filename) {
		int beginRevision = filename.length() - 10;
		String revisionPart = filename.substring(beginRevision);

		try {
			return Integer.parseInt(revisionPart);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * @return resource id of the resource that threw the exception
	 */
	public UUID getId() {
		return mResourceId;
	}

	/**
	 * @return revision of the resource that threw the exception, -1 if no revision was specified
	 */
	public int getRevision() {
		return mRevision;
	}

	/** Length of UUID in string format */
	private static final int UUID_STRING_LENGTH = UUID.randomUUID().toString().length();
	/** Resource id */
	private UUID mResourceId;
	/** Revision */
	private int mRevision = -1;
	/** Generated serial id */
	private static final long serialVersionUID = 5513849148086050205L;
}
