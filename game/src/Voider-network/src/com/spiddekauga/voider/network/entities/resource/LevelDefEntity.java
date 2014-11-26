package com.spiddekauga.voider.network.entities.resource;

import java.util.UUID;

/**
 * Level definition entity
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelDefEntity extends DefEntity {
	/** Level length */
	public float levelLength = 0;
	/** Level speed */
	public float levelSpeed = 0;
	/** Level id, not definition */
	public UUID levelId = null;
}
