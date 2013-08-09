package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.JsonWrapper; import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceDependencyLoaderTest;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;

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

		mWorld = new World(new Vector2(), false);
		Actor.setWorld(mWorld);
		mPlayerActor = new PlayerActor();

		ResourceSaver.save(mUsingLevelDef);
		ResourceSaver.save(mPlayerActor.getDef());
		ResourceCacheFacade.load(mPlayerActor.getDef(), false);
		ResourceCacheFacade.load(mUsingLevelDef, false);
		ResourceCacheFacade.finishLoading();

		mfXCoord = Level.class.getDeclaredField("mXCoord");
		mfXCoord.setAccessible(true);
		mfLevelDef = Level.class.getDeclaredField("mLevelDef");
		mfLevelDef.setAccessible(true);
		mfSpeed = Level.class.getDeclaredField("mSpeed");
		mfSpeed.setAccessible(true);
		mfCompletedLevel = Level.class.getDeclaredField("mCompletedLevel");
		mfCompletedLevel.setAccessible(true);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourceDependencyLoaderTest.delete(mUsingLevelDef);
		ResourceDependencyLoaderTest.delete(mPlayerActor.getDef());

		mWorld.dispose();
		Config.dispose();
	}

	/**
	 * Tests to write and read to/from a json object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void writeRead() throws IllegalArgumentException, IllegalAccessException {
		// Empty level
		Level level = new Level(mUsingLevelDef);

		Json json = new JsonWrapper();
		String jsonString = json.toJson(level);
		Level jsonLevel = json.fromJson(Level.class, jsonString);

		assertEquals("uuid", level.getId(), jsonLevel.getId());
		assertEquals("x-coord", 0.0f, mfXCoord.get(jsonLevel));
		assertEquals("level def", mUsingLevelDef, mfLevelDef.get(jsonLevel));
		assertEquals("speed", mfSpeed.get(level), mfSpeed.get(jsonLevel));
		assertEquals("completed level", false, mfCompletedLevel.get(jsonLevel));


		// Test with setting the values to something else
		mfXCoord.set(level, 55.3f);
		mfSpeed.set(level, 0.578f);
		mfCompletedLevel.set(level, true);

		jsonString = json.toJson(level);
		jsonLevel = json.fromJson(Level.class, jsonString);

		assertEquals("uuid", level.getId(), jsonLevel.getId());
		assertEquals("x-coord", 55.3f, mfXCoord.get(jsonLevel));
		assertEquals("level def", mUsingLevelDef, mfLevelDef.get(jsonLevel));
		assertEquals("speed", 0.578f, mfSpeed.get(jsonLevel));
		assertEquals("completed level", true, mfCompletedLevel.get(jsonLevel));
	}

	/** Level definition used for the tests */
	private static LevelDef mUsingLevelDef = new LevelDef();
	/** Player */
	private static PlayerActor mPlayerActor = null;
	/** World used for actors */
	private static World mWorld = null;

	// Fields for testing private members
	/** X-Coord */
	private static Field mfXCoord = null;
	/** Level Def */
	private static Field mfLevelDef = null;
	/** Speed */
	private static Field mfSpeed = null;
	/** Completed level */
	private static Field mfCompletedLevel = null;
}
