package com.spiddekauga.voider.network.resource;


/**
 * Fetches information about bullets
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BulletFetchMethod extends FetchMethod {
	/** Search by text if not null */
	public String searchString = null;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.BULLET_FETCH;
	}

}
