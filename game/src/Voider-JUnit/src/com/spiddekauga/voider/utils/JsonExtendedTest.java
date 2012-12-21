package com.spiddekauga.voider.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Test;

import com.badlogic.gdx.utils.Json;

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
	}

}
