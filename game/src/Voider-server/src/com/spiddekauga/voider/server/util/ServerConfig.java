package com.spiddekauga.voider.server.util;


/**
 * Server configuration
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
		LEVEL_STATS("level_stats"),
		/** Actor stats */
		ACTOR_STATS("actor_stats")

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
}
