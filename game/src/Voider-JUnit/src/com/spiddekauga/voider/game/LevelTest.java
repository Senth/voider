package com.spiddekauga.voider.game;

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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.SceneStub;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tests the Level implementation. More specifically the write and read to/from
 * json objects. There aren't much more that can be tested in a unit test
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Config.init();
		ResourceSaver.init();
		ResourceNames.useTestPath();
		ResourceCacheFacade.init();
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourceCacheFacade.unload(mScene, mLevelDef, false);
		ResourceCacheFacade.unload(mScene, mPlayerActor.getDef(), false);
		ResourceCacheFacade.unload(mScene, mEnemyDef, false);
		ResourceSaver.clearResources(LevelDef.class);
		ResourceSaver.clearResources(PlayerActorDef.class);

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
		level.setXCoord(-77);
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
	 * Asserts if the bound trigger and enemy aren't the same instance
	 * @param copy the copied/read level
	 */
	private void assertBoundEnemyTriggerSame(Level copy) {
		ArrayList<EnemyActor> enemyActors = copy.getResources(EnemyActor.class);
		ArrayList<TScreenAt> triggers = copy.getResources(TScreenAt.class);

		assertEquals(1, enemyActors.size());
		assertEquals(1, triggers.size());

		EnemyActor enemyActor = enemyActors.get(0);
		TScreenAt trigger = triggers.get(0);

		ArrayList<TriggerInfo> listeners = trigger.getListeners();
		assertEquals(1, listeners.size());
		TriggerInfo listenerInfo = listeners.get(0);

		assertSame(listenerInfo.trigger, trigger);
		assertSame(listenerInfo.listener, enemyActor);
		try {
			assertSame(copy, mfTriggerLevel.get(trigger));
		} catch (Exception e) {
			failWithException(e);
		}

		Pools.arrayList.freeAll(enemyActors, triggers);
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

				// Temporarily remove level
				expected.removeResource(expected.getId());
				actual.removeResource(actual.getId());
				assertEquals(expected.getResources(IResource.class), actual.getResources(IResource.class));
				expected.addResource(expected);
				actual.addResource(actual);
			} else {
				assertEquals(expected, actual);
				assertEquals("LevelDef", expected.getDef(), actual.getDef());

				assertEquals(expected.getResources(IResource.class), actual.getResources(IResource.class));
			}

		} catch (Exception e) {
			failWithException(e);
		}
	}

	/**
	 * Fails the test as an exception was thrown. Prints the exception
	 * @param exception the exception that was thrown
	 */
	private void failWithException(Exception exception) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		fail("Exception thrown!\n" + stringWriter.toString());
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
}
