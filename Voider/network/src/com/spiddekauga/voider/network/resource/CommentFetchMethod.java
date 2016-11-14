package com.spiddekauga.voider.network.resource;

import java.util.UUID;

/**
 * Gets comment for the specified level

 */
public class CommentFetchMethod extends FetchMethod {
	/** Resource id */
	public UUID resourceId;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.COMMENT_FETCH;
	}
}
