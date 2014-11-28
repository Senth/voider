package com.spiddekauga.voider.network.entities.resource;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Common class for fetching stuff from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public abstract class FetchMethod implements IMethodEntity {
	/** Cursor to continue the search/get from */
	public String nextCursor = null;
}
