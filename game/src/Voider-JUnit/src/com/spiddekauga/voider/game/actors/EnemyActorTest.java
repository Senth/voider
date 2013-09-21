package com.spiddekauga.voider.game.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponTest;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
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
			// EnemyActorDef
			mfAimRotateVars = EnemyActorDef.class.getDeclaredField("mAimRotateVars");
			mfAimRotateVars.setAccessible(true);
			mfAiMovementVars = EnemyActorDef.class.getDeclaredField("mAiMovementVars");
			mfAiMovementVars.setAccessible(true);
			mfMovementVars = EnemyActorDef.class.getDeclaredField("mMovementVars");
			mfMovementVars.setAccessible(true);


			// EnemyActor
			mfWeapon = EnemyActor.class.getDeclaredField("mWeapon");
			mfWeapon.setAccessible(true);
			mfShootingAngle = EnemyActor.class.getDeclaredField("mShootingAngle");
			mfShootingAngle.setAccessible(true);
			mfRandomMoveNext = EnemyActor.class.getDeclaredField("mRandomMoveNext");
			mfRandomMoveNext.setAccessible(true);
			mfRandomMoveDirection = EnemyActor.class.getDeclaredField("mRandomMoveDirection");
			mfRandomMoveDirection.setAccessible(true);
			mfPathIndexNext = EnemyActor.class.getDeclaredField("mPathIndexNext");
			mfPathIndexNext.setAccessible(true);
			mfPathForward = EnemyActor.class.getDeclaredField("mPathForward");
			mfPathForward.setAccessible(true);
			mfPathOnceReachedEnd = EnemyActor.class.getDeclaredField("mPathOnceReachedEnd");
			mfPathOnceReachedEnd.setAccessible(true);
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
		enemyDef.getWeaponDef().setBulletSpeed(5);
		enemyDef.setUseWeapon(true);
		enemyDef.setAimType(AimTypes.MOVE_DIRECTION);
		enemyDef.setAimRotateSpeed(1);
		enemyDef.setMovementType(MovementTypes.PATH);
		enemyDef.setSpeed(1);
		enemyDef.setTurnSpeed(15);
		enemyDef.setTurn(true);
		enemyDef.setPlayerDistanceMin(5);
		enemyDef.setPlayerDistanceMax(15);
		enemyDef.setMoveRandomly(true);
		enemyDef.setRandomTimeMin(3);
		enemyDef.setRandomTimeMax(6);
		enemyDef.setStartAngleDeg(90);
		enemyDef.setRotationSpeedDeg(15);


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
		try {

			// Group + leader
			EnemyActorDef enemyActorDef = new EnemyActorDef();
			ActorTest.saveAndLoad(enemyActorDef);
			EnemyActor enemy = new EnemyActor();
			enemy.setDef(enemyActorDef);
			EnemyGroup enemyGroup = new EnemyGroup();
			enemyGroup.setLeaderEnemy(enemy);

			EnemyActor copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();


			// Group + not leader
			ArrayList<EnemyActor> addedEnemies = new ArrayList<EnemyActor>();
			enemyGroup.setEnemyCount(2, addedEnemies, null);
			assertEquals(1, addedEnemies.size());
			copy = addedEnemies.get(0).copy();
			testEnemyEquals(addedEnemies.get(0), copy, true);
			copy.dispose();

			copy = KryoPrototypeTest.copy(addedEnemies.get(0), EnemyActor.class, mKryo);
			testEnemyEquals(addedEnemies.get(0), copy, false);
			copy.dispose();
			enemy.dispose();
			addedEnemies.get(0).dispose();


			// Test AI variables with random movement
			enemy = new EnemyActor();
			enemyActorDef.setMovementType(MovementTypes.AI);
			enemyActorDef.setMoveRandomly(true);
			ActorTest.saveAndLoad(enemyActorDef);
			enemy.setDef(enemyActorDef);
			mfRandomMoveDirection.set(enemy, new Vector2(1,2));
			mfRandomMoveNext.set(enemy, 1.25f);

			copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();


			// Test AI variables without random movement
			enemyActorDef.setMoveRandomly(false);
			ActorTest.saveAndLoad(enemyActorDef);

			// Regular copy sets random movement variables
			copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			// Write/Read does not set random movement variables
			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			mfRandomMoveDirection.set(enemy, new Vector2(0,0));
			mfRandomMoveNext.set(enemy, 0);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();


			// Test Path ONCE
			Path path = new Path();
			path.setPathType(PathTypes.ONCE);
			enemyActorDef.setMovementType(MovementTypes.PATH);
			ActorTest.saveAndLoad(enemyActorDef);
			enemy.setPath(path);
			mfPathIndexNext.set(enemy, 2);
			mfPathForward.set(enemy, false);
			mfPathOnceReachedEnd.set(enemy, true);

			// Regular copy sets all variables
			copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			// Write/Read does not set all
			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			mfPathForward.set(enemy, true);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();


			// Test BACK_AND_FORTH
			path.setPathType(PathTypes.BACK_AND_FORTH);
			mfPathForward.set(enemy, false);

			// Regular copy sets all variables
			copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			// Write/Read does not set all
			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			mfPathOnceReachedEnd.set(enemy, false);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();


			// Test Path LOOP
			path.setPathType(PathTypes.LOOP);
			mfPathOnceReachedEnd.set(enemy, true);

			// Regular copy sets all variables
			copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			// Write/Read does not set all
			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			mfPathOnceReachedEnd.set(enemy, false);
			mfPathForward.set(enemy, true);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();


			// Test weapon has no weapon
			enemyActorDef.dispose();
			enemyActorDef = new EnemyActorDef();
			ActorTest.saveAndLoad(enemyActorDef);
			enemy.dispose();
			enemy = new EnemyActor();
			Weapon weapon = (Weapon) mfWeapon.get(enemy);
			weapon.setPosition(new Vector2(12,213));
			mfShootingAngle.set(enemy, 37);

			// Copy sets all variables
			copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			// Write/Read does not set all variable
			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			weapon.setPosition(new Vector2(0,0));
			mfShootingAngle.set(enemy, 0);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();

			// Test weapon has weapon
			weapon.setPosition(new Vector2(12,213));
			mfShootingAngle.set(enemy, 37);
			enemyActorDef.setUseWeapon(true);
			ActorTest.saveAndLoad(enemyActorDef);

			// Copy sets all variables
			copy = enemy.copy();
			testEnemyEquals(enemy, copy, true);
			copy.dispose();

			// Write/Read should also set all weapon variables now
			copy = KryoPrototypeTest.copy(enemy, EnemyActor.class, mKryo);
			testEnemyEquals(enemy, copy, false);
			copy.dispose();


		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			fail("Exception thrown!\n " + stringWriter.toString());
		}

		ResourceSaver.clearResources(EnemyActor.class);
	}

	/**
	 * Tests if two enemy actors definitions are equal
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
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			fail("Exception thrown!\n " + stringWriter.toString());
		}
	}

	/**
	 * Tests if two enemy actors are equal
	 * @param expected the original actor
	 * @param actual copy or read actor
	 * @param copied true if copied, false if read
	 */
	private static void testEnemyEquals(EnemyActor expected, EnemyActor actual, boolean copied) {
		try {
			assertNotSame(expected, actual);
			if (copied) {
				assertSame(mfWeapon.get(expected), mfWeapon.get(actual));
				assertSame(expected.getEnemyGroup(), actual.getEnemyGroup());
				assertSame(expected.getPath(), actual.getPath());
			} else {
				assertNotSame(mfWeapon.get(expected), mfWeapon.get(actual));
				assertEquals(mfWeapon.get(expected), mfWeapon.get(actual));
				assertNotSame(expected.getEnemyGroup(), actual.getEnemyGroup());
				assertEquals(expected.getEnemyGroup(), actual.getEnemyGroup());
				assertNotSame(expected.getPath(), actual.getPath());
				assertEquals(expected.getPath(), actual.getPath());
			}
			assertEquals(mfShootingAngle.get(expected), mfShootingAngle.get(actual));
			assertEquals(expected.isGroupLeader(), actual.isGroupLeader());
			assertEquals(mfRandomMoveNext.get(expected), mfRandomMoveNext.get(actual));
			assertEquals(mfRandomMoveDirection.get(expected), mfRandomMoveDirection.get(actual));
			assertEquals(mfPathIndexNext.get(expected), mfPathIndexNext.get(actual));
			assertEquals(mfPathForward.get(expected), mfPathForward.get(actual));
			assertEquals(mfPathOnceReachedEnd.get(expected), mfPathOnceReachedEnd.get(actual));

		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			fail("Exception thrown!\n " + stringWriter.toString());
		}
	}

	// EnemyActorDef
	/** Movement variables field */
	private static Field mfMovementVars = null;
	/** Ai movement variables field */
	private static Field mfAiMovementVars = null;
	/** Aim rotate vars */
	private static Field mfAimRotateVars = null;

	// EnemyActor
	/** Weapon field */
	private static Field mfWeapon = null;
	/** Shooting angle field */
	private static Field mfShootingAngle = null;
	/** AI random movement next time */
	private static Field mfRandomMoveNext = null;
	/** AI random movement direction */
	private static Field mfRandomMoveDirection = null;
	/** Next path index field */
	private static Field mfPathIndexNext = null;
	/** Path forward field */
	private static Field mfPathForward = null;
	/** Path reach end field */
	private static Field mfPathOnceReachedEnd = null;

}
