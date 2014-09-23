package com.spiddekauga.voider.network.entities;


/**
 * Abstract class for all method entities
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
		/** Get upload url for a blob */
		GET_UPLOAD_URL,

		// Resources
		/** Synchronize downloaded resources */
		DOWNLOAD_SYNC,
		/** Get all levels */
		LEVEL_GET_ALL,
		/** Get level comments */
		RESOURCE_COMMENT_GET,
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

		// Stats
		/** Highscore sync */
		HIGHSCORE_SYNC,
		/** Get highscore for a specific level */
		HIGHSCORE_GET,
		/** Synchronize statistics */
		STAT_SYNC,


		;
		/**
		 * Creates the enumeration with the correct url
		 */
		private MethodNames() {
			mUrl = name().toLowerCase().replace('_', '-');
		}

		@Override
		public String toString() {
			return mUrl;
		}

		/** The actual url of the method */
		private String mUrl;
	}
}
