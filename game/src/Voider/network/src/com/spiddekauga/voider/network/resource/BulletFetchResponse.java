package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;

/**
 * Information about bullets we fetched or search for
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BulletFetchResponse extends FetchResponse {
	/** All bullets */
	public ArrayList<BulletDefEntity> bullets = new ArrayList<>();
}
