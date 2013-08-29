package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.JsonWrapper;
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
		ResourceItem item = new ResourceItem(uuid, PlayerActorDef.class, -1);
		ResourceItem testItem = new ResourceItem(uuid, Texture.class, -1);

		assertEquals("testing with uuid directly", item.resourceId, uuid);
		assertEquals("different types", testItem, item);

		UUID otherUuid = UUID.randomUUID();
		testItem = new ResourceItem(otherUuid, PlayerActorDef.class, -1);
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
		ResourceItem item = new ResourceItem(uuid, PlayerActorDef.class, -1);

		Json json = new JsonWrapper();
		String jsonString = json.toJson(item);

		ResourceItem testItem = json.fromJson(ResourceItem.class, jsonString);

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
