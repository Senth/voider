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

		// Stats
		/** Highscore sync */
		HIGHSCORE_SYNC,
		/** Get highscore for a specific level */
		HIGHSCORE_GET,
		/** Synchronize statistics */
		STAT_SYNC,

		// Analytics
		/** Send analytics */
		ANALYTICS,

		;
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

		/** The actual URL of the method */
		private String mUrl;
	}
}
