package com.spiddekauga.voider.network.entities.stat;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.resource.LevelDefEntity;

/**
 * Wrapper for all level information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelInfoEntity implements IEntity {
	/** Level definition entity for the level */
	public LevelDefEntity defEntity = null;
	/** Level stats */
	public LevelStatsEntity stats = null;
	/** Level tags */
	public ArrayList<Tags> tags = null;
}
