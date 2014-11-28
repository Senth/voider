package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

/**
 * Information about bullets we fetched or search for
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BulletFetchMethodResponse extends FetchMethodResponse {
	/** All bullets */
	public ArrayList<BulletDefEntity> bullets = new ArrayList<>();
}
