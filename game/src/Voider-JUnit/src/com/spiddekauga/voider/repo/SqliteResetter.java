package com.spiddekauga.voider.repo;

import com.badlogic.gdx.sql.SQLiteGdxException;


/**
 * Reses SQLite database for the next test
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SqliteResetter extends SqliteGateway {

	/**
	 * Resets/Clears the database
	 */
	public static void reset() {
		if (mDatabase != null) {
			try {
				mDatabase.execSQL("PRAGMA writable_schema = 1; delete from sqlite_master where type = 'table'; PRAGMA writable_schema = 0; VACUUM;");
				SqliteUpgrader sqliteUpgrader = new SqliteUpgrader(mDatabase);
				sqliteUpgrader.initAndUpgrade();
			} catch (SQLiteGdxException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}
