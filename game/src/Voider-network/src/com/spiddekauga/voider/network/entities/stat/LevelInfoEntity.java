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
	/** User level stats, null if user haven't played these */
	@Deprecated public UserLevelStatsEntity userStats = null;
	/** Level tags */
	@Deprecated public ArrayList<Tags> tags = null;
}
