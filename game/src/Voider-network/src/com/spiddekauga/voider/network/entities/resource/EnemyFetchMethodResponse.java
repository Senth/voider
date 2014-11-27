package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

/**
 * Information about enemies we fetched or search for
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class EnemyFetchMethodResponse extends ResourceFetchMethodResponse {
	/** All enemies */
	public ArrayList<EnemyDefEntity> enemies = new ArrayList<>();
}
