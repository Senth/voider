package com.spiddekauga.voider.network.entities;

import java.util.UUID;

/**
 * Level definition entity
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelDefEntity extends DefEntity {
	/** Level length */
	public float levelLength = 0;
	/** Level id, not definition */
	public UUID levelId = null;
	/** Level speed */
	public float levelSpeed = 0;
}
