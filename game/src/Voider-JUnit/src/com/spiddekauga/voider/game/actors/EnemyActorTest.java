package com.spiddekauga.voider.game.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.game.WeaponTest;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceSaver;

/**
 * Tests enemy actor and definition
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyActorTest extends ActorTest {
	/**
	 * Initialize tests
	 */
	@BeforeClass
	public static void beforeClass() {
		ActorTest.beforeClass();

		try {
			mfAimRotateVars = EnemyActorDef.class.getDeclaredField("mAimRotateVars");
			mfAimRotateVars.setAccessible(true);
			mfAiMovementVars = EnemyActorDef.class.getDeclaredField("mAiMovementVars");
			mfAiMovementVars.setAccessible(true);
			mfMovementVars = EnemyActorDef.class.getDeclaredField("mMovementVars");
			mfMovementVars.setAccessible(true);
		} catch (Exception e) {
			// Does nothing
		}
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
		EnemyActorDef enemyDef = new EnemyActorDef();
		enemyDef.setUseWeapon(true);
		enemyDef.setAimType(AimTypes.MOVE_DIRECTION);
		enemyDef.setAimRotateSpeed(1);
		enemyDef.setMovementType(MovementTypes.PATH);
		enemyDef.setMoveRandomly(true);


		// Regular copy
		EnemyActorDef copyEnemyDef = enemyDef.copy();
		testEnemyDefEquals(enemyDef, copyEnemyDef);
		copyEnemyDef.dispose();

		// New def
		copyEnemyDef = enemyDef.copyNewResource();
		testEnemyDefEquals(enemyDef, copyEnemyDef);
		assertFalse(enemyDef.equals(copyEnemyDef));
		assertFalse(enemyDef.getId().equals(copyEnemyDef.getId()));
		copyEnemyDef.dispose();

		// Read write
		copyEnemyDef = KryoPrototypeTest.copy(enemyDef, EnemyActorDef.class, mKryo);
		testEnemyDefEquals(enemyDef, copyEnemyDef);
		copyEnemyDef.dispose();
	}

	@Override
	@Test
	public void testActorWriteRead() {
		BulletActor bullet = new BulletActor();
		BulletActorDef bulletDef = new BulletActorDef();
		bullet.setDef(bulletDef);

		ResourceSaver.save(bulletDef);
		ResourceCacheFacade.load(mScene, bulletDef.getId(), BulletActorDef.class, bulletDef.getRevision(), false);
		ResourceCacheFacade.finishLoading();

		BulletActor copyBullet = KryoPrototypeTest.copy(bullet, BulletActor.class, mKryo);

		bullet.dispose();
		copyBullet.dispose();
		bulletDef.dispose();

		ResourceSaver.clearResources(BulletActorDef.class);
	}

	/**
	 * Tests if two enemy actors are equal
	 * @param expected expected result
	 * @param actual the actual result
	 */
	private static void testEnemyDefEquals(EnemyActorDef expected, EnemyActorDef actual) {
		assertNotSame(expected, actual);
		assertEquals(expected.hasWeapon(), actual.hasWeapon());
		WeaponTest.testWeaponDefEquals(expected.getWeaponDef(), actual.getWeaponDef());
		assertEquals(expected.getAimType(), actual.getAimType());
		try {
			assertEquals(mfAimRotateVars.get(expected), mfAimRotateVars.get(actual));
			assertEquals(mfAiMovementVars.get(expected), mfAiMovementVars.get(actual));
			assertEquals(mfMovementVars.get(expected), mfMovementVars.get(actual));
		} catch (Exception e) {
			fail("Exception error!");
		}
	}

	/** Movement variables field */
	private static Field mfMovementVars = null;
	/** Ai movement variables field */
	private static Field mfAiMovementVars = null;
	/** Aim rotate vars */
	private static Field mfAimRotateVars = null;
}
