package com.spiddekauga.voider.game.actors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceSaver;

/**
 * Tests for bullets including the definition.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletActorTest extends ActorTest {
	/**
	 * Initialize tests
	 */
	@BeforeClass
	public static void beforeClass() {
		ActorTest.beforeClass();
	}

	/**
	 * Dispose
	 */
	@AfterClass
	public static void afterClass() {
		ActorTest.afterClass();
	}

	@Override
	@Test
	public void testActorDefWriteRead() {
		BulletActorDef bulletDef = new BulletActorDef();
		BulletActorDef copyBulletDef = bulletDef.copy();

		// Nothing to test atm

		bulletDef.dispose();
		copyBulletDef.dispose();
	}

	@Override
	@Test
	public void testActorWriteRead() {
		BulletActor bullet = new BulletActor();
		BulletActorDef bulletDef = new BulletActorDef();
		bullet.setDef(bulletDef);

		ResourceSaver.save(bulletDef);
		ResourceCacheFacade.load(mScene, bulletDef.getId(), false, bulletDef.getRevision());
		ResourceCacheFacade.finishLoading();

		BulletActor copyBullet = KryoPrototypeTest.copy(bullet, BulletActor.class, mKryo);

		bullet.dispose();
		copyBullet.dispose();
		bulletDef.dispose();

		ResourceSaver.clearResources(BulletActorDef.class);
	}

}
