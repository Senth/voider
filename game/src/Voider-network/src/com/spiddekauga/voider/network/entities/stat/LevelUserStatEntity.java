package com.spiddekauga.voider.network.entities.stat;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Level user stats entity
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelUserStatEntity implements IEntity {
	/** Total plays */
	public int cPlayed = 0;
	/** Total number of clears */
	public int cCleared = 0;
	/** Bookmarked */
	public boolean bookmarked = false;
	/** Rating */
	public int rating = 0;
	/** Tags for the level */
	public ArrayList<Tags> tags = new ArrayList<>();
}
