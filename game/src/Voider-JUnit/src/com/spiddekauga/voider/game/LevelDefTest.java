package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.resources.ResourceNames;

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
		def.setMusic(ResourceNames.SOUND_TEST);
		def.setCampaignId(UUID.randomUUID());
		def.setStoryBefore("story before");
		def.setStoryAfter("story after");
		def.setEndXCoord(555.025f);
		def.setVersion(1, 2, 3);
		def.setThemeId(UUID.randomUUID());
		def.increaseRevision();

		Json json = new Json();
		String jsonString = json.toJson(def);

		LevelDef jsonDef = json.fromJson(LevelDef.class, jsonString);
		assertEquals("same def", def, jsonDef);
		assertEquals("music", def.getMusic(), jsonDef.getMusic());
		assertEquals("campaign id", def.getCampaignId(), jsonDef.getCampaignId());
		assertEquals("story before", def.getStoryBefore(), jsonDef.getStoryBefore());
		assertEquals("story after", def.getStoryAfter(), jsonDef.getStoryAfter());
		assertEquals("end x coord", def.getEndXCoord(), jsonDef.getEndXCoord(), 0.0f);
		assertEquals("version", def.getVersionString(), jsonDef.getVersionString());
		assertEquals("theme id", def.getThemeId(), jsonDef.getThemeId());
		assertEquals("revision", def.getRevision(), jsonDef.getRevision());
		assertEquals("base speed", def.getBaseSpeed(), jsonDef.getBaseSpeed(), 0.0f);
		assertEquals("level id", def.getLevelId(), jsonDef.getLevelId());


		// Test with an empty level def
		def = new LevelDef();
		jsonString = json.toJson(def);

		jsonDef = json.fromJson(LevelDef.class, jsonString);
		assertEquals("same def", def, jsonDef);
		assertEquals("music", def.getMusic(), jsonDef.getMusic());
		assertEquals("campaign id", def.getCampaignId(), jsonDef.getCampaignId());
		assertEquals("story before", def.getStoryBefore(), jsonDef.getStoryBefore());
		assertEquals("story after", def.getStoryAfter(), jsonDef.getStoryAfter());
		assertEquals("end x coord", def.getEndXCoord(), jsonDef.getEndXCoord(), 0.0f);
		assertEquals("version", def.getVersionString(), jsonDef.getVersionString());
		assertEquals("theme id", def.getThemeId(), jsonDef.getThemeId());
		assertEquals("revision", def.getRevision(), jsonDef.getRevision());
		assertEquals("base speed", def.getBaseSpeed(), jsonDef.getBaseSpeed(), 0.0f);
		assertEquals("level id", def.getLevelId(), jsonDef.getLevelId());
	}

	/**
	 * Tests to set a theme and see if it is added to dependencies
	 */
	@Test
	public void setThemeId() {
		LevelDef def = new LevelDef();
		// No theme
		assertEquals("No theme", 0, def.getExternalDependenciesCount());

		// Set theme
		UUID themeId = UUID.randomUUID();
		def.setThemeId(themeId);
		assertEquals("theme added dependency", 1, def.getExternalDependenciesCount());

		// Set another theme
		themeId = UUID.randomUUID();
		def.setThemeId(themeId);
		assertEquals("another theme added size", 1, def.getExternalDependenciesCount());

		// Remove theme
		def.setThemeId(null);
		assertEquals("removed theme", 0, def.getExternalDependenciesCount());
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.LevelDef#increaseVersionFirst()}.
	 */
	@Test
	public void increaseVersionFirst() {
		LevelDef def = new LevelDef();
		def.setVersion(1, 2, 3);
		def.increaseVersionFirst();

		assertEquals("first", 2, def.getVersionFirst());
		assertEquals("second", 0, def.getVersionSecond());
		assertEquals("third", 0, def.getVersionThird());
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.LevelDef#increaseVersionSecond()}.
	 */
	@Test
	public void increaseVersionSecond() {
		LevelDef def = new LevelDef();
		def.setVersion(1, 2, 3);
		def.increaseVersionSecond();

		assertEquals("first", 1, def.getVersionFirst());
		assertEquals("second", 3, def.getVersionSecond());
		assertEquals("third", 0, def.getVersionThird());
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.LevelDef#increaseVersionThird()}.
	 */
	@Test
	public void increaseVersionThird() {
		LevelDef def = new LevelDef();
		def.setVersion(1, 2, 3);
		def.increaseVersionThird();

		assertEquals("first", 1, def.getVersionFirst());
		assertEquals("second", 2, def.getVersionSecond());
		assertEquals("third", 4, def.getVersionThird());
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.LevelDef#getVersionString()}.
	 */
	@Test
	public void getVersionString() {
		LevelDef def = new LevelDef();
		def.setVersion(1, 2, 3);
		assertEquals("version string", "1.2.3", def.getVersionString());
	}

}
