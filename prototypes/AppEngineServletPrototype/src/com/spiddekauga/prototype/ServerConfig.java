package com.spiddekauga.prototype;


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

		private String mName;
	}
}
