package com.spiddekauga.voider.network.resource;

import java.util.UUID;

/**
 * Gets comment for the specified level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CommentFetchMethod extends FetchMethod {
	/** Resource id */
	public UUID resourceId;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.COMMENT_FETCH;
	}
}
