package com.spiddekauga.voider.network.entities.resource;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Gets comment for the specified level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceCommentGetMethod implements IMethodEntity {
	/** Resource id */
	public UUID resourceId;
	/** Cursor, continues the query if not null */
	public String cursor = null;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.RESOURCE_COMMENT_GET;
	}
}
