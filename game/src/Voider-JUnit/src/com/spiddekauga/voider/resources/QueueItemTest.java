package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.JsonWrapper;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

/**
 * JUnit test for QueueItem class
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class QueueItemTest {

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceItem#equals(java.lang.Object)}.
	 */
	@Test
	public void equalsObject() {
		// Queue item should equal if the UUID is equal, that is enough
		UUID equalUUID = UUID.randomUUID();
		ResourceItem equalUUID1 = new ResourceItem(equalUUID, Texture.class);
		ResourceItem equalUUID2 = new ResourceItem(equalUUID, PlayerActorDef.class);
		assertTrue("QueueItem.equals() with same UUID, but different types", equalUUID1.equals(equalUUID2));

		ResourceItem inequal = new ResourceItem(UUID.randomUUID(), Texture.class);
		assertTrue("QueueItem.equals() inequality different UUID, but same types", !equalUUID1.equals(inequal));
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceItem#write(com.badlogic.gdx.utils.Json)}.
	 */
	@Test
	public void writeAndRead() {
		ResourceItem writeItem = new ResourceItem(UUID.randomUUID(), PlayerActorDef.class);
		Json json = new JsonWrapper();

		String jsonString = json.toJson(writeItem);
		ResourceItem readItem = json.fromJson(ResourceItem.class, jsonString);

		assertTrue("QueueItem.write()/read(): uuid equals", writeItem.resourceId.equals(readItem.resourceId));
		assertTrue("QueueItem.write()/read(): type equals", writeItem.resourceType.equals(readItem.resourceType));
		assertTrue("QueueItem.write()/read(): fullname equals", writeItem.fullName.equals(readItem.fullName));
	}
}
