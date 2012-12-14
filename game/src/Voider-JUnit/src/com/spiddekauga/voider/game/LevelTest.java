package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.Config;
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

		ResourceSaver.save(mLevelDef);
		ResourceCacheFacade.load(mLevelDef, false);
		ResourceCacheFacade.finishLoading();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourceDependencyLoaderTest.delete(mLevelDef);
	}

	/**
	 * Tests to write and read to/from a json object
	 */
	@Test
	public void writeRead() {
		Level level = new Level(mLevelDef);

		Json json = new Json();
		String jsonString = json.toJson(level);
		json.prettyPrint(jsonString);
		Level jsonLevel = json.fromJson(Level.class, jsonString);

		assertEquals("level ids equal", level.getId(), jsonLevel.getId());

		// TODO test more variables
	}

	/** Level definition used for the tests */
	private static LevelDef mLevelDef = new LevelDef();
}
