package com.spiddekauga.voider.network.entities;


/**
 * Abstract class for all method entities
 */
public abstract interface IMethodEntity extends IEntity {
/**
 * @return method url
 */
MethodNames getMethodName();

/**
 * Helper method names
 */
public enum MethodNames {
	// Misc
	/** For downloading a blob */
	BLOB_DOWNLOAD,
	/** Send a bug report */
	BUG_REPORT,
	/** Get upload URL for a blob */
	GET_UPLOAD_URL,

	// Backup
	/** Get new blobs for backup */
	BACKUP_NEW_BLOBS,
	/** Delete all blobs from the server */
	DELETE_ALL_BLOBS,
	/** Restore blobs to the server */
	RESTORE_BLOBS,

	// Resources
	/** Synchronize downloaded resources */
	DOWNLOAD_SYNC,
	/** Fetch levels definitions */
	LEVEL_FETCH,
	/** Fetch enemy definitions */
	ENEMY_FETCH,
	/** Fetch bullet definitions */
	BULLET_FETCH,
	/** Get level comments */
	COMMENT_FETCH,
	/** Publish resources */
	PUBLISH,
	/** Download a resources */
	RESOURCE_DOWNLOAD,
	/** Synchronize user resources */
	USER_RESOURCE_SYNC,
	/** Fix user resource conflicts */
	USER_RESOURCE_FIX_CONFLICT,


	// User
	/** Login the user */
	LOGIN,
	/** Logout the user */
	LOGOUT,
	/** Register new user */
	REGISTER_USER,
	/** Reset password -> Send token */
	PASSWORD_RESET_SEND_TOKEN,
	/** Reset password -> New password from token */
	PASSWORD_RESET,
	/** Change account settings */
	ACCOUNT_CHANGE,

	// Stats
	/** Highscore sync */
	HIGHSCORE_SYNC,
	/** Get highscore for a specific level */
	HIGHSCORE_GET,
	/** Synchronize statistics */
	STAT_SYNC,

	// Analytics
	/** Send analytics */
	ANALYTICS,;

	/** The actual URL of the method */
	private String mUrl;

	/**
	 * Creates the enumeration with the correct URL
	 */
	private MethodNames() {
		mUrl = "api/" + name().toLowerCase().replace('_', '-');
	}

	@Override
	public String toString() {
		return mUrl;
	}
}
}
