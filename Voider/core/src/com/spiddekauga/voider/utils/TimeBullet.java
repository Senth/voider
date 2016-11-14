package com.spiddekauga.voider.utils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.game.actors.BulletActor;

/**
 * Container class for bullet and time
 */
public class TimeBullet {
/** The bound bullet actor to this time */
@Tag(86)
public BulletActor bulletActor = null;
/** Time bound to the bullet actor */
public float time = 0;
}
