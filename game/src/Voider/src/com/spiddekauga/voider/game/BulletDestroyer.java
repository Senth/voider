package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.TimeBullet;

/**
 * Handles alive bullets, and destroys these once they are outside the screen.
 * To avoid checked each bullet every frame, only some are checked each frame.
 * All new bullets shall be added to this BulletDestroyer.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletDestroyer implements Disposable {
	/**
	 * Adds a new bullet.
	 * @param bullet a newly created bullet
	 */
	public void add(BulletActor bullet) {
		TimeBullet timeBullet = BulletPools.timeBullet.obtain();
		timeBullet.bulletActor = bullet;
		timeBullet.time = SceneSwitcher.getGameTime().getTotalTimeElapsed();

		mBullets.add(timeBullet);
	}

	/**
	 * Removes out of bounds/screen bullets. This function does not remove
	 * all bullets, as it only checks a bullet every x seconds. Defined
	 * in Config.Actor.Bullet.CHECK_OUT_OF_BOUNDS_TIME.
	 * @note because some bullets might come on to the screen again bullets
	 * it will enlargen the minPos and maxPos by 3 times. I.e. the screen size
	 * is the XX size.
	 * \code
	 * __________
	 * |__|__|__|
	 * |__|XX|__|
	 * |__|__|__|
	 * \endCode
	 * @param minPos minimum position of the screen (lower left corner). This
	 * variable will be changed!
	 * @param maxPos maximum position of the screen (upper right corner). This
	 * variable will be changed!
	 */
	public void removeOutOfBondsBullets(Vector2 minPos, Vector2 maxPos) {
		float elapsedTime = SceneSwitcher.getGameTime().getTotalTimeElapsed();

		Vector2 diffVector = Pools.obtain(Vector2.class);
		diffVector.set(maxPos).sub(minPos);
		minPos.sub(diffVector);
		maxPos.add(diffVector);

		for (int i = 0; i < mBullets.size(); ++i) {
			// Only check out of bounds it some time has elapsed since last check
			if (mBullets.get(i).time + Config.Actor.Bullet.CHECK_OUT_OF_BOUNDS_TIME <= elapsedTime) {
				mBullets.get(i).time = elapsedTime;

				// Is the bullet out of bounds
				Vector2 position = mBullets.get(i).bulletActor.getBody().getPosition();
				boolean outOfBounds = false;
				// LEFT
				if (position.x < minPos.x) {
					outOfBounds = true;
				}
				// RIGHT
				else if (position.x > maxPos.x) {
					outOfBounds = true;
				}
				// BOTTOM
				else if (position.y < minPos.y) {
					outOfBounds = true;
				}
				// TOP
				else if (position.y > maxPos.y) {
					outOfBounds = true;
				}

				if (outOfBounds) {
					TimeBullet removeTimeBullet = mBullets.remove(i);
					i--;

					freeBullet(removeTimeBullet.bulletActor);
				}
			}
		}

		Pools.free(diffVector);
	}

	@Override
	public void dispose() {
		for (TimeBullet timeBullet : mBullets) {
			freeBullet(timeBullet.bulletActor);
		}
	}

	/**
	 * Destroys and frees a bullet
	 * @param bullet the bullet to destroy and free
	 */
	private void freeBullet(BulletActor bullet) {
		bullet.destroyBody();
		BulletPools.bullet.free(bullet);
	}

	/** All alive bullets */
	private ArrayList<TimeBullet> mBullets = new ArrayList<TimeBullet>();
}
