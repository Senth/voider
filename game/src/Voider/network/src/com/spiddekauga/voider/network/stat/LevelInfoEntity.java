package com.spiddekauga.voider.network.stat;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.resource.LevelDefEntity;

/**
 * Wrapper for all level information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LevelInfoEntity implements IEntity {
	private static final long serialVersionUID = 1L;
	/** Level definition entity for the level */
	public LevelDefEntity defEntity = new LevelDefEntity();
	/** Level stats */
	public LevelStatsEntity stats = new LevelStatsEntity();
	/** Level tags */
	public ArrayList<Tags> tags = new ArrayList<>();
}
