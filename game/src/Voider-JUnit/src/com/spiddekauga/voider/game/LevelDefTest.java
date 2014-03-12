package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tests the LevelDef for bugs
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LevelDefTest {
	/**
	 * Test to write/read the LevelDef to/from Kryo.
	 * Tests to write and read to/from a Json object
	 */
	@Test
	public void writeRead() {
		Kryo kryo = Pools.kryo.obtain();

		LevelDef def = new LevelDef();
		def.setCampaignId(UUID.randomUUID());
		def.setPrologue("story before");
		def.setStoryAfter("story after");
		def.setEndXCoord(555.025f);
		def.setRevision(1);
		def.setBaseSpeed(15);

		// Copy
		LevelDef copy = def.copy();
		assertLevelDefEquals(def, copy, false);

		// New definition copy
		copy = def.copyNewResource();
		assertLevelDefEquals(def, copy, true);

		// Write/Read
		copy = KryoPrototypeTest.copy(def, LevelDef.class, kryo);
		assertLevelDefEquals(def, copy, false);


		// Test with an empty level def
		def = new LevelDef();

		// Copy
		copy = def.copy();
		assertLevelDefEquals(def, copy, false);

		// New definition copy
		copy = def.copyNewResource();
		assertLevelDefEquals(def, copy, true);

		// Write/Read
		copy = KryoPrototypeTest.copy(def, LevelDef.class, kryo);
		assertLevelDefEquals(def, copy, false);


		Pools.kryo.free(kryo);
	}

	/**
	 * Tests if two definitions are equal
	 * @param expected the original definition
	 * @param actual the copied/read definition
	 * @param newResource if the actual was created using copyNewResource()
	 */
	private void assertLevelDefEquals(LevelDef expected, LevelDef actual, boolean newResource) {
		assertNotSame(expected, actual);
		assertEquals(expected.getPrologue(), actual.getPrologue());
		assertEquals(expected.getEpilogue(), actual.getEpilogue());
		assertEquals("end x coord", expected.getEndXCoord(), actual.getEndXCoord(), 0.0f);
		assertEquals("revision", expected.getRevision(), actual.getRevision());
		assertEquals("base speed", expected.getBaseSpeed(), actual.getBaseSpeed(), 0.0f);

		if (newResource) {
			assertNull(actual.getCampaignId());
			assertFalse(expected.getLevelId().equals(actual.getLevelId()));
			assertFalse(expected.equals(actual));
		} else {
			assertEquals("campaign id", expected.getCampaignId(), actual.getCampaignId());
			assertEquals("level id", expected.getLevelId(), actual.getLevelId());
			assertEquals(expected.getId(), actual.getId());
		}
	}

}
