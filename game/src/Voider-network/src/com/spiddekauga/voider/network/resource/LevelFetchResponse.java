package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.stat.LevelInfoEntity;

/**
 * All levels that matched the query
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelFetchResponse extends FetchResponse {
	/** All levels */
	public ArrayList<LevelInfoEntity> levels = new ArrayList<>();
}
