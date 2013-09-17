package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.JsonWrapper;

/**
 * Tests the LevelDef for bugs
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelDefTest {
	/**
	 * Test method for {@link com.spiddekauga.voider.game.LevelDef#write(com.badlogic.gdx.utils.Json)}.
	 * Tests to write and read to/from a Json object
	 */
	@Test
	public void writeRead() {
		LevelDef def = new LevelDef();
		def.setCampaignId(UUID.randomUUID());
		def.setPrologue("story before");
		def.setStoryAfter("story after");
		def.setEndXCoord(555.025f);
		def.setRevision(1);

		Json json = new JsonWrapper();
		String jsonString = json.toJson(def);

		LevelDef jsonDef = json.fromJson(LevelDef.class, jsonString);
		assertEquals("same def", def, jsonDef);
		assertEquals("campaign id", def.getCampaignId(), jsonDef.getCampaignId());
		assertEquals("story before", def.getPrologue(), jsonDef.getPrologue());
		assertEquals("story after", def.getEpilogue(), jsonDef.getEpilogue());
		assertEquals("end x coord", def.getEndXCoord(), jsonDef.getEndXCoord(), 0.0f);
		assertEquals("revision", def.getRevision(), jsonDef.getRevision());
		assertEquals("base speed", def.getBaseSpeed(), jsonDef.getBaseSpeed(), 0.0f);
		assertEquals("level id", def.getLevelId(), jsonDef.getLevelId());


		// Test with an empty level def
		def = new LevelDef();
		jsonString = json.toJson(def);

		jsonDef = json.fromJson(LevelDef.class, jsonString);
		assertEquals("same def", def, jsonDef);
		assertEquals("campaign id", def.getCampaignId(), jsonDef.getCampaignId());
		assertEquals("story before", def.getPrologue(), jsonDef.getPrologue());
		assertEquals("story after", def.getEpilogue(), jsonDef.getEpilogue());
		assertEquals("end x coord", def.getEndXCoord(), jsonDef.getEndXCoord(), 0.0f);
		assertEquals("revision", def.getRevision(), jsonDef.getRevision());
		assertEquals("base speed", def.getBaseSpeed(), jsonDef.getBaseSpeed(), 0.0f);
		assertEquals("level id", def.getLevelId(), jsonDef.getLevelId());
	}

}
