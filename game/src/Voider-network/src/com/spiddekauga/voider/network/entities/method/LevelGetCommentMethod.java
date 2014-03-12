package com.spiddekauga.voider.network.entities.method;

import java.util.UUID;

/**
 * Gets comment for the specified level
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetCommentMethod implements IMethodEntity {
	/** Level id */
	public UUID levelId;
	/** Cursor, continues the query if not null */
	public String cursor = null;

	@Override
	public String getMethodName() {
		return "level-get-comment";
	}
}
