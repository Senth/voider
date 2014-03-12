package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.repo.ApplicationStub;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.SceneStub;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tests the Level implementation. More specifically the write and read to/from
 * json objects. There aren't much more that can be tested in a unit test
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LevelTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Gdx.app = new ApplicationStub();
		Config.Debug.JUNIT_TEST = true;
		SceneSwitcher.switchTo(mScene);

		mWorld = new World(new Vector2(), false);
		Actor.setWorld(mWorld);
		mPlayerActor = new PlayerActor();
		mEnemyDef = new EnemyActorDef();

		ResourceSaver.save(mLevelDef);
		ResourceSaver.save(mPlayerActor.getDef());
		ResourceSaver.save(mEnemyDef);
		ResourceCacheFacade.load(mScene, mPlayerActor.getDef().getId(), false, mPlayerActor.getDef().getRevision());
		ResourceCacheFacade.load(mScene, mLevelDef.getId(), false, mLevelDef.getRevision());
		ResourceCacheFacade.load(mScene, mEnemyDef.getId(), false, mEnemyDef.getRevision());
		ResourceCacheFacade.finishLoading();

		mfLevelDef = Level.class.getDeclaredField("mLevelDef");
		mfLevelDef.setAccessible(true);
		mfCompletedLevel = Level.class.getDeclaredField("mCompletedLevel");
		mfCompletedLevel.setAccessible(true);
		mfTriggerLevel = TScreenAt.class.getDeclaredField("mLevel");
		mfTriggerLevel.setAccessible(true);
		mfTriggerListeners = Trigger.class.getDeclaredField("mListeners");
		mfTriggerListeners.setAccessible(true);
		mfGroupEnemies = EnemyGroup.class.getDeclaredField("mEnemies");
		mfGroupEnemies.setAccessible(true);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourceSaver.clearResources();
		ResourceCacheFacade.unload(mScene);

		mEnemyDef.dispose();
		mPlayerActor.dispose();

		Pools.kryo.free(mKryo);

		mWorld.dispose();
		Config.dispose();
	}

	/**
	 * Tests to copy, write, and read an empty level
	 */
	@Test
	public void testCopyEmptyLevel() {
		Level level = new Level(mLevelDef);

		// Copy
		Level copy = level.copy();
		assertLevelEquals(level, copy, false);
		copy.dispose();

		// Copy new resource
		copy = level.copyNewResource();
		assertLevelEquals(level, copy, true);
		copy.dispose();

		// Write/Read
		copy = KryoPrototypeTest.copy(level, Level.class, mKryo);
		assertLevelEquals(level, copy, false);
		copy.dispose();

		level.dispose();
	}

	/**
	 * Test level with the variables set, but no resources
	 */
	@Test
	public void testCopyLevel() {
		Level level = new Level(mLevelDef);
		level.setPlayer(mPlayerActor);
		level.setSpeed(15);
		level.setStartPosition(-77);
		try {
			mfCompletedLevel.set(level, true);
			mfLevelDef.set(level, mLevelDef);
		} catch (Exception e) {
			failWithException(e);
		}

		// Copy
		Level copy = level.copy();
		assertLevelEquals(level, copy, false);
		copy.dispose();

		// Copy new resource
		copy = level.copyNewResource();
		assertLevelEquals(level, copy, true);
		copy.dispose();

		// Write/Read
		copy = KryoPrototypeTest.copy(level, Level.class, mKryo);
		assertLevelEquals(level, copy, false);
		copy.dispose();

		level.dispose();
	}

	/**
	 * Test to see if the resources are bound correctly with each other.
	 * Do this with trigger and enemy
	 */
	@Test
	public void testCopyEnemyTriggerBound() {
		Level level = new Level(mLevelDef);

		EnemyActor enemy = new EnemyActor();
		enemy.setDef(mEnemyDef);

		Trigger trigger = new TScreenAt(level, 15);
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.action = Actions.ACTOR_ACTIVATE;
		triggerInfo.delay = 15;
		triggerInfo.trigger = trigger;
		triggerInfo.listener = enemy;
		trigger.addListener(triggerInfo);

		level.addResource(enemy);
		level.addResource(trigger);


		// Copy
		Level copy = level.copy();
		assertLevelEquals(level, copy, false);
		assertBoundEnemyTriggerSame(copy);
		copy.dispose();

		// Copy new resource
		copy = level.copyNewResource();
		assertLevelEquals(level, copy, true);
		assertBoundEnemyTriggerSame(copy);
		copy.dispose();

		// Write/Read
		copy = KryoPrototypeTest.copy(level, Level.class, mKryo);
		assertLevelEquals(level, copy, false);
		assertBoundEnemyTriggerSame(copy);
		copy.dispose();
	}

	/**
	 * Test with TScreen
	 */
	@Test
	public void testTScreenAt() {
		Level level = new Level(mLevelDef);

		TScreenAt tScreenAt = new TScreenAt(level, 10);
		level.addResource(tScreenAt);

		// Copy
		Level copy = level.copy();
		assertLevelEquals(level, copy, false);
		assertTScreenAt(level, copy, false);
		copy.dispose();

		// Copy new resource
		copy = level.copyNewResource();
		assertLevelEquals(level, copy, true);
		assertTScreenAt(level, copy, true);
		copy.dispose();

		// Write/Read
		copy = KryoPrototypeTest.copy(level, Level.class, mKryo);
		assertLevelEquals(level, copy, false);
		assertTScreenAt(level, copy, false);
		copy.dispose();
	}

	/**
	 * Test EnemyGroup
	 */
	@Test
	public void testEnemyGroup() {
		Level level = new Level(mLevelDef);

		EnemyActor enemyActor = new EnemyActor();
		enemyActor.setDef(mEnemyDef);
		EnemyGroup enemyGroup = new EnemyGroup();
		enemyGroup.setLeaderEnemy(enemyActor);

		level.addResource(enemyActor);
		level.addResource(enemyGroup);

		@SuppressWarnings("unchecked")
		ArrayList<EnemyActor> addedEnemies = Pools.arrayList.obtain();
		addedEnemies.clear();
		enemyGroup.setEnemyCount(10, addedEnemies, null);
		level.addResource(addedEnemies);
		Pools.arrayList.free(addedEnemies);


		// Copy
		Level copy = level.copy();
		assertLevelEquals(level, copy, false);
		assertEnemyGroup(level, copy);
		copy.dispose();

		// Copy new resource
		copy = level.copyNewResource();
		assertLevelEquals(level, copy, true);
		assertEnemyGroup(level, copy);
		copy.dispose();

		// Write/Read
		copy = KryoPrototypeTest.copy(level, Level.class, mKryo);
		assertLevelEquals(level, copy, false);
		assertEnemyGroup(level, copy);
		copy.dispose();
	}

	/**
	 * Test EnemyActor + TScreenAt + EnemyGroup
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testEnemyGroupTScreenAt() {
		Level level = new Level(mLevelDef);

		EnemyActor enemyActor = new EnemyActor();
		enemyActor.setDef(mEnemyDef);

		TScreenAt triggerActivate = new TScreenAt(level, 15);
		TriggerInfo triggerInfoActivate = new TriggerInfo();
		triggerInfoActivate.trigger = triggerActivate;
		triggerInfoActivate.action = Actions.ACTOR_ACTIVATE;
		triggerInfoActivate.listener = enemyActor;
		enemyActor.addTrigger(triggerInfoActivate);
		triggerActivate.addListener(triggerInfoActivate);

		TScreenAt triggerDeactivate = new TScreenAt(level, 20);
		TriggerInfo triggerInfoDeactivate = new TriggerInfo();
		triggerInfoDeactivate.trigger = triggerDeactivate;
		triggerInfoDeactivate.action = Actions.ACTOR_DEACTIVATE;
		triggerInfoDeactivate.listener = enemyActor;
		enemyActor.addTrigger(triggerInfoDeactivate);
		triggerDeactivate.addListener(triggerInfoDeactivate);

		EnemyGroup enemyGroup = new EnemyGroup();
		enemyGroup.setLeaderEnemy(enemyActor);

		level.addResource(triggerActivate, triggerDeactivate, enemyActor, enemyGroup);

		ArrayList<EnemyActor> addedEnemies = Pools.arrayList.obtain();
		addedEnemies.clear();
		enemyGroup.setEnemyCount(3, addedEnemies, null);
		level.addResource(addedEnemies);
		Pools.arrayList.free(addedEnemies);

		assertEnemyGroupTriggers(level);

		ArrayList<EnemyActor> removedEnemies = Pools.arrayList.obtain();
		removedEnemies.clear();
		enemyGroup.setEnemyCount(2, null, removedEnemies);
		level.removeResource(removedEnemies);

		assertEnemyGroupTriggers(level);

		addedEnemies = Pools.arrayList.obtain();
		addedEnemies.clear();
		enemyGroup.setEnemyCount(4, addedEnemies, null);
		level.addResource(addedEnemies);
		Pools.arrayList.free(addedEnemies);

		assertEnemyGroupTriggers(level);
	}

	/**
	 * Test with same activate/deactivate trigger
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSameActivateDeactivateTrigger() {
		Level level = new Level(mLevelDef);

		EnemyActor enemyActor = new EnemyActor();
		enemyActor.setDef(mEnemyDef);

		TScreenAt trigger = new TScreenAt(level, 15);
		TriggerInfo triggerInfoActivate = new TriggerInfo();
		triggerInfoActivate.trigger = trigger;
		triggerInfoActivate.action = Actions.ACTOR_ACTIVATE;
		triggerInfoActivate.listener = enemyActor;
		enemyActor.addTrigger(triggerInfoActivate);

		TriggerInfo triggerInfoDeactivate = triggerInfoActivate.copy();
		triggerInfoDeactivate.action = Actions.ACTOR_DEACTIVATE;
		enemyActor.addTrigger(triggerInfoDeactivate);

		EnemyGroup enemyGroup = new EnemyGroup();
		enemyGroup.setLeaderEnemy(enemyActor);

		level.addResource(trigger, enemyActor, enemyGroup);

		ArrayList<EnemyActor> addedEnemies = Pools.arrayList.obtain();
		addedEnemies.clear();
		enemyGroup.setEnemyCount(3, addedEnemies, null);
		level.addResource(addedEnemies);
		Pools.arrayList.free(addedEnemies);

		assertEnemyGroupTriggers(level);

		ArrayList<EnemyActor> removedEnemies = Pools.arrayList.obtain();
		removedEnemies.clear();
		enemyGroup.setEnemyCount(2, null, removedEnemies);
		level.removeResource(removedEnemies);

		assertEnemyGroupTriggers(level);

		addedEnemies = Pools.arrayList.obtain();
		addedEnemies.clear();
		enemyGroup.setEnemyCount(4, addedEnemies, null);
		level.addResource(addedEnemies);
		Pools.arrayList.free(addedEnemies);

		assertEnemyGroupTriggers(level);
	}

	/**
	 * Assert enemy group triggers
	 * @param level the level to create copies of and test
	 */
	private void assertEnemyGroupTriggers(Level level) {
		assertBoundEnemyTriggerSame(level);

		// Copy
		Level copy = level.copy();
		assertLevelEquals(level, copy, false);
		assertEnemyGroup(level, copy);
		assertTScreenAt(level, copy, false);
		assertBoundEnemyTriggerSame(copy);
		copy.dispose();

		// Copy new resource
		copy = level.copyNewResource();
		assertLevelEquals(level, copy, true);
		assertEnemyGroup(level, copy);
		assertTScreenAt(level, copy, true);
		assertBoundEnemyTriggerSame(copy);
		copy.dispose();

		// Write/Read
		copy = KryoPrototypeTest.copy(level, Level.class, mKryo);
		assertLevelEquals(level, copy, false);
		assertEnemyGroup(level, copy);
		assertTScreenAt(level, copy, false);
		assertBoundEnemyTriggerSame(copy);
		copy.dispose();
	}


	/**
	 * Assertion tests for EnemyGroup
	 * @param expected original level
	 * @param actual copy/read level
	 */
	@SuppressWarnings("unchecked")
	private void assertEnemyGroup(Level expected, Level actual) {
		ArrayList<EnemyGroup> groupsExpected = expected.getResources(EnemyGroup.class);
		ArrayList<EnemyGroup> groupsActual = actual.getResources(EnemyGroup.class);
		ArrayList<EnemyActor> enemiesExpected = expected.getResources(EnemyActor.class);
		ArrayList<EnemyActor> enemiesActual = actual.getResources(EnemyActor.class);

		assertEquals(groupsExpected.size(), groupsActual.size());
		assertEquals(enemiesExpected.size(), enemiesActual.size());

		Exception failException = null;
		try {
			for (int groupIndex = 0; groupIndex < groupsExpected.size(); ++groupIndex) {
				EnemyGroup groupExpected = groupsExpected.get(groupIndex);
				EnemyGroup groupActual = groupsActual.get(groupIndex);

				assertNotSame(groupExpected, groupActual);
				assertEquals(groupExpected, groupActual);

				ArrayList<EnemyActor> groupEnemiesExpected = (ArrayList<EnemyActor>) mfGroupEnemies.get(groupExpected);
				ArrayList<EnemyActor> groupEnemiesActual = (ArrayList<EnemyActor>) mfGroupEnemies.get(groupActual);

				assertNotSame(groupEnemiesExpected, groupEnemiesActual);
				assertEquals(groupEnemiesExpected.size(), groupEnemiesActual.size());

				// Enemies equals in group?
				for (int enemyIndex = 0; enemyIndex < groupEnemiesExpected.size(); ++enemyIndex) {
					EnemyActor enemyExpected = groupEnemiesExpected.get(enemyIndex);
					EnemyActor enemyActual = groupEnemiesActual.get(enemyIndex);

					assertNotSame(enemyExpected, enemyActual);
				}

				// Search for enemy in level
				assertTrue(enemiesExpected.containsAll(groupEnemiesExpected));
				assertTrue(enemiesActual.containsAll(groupEnemiesActual));
			}
		} catch (Exception e) {
			failException = e;
		}

		Pools.arrayList.freeAll(groupsExpected, groupsActual, enemiesExpected, enemiesActual);

		failWithException(failException);
	}

	/**
	 * Asserts if TScreenAt is the same in both levels, and other things inside the TScreenAt
	 * does not match
	 * @param expected original level
	 * @param actual copy/read level
	 * @param newResource set to true if the copy is a new resource
	 */
	@SuppressWarnings("unchecked")
	private void assertTScreenAt(Level expected, Level actual, boolean newResource) {
		ArrayList<TScreenAt> tScreenAtsExpected = expected.getResources(TScreenAt.class);
		ArrayList<TScreenAt> tScreenAtsActual = actual.getResources(TScreenAt.class);

		assertEquals(tScreenAtsExpected.size(), tScreenAtsActual.size());

		Exception failedException = null;
		try {
			for (int triggerIndex = 0; triggerIndex < tScreenAtsExpected.size(); ++triggerIndex) {
				TScreenAt triggerExpected = tScreenAtsExpected.get(triggerIndex);
				TScreenAt triggerActual = tScreenAtsActual.get(triggerIndex);

				assertNotSame(triggerExpected, triggerActual);

				// Test level
				if (newResource) {
					assertFalse(mfTriggerLevel.get(triggerExpected).equals(mfTriggerLevel.get(triggerActual)));
				} else {
					assertEquals(mfTriggerLevel.get(triggerExpected), mfTriggerLevel.get(triggerActual));
				}

				// Test listeners
				ArrayList<TriggerInfo> listenersExpected = (ArrayList<TriggerInfo>) mfTriggerListeners.get(triggerExpected);
				ArrayList<TriggerInfo> listenersActual  = (ArrayList<TriggerInfo>) mfTriggerListeners.get(triggerActual);

				assertNotSame(listenersExpected, listenersActual );
				assertEquals(listenersExpected.size(), listenersActual .size());

				for (int listenerIndex = 0; listenerIndex < listenersExpected.size(); ++listenerIndex) {
					TriggerInfo triggerInfoExpected = listenersExpected.get(listenerIndex);
					TriggerInfo triggerInfoActual = listenersActual.get(listenerIndex);
					assertNotSame(triggerInfoExpected, triggerInfoActual);
					assertNotSame(triggerInfoExpected.trigger, triggerInfoActual.trigger);
					assertEquals(triggerInfoExpected.trigger, triggerInfoActual.trigger);
					assertNotSame(triggerInfoExpected.listener, triggerInfoActual.listener);
					assertEquals(triggerInfoExpected.listener, triggerInfoActual.listener);
				}
			}
		} catch (Exception e) {
			failedException = e;
		}

		Pools.arrayList.freeAll(tScreenAtsExpected, tScreenAtsActual);

		failWithException(failedException);
	}

	/**
	 * Asserts if the bound trigger and enemy aren't the same instance
	 * @param copy the copied/read level
	 */
	private void assertBoundEnemyTriggerSame(Level copy) {
		ArrayList<EnemyActor> enemies = copy.getResources(EnemyActor.class);
		ArrayList<TScreenAt> triggers = copy.getResources(TScreenAt.class);


		for (EnemyActor enemy : enemies) {
			// Triggers should contain all triggerInfo triggers, this checks equals
			for (TriggerInfo triggerInfo : enemy.getTriggerInfos()) {
				assertTrue(triggers.contains(triggerInfo.trigger));

				// Check so they are same
				for (Trigger trigger : triggers) {
					if (trigger.equals(triggerInfo.trigger)) {
						assertSame(trigger, triggerInfo.trigger);
					}
				}
			}
		}

		// Triggers' listeners should be same as enemies
		for (TScreenAt trigger : triggers) {
			for (TriggerInfo triggerInfo : trigger.getListeners()) {
				assertTrue(enemies.contains(triggerInfo.listener));

				// Check so they are same
				for (EnemyActor enemy : enemies) {
					if (enemy.equals(triggerInfo.listener)) {
						assertSame(enemy, triggerInfo.listener);
					}
				}
			}

			// Check so level trigger is same
			try {
				assertSame(copy, mfTriggerLevel.get(trigger));
			} catch (Exception e) {
				failWithException(e);
			}
		}

		Pools.arrayList.freeAll(enemies, triggers);
	}

	/**
	 * Tests if two levels are equal and if all the actors are bound correctly
	 * @param expected the original level
	 * @param actual the copied/read level
	 * @param newResource true if the "actual" level was copied using copyNewResource()
	 */
	private void assertLevelEquals(Level expected, Level actual, boolean newResource) {
		try {
			assertNotSame(expected, actual);
			assertEquals("mXCoord", expected.getXCoord(), actual.getXCoord(), 0);
			assertEquals("mSpeed", expected.getSpeed(), actual.getSpeed(), 0);
			assertEquals("mCompletedLevel", expected.isCompletedLevel(), actual.isCompletedLevel());



			if (newResource) {
				assertFalse(expected.equals(actual));
				assertFalse("LevelDef", expected.getDef().equals(actual.getDef()));
			} else {
				assertEquals(expected, actual);
				assertEquals("LevelDef", expected.getDef(), actual.getDef());
			}

			ArrayList<IResource> resourcesExpected = expected.getResources(IResource.class);
			ArrayList<IResource> resourcesActual = actual.getResources(IResource.class);

			assertEquals(resourcesExpected.size(), resourcesActual.size());
			assertTrue(resourcesExpected.containsAll(resourcesActual));

			// Test so they don't include same reference
			for (IResource resourceExpected : resourcesExpected) {
				for (IResource resourceActual : resourcesActual) {
					assertNotSame(resourceExpected, resourceActual);
				}
			}

			Pools.arrayList.freeAll(resourcesActual, resourcesExpected);

		} catch (Exception e) {
			failWithException(e);
		}
	}

	/**
	 * Fails the test as an exception was thrown. Prints the exception
	 * @param exception the exception that was thrown
	 */
	private void failWithException(Exception exception) {
		if (exception != null) {
			fail("Exception thrown!\n" + Strings.stackTraceToString(exception));
		}
	}


	/** Level definition used for the tests */
	private static LevelDef mLevelDef = new LevelDef();
	/** Player */
	private static PlayerActor mPlayerActor = null;
	/** Enemy def used */
	private static EnemyActorDef mEnemyDef = null;
	/** World used for actors */
	private static World mWorld = null;
	/** Kryo used for writing and reading */
	private static Kryo mKryo = Pools.kryo.obtain();
	/** Scene used for loading/unloading */
	private static SceneStub mScene = new SceneStub();

	// Fields for testing private members
	/** Level Def */
	private static Field mfLevelDef = null;
	/** Completed level */
	private static Field mfCompletedLevel = null;
	/** Level bound in trigger */
	private static Field mfTriggerLevel = null;
	/** Trigger listeners */
	private static Field mfTriggerListeners = null;
	/** Group enemies */
	private static Field mfGroupEnemies = null;
}
