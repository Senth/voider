package com.spiddekauga.voider.backup;

import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.server.util.SearchTables;

/**
 * Delete all search tables
 */
public class DeleteSearchJob extends Job0<Void> {
private static final String[] SEARCH_TABLES = {
		SearchTables.BULLET,
		SearchTables.ENEMY,
		SearchTables.LEVEL,
};

@Override
public Value<Void> run() throws Exception {
	for (String table : SEARCH_TABLES) {
		SearchUtils.deleteIndex(table);
	}

	return immediate(null);
}
}
