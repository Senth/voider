package com.spiddekauga.voider.game;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.utils.TimeBullet;

/**
 * Container for the bullet pools
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletPools {
	/** Pool for bullets */
	public final static Pool<BulletActor> bullet = new ReflectionPool<BulletActor>(BulletActor.class, 0, Config.Actor.Bullet.BULLETS_MAX);
	/** Pool for time bullets */
	public final static Pool<TimeBullet> timeBullet = new ReflectionPool<TimeBullet>(TimeBullet.class, 0, Config.Actor.Bullet.BULLETS_MAX);
}
