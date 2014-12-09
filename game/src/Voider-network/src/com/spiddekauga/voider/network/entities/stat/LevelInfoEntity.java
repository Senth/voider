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
	public LevelDefEntity defEntity = new LevelDefEntity();
	/** Level stats */
	public LevelStatsEntity stats = new LevelStatsEntity();
	/** Level tags */
	public ArrayList<Tags> tags = new ArrayList<>();
}
