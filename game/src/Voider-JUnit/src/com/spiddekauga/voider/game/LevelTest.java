package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Vector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.Config;
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
		Config.init();
		ResourceSaver.init();
		ResourceNames.useTestPath();
		ResourceCacheFacade.init();
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();

		ResourceSaver.save(mUsingLevelDef);
		ResourceCacheFacade.load(mUsingLevelDef, false);
		ResourceCacheFacade.finishLoading();

		mfActors = Level.class.getDeclaredField("mActors");
		mfActors.setAccessible(true);
		mfTriggers = Level.class.getDeclaredField("mTriggers");
		mfTriggers.setAccessible(true);
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
	}

	/**
	 * Tests to write and read to/from a json object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void writeRead() throws IllegalArgumentException, IllegalAccessException {
		// Empty level
		Level level = new Level(mUsingLevelDef);

		Json json = new Json();
		String jsonString = json.toJson(level);
		Level jsonLevel = json.fromJson(Level.class, jsonString);

		assertEquals("uuid", level.getId(), jsonLevel.getId());
		assertEquals("actors", 0, ((Vector<Actor>) mfActors.get(jsonLevel)).size());
		assertEquals("triggers", 0, ((Vector<Trigger>) mfTriggers.get(jsonLevel)).size());
		assertEquals("x-coord", 0.0f, mfXCoord.get(jsonLevel));
		assertEquals("level def", mUsingLevelDef, mfLevelDef.get(jsonLevel));
		assertEquals("speed", mfSpeed.get(level), mfSpeed.get(jsonLevel));
		assertEquals("completed level", false, mfCompletedLevel.get(jsonLevel));


		// Test with setting the values to something else
		((Vector<Actor>) mfActors.get(level)).add(new PlayerActor());
		((Vector<Trigger>) mfTriggers.get(level)).add(new Trigger());
		mfXCoord.set(level, 55.3f);
		mfSpeed.set(level, 0.578f);
		mfCompletedLevel.set(level, true);

		jsonString = json.toJson(level);
		jsonLevel = json.fromJson(Level.class, jsonString);

		assertEquals("uuid", level.getId(), jsonLevel.getId());
		assertEquals("actors", 1, ((Vector<Actor>) mfActors.get(jsonLevel)).size());
		assertEquals("triggers", 1, ((Vector<Trigger>) mfTriggers.get(jsonLevel)).size());
		assertEquals("x-coord", 55.3f, mfXCoord.get(jsonLevel));
		assertEquals("level def", mUsingLevelDef, mfLevelDef.get(jsonLevel));
		assertEquals("speed", 0.578f, mfSpeed.get(jsonLevel));
		assertEquals("completed level", true, mfCompletedLevel.get(jsonLevel));
	}

	/** Level definition used for the tests */
	private static LevelDef mUsingLevelDef = new LevelDef();

	// Fields for testing private members
	/** Actors */
	private static Field mfActors = null;
	/** Triggers */
	private static Field mfTriggers = null;
	/** X-Coord */
	private static Field mfXCoord = null;
	/** Level Def */
	private static Field mfLevelDef = null;
	/** Speed */
	private static Field mfSpeed = null;
	/** Completed level */
	private static Field mfCompletedLevel = null;
}
