package com.spiddekauga.voider.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Test;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.Def;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class JsonExtendedTest {

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Tests if the extended json can write/read uuid correctly
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void uuid() {
		Json json = new JsonExtended();
		String jsonString = null;
		UUID testUuid = UUID.randomUUID();
		try {
			jsonString = json.toJson(testUuid);
			assertTrue("Could write uuid to json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not write uuid to json");
		}

		UUID jsonUuid = null;
		try {
			jsonUuid = json.fromJson(UUID.class, jsonString);
			assertTrue("Could read uuid from json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read uuid from json");
		}

		assertEquals("Same uuid", testUuid, jsonUuid);


		// Test null
		jsonString = null;
		testUuid = null;
		try {
			jsonString = json.toJson(testUuid);
			assertTrue("Could write null uuid to json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not write null uuid to json");
		}

		jsonUuid = null;
		try {
			jsonUuid = json.fromJson(UUID.class, jsonString);
			assertTrue("Could read null uuid from json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read null uuid from json");
		}

		assertNull("UUID was null", jsonUuid);


		Array<Def> testArray = new Array<Def>();
		testArray.add(new LevelDef());
		jsonString = json.toJson(testArray);


		// Test when inside a collection
		Array<UUID> uuidArray = new Array<UUID>();
		uuidArray.add(UUID.randomUUID());
		jsonString = json.toJson(uuidArray);
		Array<UUID> jsonArray = json.fromJson(Array.class, jsonString);
		assertEquals("Array the same", uuidArray.get(0), jsonArray.get(0));
	}

}
