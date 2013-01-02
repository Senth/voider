package com.spiddekauga.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.Def;
/**
 * Tests the extended json class
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class JsonTest {
	/**
	 * Tests maps with keys
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void maps() {
		Json json = new Json();

		// String - Integer
		ObjectMap<String, Integer> stringMap = new ObjectMap<String, Integer>();
		stringMap.put("First", 1);
		stringMap.put("Second", 2);
		String jsonString = json.toJson(stringMap);
		ObjectMap<String, Integer> jsonStringMap = json.fromJson(ObjectMap.class, jsonString);
		assertTrue(jsonStringMap.containsKey("First"));
		assertTrue(jsonStringMap.containsKey("Second"));
		assertTrue(jsonStringMap.containsValue(1, false));
		assertTrue(jsonStringMap.containsValue(2, false));


		// Integer - Integer
		ObjectMap<Integer, Integer> intMap = new ObjectMap<Integer, Integer>();
		intMap.put(1, 1);
		intMap.put(2, 2);
		jsonString = json.toJson(intMap);
		ObjectMap<Integer, Integer> jsonIntMap = json.fromJson(ObjectMap.class, jsonString);
		assertTrue(jsonIntMap.containsKey(1));
		assertTrue(jsonIntMap.containsKey(2));
		assertTrue(jsonIntMap.containsValue(1, false));
		assertTrue(jsonIntMap.containsValue(2, false));
	}

	/**
	 * Tests if the extended json can write/read uuid correctly
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void uuid() {
		Json json = new Json();
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


		// Test when used as a key
		ObjectMap<UUID, String> uuidMap = new ObjectMap<UUID, String>();
		testUuid = UUID.randomUUID();
		uuidMap.put(testUuid, "test");
		jsonString = json.toJson(uuidMap);
		ObjectMap<UUID, String> jsonMap = json.fromJson(ObjectMap.class, jsonString);
		assertTrue(jsonMap.containsKey(testUuid));
	}

}
