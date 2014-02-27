package com.spiddekauga.voider.network.entities.method;

import java.util.UUID;

/**
 * Gets comment for the specified level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelGetCommentMethod implements IMethodEntity {
	/** Level id */
	public UUID levelId;
	/** Offset */
	public int offset = 0;

	@Override
	public String getMethodName() {
		return "level-get-comment";
	}
}
