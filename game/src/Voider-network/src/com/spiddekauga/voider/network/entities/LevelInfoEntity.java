package com.spiddekauga.voider.network.entities;

import java.util.ArrayList;

/**
 * Wrapper for all level information
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelInfoEntity implements IEntity {
	/** Level definition entity for the level */
	public LevelDefEntity defEntity = null;
	/** Level stats */
	public LevelStatsEntity stats = null;
	/** User level stats, null if user haven't played these */
	public UserLevelStatsEntity userStats = null;
	/** Level tags */
	public ArrayList<Tags> tags = null;
}
