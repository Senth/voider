package com.spiddekauga.voider.server.util;


/**
 * Server configuration
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ServerConfig {
	/** Database tables */
	public enum DatastoreTables {
		/** Blob information for all blobs */
		BLOB_INFO("__BlobInfo__"),
		/** All users */
		USERS("users"),
		/** Published resources */
		PUBLISHED("published"),
		/** Dependencies */
		DEPENDENCY("dependency"),
		/** Different revisions of a resource */
		REVISION("revision"),
		/** Revision dependencies */
		REVISION_DEPNDENCY("revision_dependency"),
		/** Level statistics */
		LEVEL_STAT("level_stat"),
		/** Actor stats */
		ACTOR_STAT("actor_stat"),
		/** User level statistics */
		USER_LEVEL_STAT("user_level_stat"),
		/** User level tags */
		USER_LEVEL_TAG("user_level_tag"),
		/** Level tags */
		LEVEL_TAG("level_tag"),
		/** Level comments */
		LEVEL_COMMENT("level_comment"),


		;
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
		public static final int RESOURCE = 2;
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
}
