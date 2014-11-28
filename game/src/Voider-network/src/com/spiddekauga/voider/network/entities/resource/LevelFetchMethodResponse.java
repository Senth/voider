package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;

/**
 * All levels that matched the query
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelFetchMethodResponse extends FetchMethodResponse {
	/** All levels */
	public ArrayList<LevelInfoEntity> levels = new ArrayList<>();
}
