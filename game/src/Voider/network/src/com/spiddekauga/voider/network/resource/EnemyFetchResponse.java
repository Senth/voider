package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;

/**
 * Information about enemies we fetched or search for
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class EnemyFetchResponse extends FetchResponse {
	/** All enemies */
	public ArrayList<EnemyDefEntity> enemies = new ArrayList<>();
}
