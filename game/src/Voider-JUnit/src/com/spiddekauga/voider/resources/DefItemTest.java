package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.graphics.Texture;
import com.spiddekauga.utils.JsonWrapper; import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

/**
 * Testing DefinitionItem
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DefItemTest {

	/**
	 * Testing if equals works
	 */
	@Test
	public void equals() {
		UUID uuid = UUID.randomUUID();
		DefItem item = new DefItem(uuid, PlayerActorDef.class);
		DefItem testItem = new DefItem(uuid, Texture.class);

		assertEquals("testing with uuid directly", item.resourceId, uuid);
		assertEquals("different types", testItem, item);

		UUID otherUuid = UUID.randomUUID();
		testItem = new DefItem(otherUuid, PlayerActorDef.class);
		assertTrue("different uuid", !item.equals(testItem));


		// Testing with pure UUID
		assertEquals("pure uuid", item, uuid);
	}

	/**
	 * Testing write and reading json
	 */
	@Test
	public void writeRead() {
		UUID uuid = UUID.randomUUID();
		DefItem item = new DefItem(uuid, PlayerActorDef.class);

		Json json = new Json();
		String jsonString = json.toJson(item);

		DefItem testItem = json.fromJson(DefItem.class, jsonString);

		assertEquals("uuid", testItem.resourceId, uuid);
		assertEquals("type", testItem.resourceType, PlayerActorDef.class);
		String fullName = null;
		try {
			fullName = ResourceNames.getDirPath(PlayerActorDef.class) + uuid.toString();
		} catch (UndefinedResourceTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("fullname", testItem.fullName, fullName);
	}

}
