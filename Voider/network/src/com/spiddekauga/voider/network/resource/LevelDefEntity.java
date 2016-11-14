package com.spiddekauga.voider.network.resource;

import java.util.UUID;

/**
 * Level definition entity
 */
public class LevelDefEntity extends DefEntity {
/** Level length */
public float levelLength = 0;
/** Level speed */
public float levelSpeed = 0;
/** Level id, not definition */
public UUID levelId = null;
/**
 * Sets default variables
 */
public LevelDefEntity() {
	type = UploadTypes.LEVEL_DEF;
}
}
