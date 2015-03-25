package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.BoundingBox;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.TimeBullet;

/**
 * Handles alive bullets, and destroys these once they are outside the screen. To avoid
 * checked each bullet every frame, only some are checked each frame. All new bullets
 * shall be added to this BulletDestroyer.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BulletDestroyer implements Disposable {
	/**
	 * Adds a new bullet.
	 * @param bullet a newly created bullet
	 */
	public void add(BulletActor bullet) {
		TimeBullet timeBullet = Pools.timeBullet.obtain();
		timeBullet.bulletActor = bullet;
		timeBullet.time = SceneSwitcher.getGameTime().getTotalTimeElapsed();

		mBullets.add(timeBullet);
	}

	/**
	 * Updates the bullets
	 * @param deltaTime elapsed time since last frame
	 */
	public void update(float deltaTime) {
		for (TimeBullet timeBullet : mBullets) {
			timeBullet.bulletActor.update(deltaTime);
		}
	}

	/**
	 * Renders all the bullets
	 * @param shapeRenderer shape renderer to be used for rendering
	 * @param windowBox position of the window
	 */
	public void render(ShapeRendererEx shapeRenderer, BoundingBox windowBox) {
		for (TimeBullet timeBullet : mBullets) {
			if (windowBox.overlaps(timeBullet.bulletActor.getBoundingBox())) {
				timeBullet.bulletActor.renderShape(shapeRenderer);
			}
		}
	}

	/**
	 * @return a copied list of all bullets
	 */
	public ArrayList<BulletActor> getBullets() {
		ArrayList<BulletActor> bullets = new ArrayList<>();

		for (TimeBullet timeBullet : mBullets) {
			bullets.add(timeBullet.bulletActor);
		}

		return bullets;
	}

	/**
	 * Removes out of bounds/screen bullets. This function does not remove all bullets, as
	 * it only checks a bullet every x seconds. Defined in
	 * Config.Actor.Bullet.CHECK_OUT_OF_BOUNDS_TIME.
	 * @note because some bullets might come on to the screen again bullets it will
	 *       enlarge the minPos and maxPos by 3 times. I.e. the screen size is the XX
	 *       size. \code __________ |__|__|__| |__|XX|__| |__|__|__| \endCode
	 * @param boundingBox everything inside this bounding box will be kept. Also read the
	 *        note.
	 */
	public void removeOutOfBondsBullets(BoundingBox boundingBox) {
		float elapsedTime = SceneSwitcher.getGameTime().getTotalTimeElapsed();

		BoundingBox enlargedBox = new BoundingBox(boundingBox);
		enlargedBox.scale(3);

		for (int i = 0; i < mBullets.size(); ++i) {
			// Only check out of bounds it some time has elapsed since last check
			if (!mBullets.get(i).bulletActor.isActive() || mBullets.get(i).time + Config.Actor.Bullet.CHECK_OUT_OF_BOUNDS_TIME <= elapsedTime) {
				mBullets.get(i).time = elapsedTime;

				boolean outOfBounds = false;
				BulletActor bulletActor = mBullets.get(i).bulletActor;
				if (bulletActor.isActive()) {

					if (!enlargedBox.overlaps(bulletActor.getBoundingBox())) {
						outOfBounds = true;
					}
				}
				// Bullet has been destroyed, probably by hitting something
				else {
					outOfBounds = true;
				}

				if (outOfBounds) {
					TimeBullet removeTimeBullet = mBullets.remove(i);
					i--;

					freeBullet(removeTimeBullet);
				}
			}
		}
	}

	@Override
	public void dispose() {
		for (TimeBullet timeBullet : mBullets) {
			freeBullet(timeBullet);
		}
	}

	/**
	 * Destroys and frees a bullet
	 * @param timeBullet the time bullet to free and destroy
	 */
	private void freeBullet(TimeBullet timeBullet) {
		timeBullet.bulletActor.destroyBody();
		Pools.bullet.free(timeBullet.bulletActor);
		Pools.timeBullet.free(timeBullet);
	}

	/** All alive bullets */
	@Tag(85) private ArrayList<TimeBullet> mBullets = new ArrayList<TimeBullet>();
}
