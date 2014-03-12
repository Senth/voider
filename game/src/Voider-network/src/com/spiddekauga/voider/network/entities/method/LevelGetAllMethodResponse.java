package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;

/**
 * All levels that matched the query
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetAllMethodResponse implements IEntity {
	/** All levels */
	public ArrayList<LevelInfoEntity> levels = new ArrayList<>();
	/** Datastore cursor to continue the query */
	public String cursor = null;
	/** Search offset */
	public int searchOffset = 0;
	/** True if no more levels can be fetched */
	public boolean fetchedAll = false;
}
