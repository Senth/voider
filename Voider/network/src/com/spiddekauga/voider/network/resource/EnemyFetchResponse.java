package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;

/**
 * Information about enemies we fetched or search for

 */
public class EnemyFetchResponse extends FetchResponse {
	/** All enemies */
	public ArrayList<EnemyDefEntity> enemies = new ArrayList<>();
}
