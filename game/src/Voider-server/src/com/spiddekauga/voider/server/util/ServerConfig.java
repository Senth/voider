package com.spiddekauga.voider.server.util;


/**
 * Server configuration
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ServerConfig {
	/** Database tables */
	public enum DatastoreTables {
		/** Blob information for all blobs */
		BLOB_INFO("__BlobInfo__"),
		/** All users */
		USERS,
		/** Published resources */
		PUBLISHED,
		/** Dependencies */
		DEPENDENCY,
		/** Different revisions of a resource */
		REVISION,
		/** Revision dependencies */
		REVISION_DEPNDENCY,
		/** Level statistics */
		LEVEL_STAT,
		/** Actor stats */
		ACTOR_STAT,
		/** User level statistics */
		USER_LEVEL_STAT,
		/** User level tags */
		USER_LEVEL_TAG,
		/** Level tags */
		LEVEL_TAG,
		/** Level comments */
		RESOURCE_COMMENT,


		;
		/**
		 * Default constructor, uses lower case enum name
		 */
		private DatastoreTables() {
			mName = name().toLowerCase();
		}

		/**
		 * Sets the name of the actual table
		 * @param name the name of the table
		 */
		private DatastoreTables(String name) {
			mName = name;
		}

		@Override
		public String toString() {
			return mName;
		}

		/**
		 * Name
		 */
		private String mName;
	}

	/** Email administrator */
	public static final String EMAIL_ADMIN = "spiddekauga@voider-game.com";

	/** Public Search tokenize sizes */
	public static class TokenSizes {
		/** Minimum token size for resources */
		public static final int RESOURCE = 1;
	}

	/** How many results to send */
	public static class FetchSizes {
		/** Number of comments to fetch */
		public static final int COMMENTS = 20;
		/** Number of levels to fetch */
		public static final int LEVELS = 12;
		/** Number of tags to get */
		public static final int TAGS = 5;
	}

	/** User information */
	public static class UserInfo {
		/** Minimum name length */
		public static final int NAME_LENGTH_MIN = 3;
		/** Minimum password length */
		public static final int PASSWORD_LENGTH_MIN = 5;
	}
}
