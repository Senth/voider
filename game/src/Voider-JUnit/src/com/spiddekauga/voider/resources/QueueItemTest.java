package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.game.ActorDef;

/**
 * JUnit test for QueueItem class
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class QueueItemTest {

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.DefinitionItem#equals(java.lang.Object)}.
	 */
	@Test
	public void equalsObject() {
		// Queue item should equal if the UUID is equal, that is enough
		UUID equalUUID = UUID.randomUUID();
		DefinitionItem equalUUID1 = new DefinitionItem(equalUUID, DefinitionItem.class);
		DefinitionItem equalUUID2 = new DefinitionItem(equalUUID, Class.class);
		assertTrue("QueueItem.equals() with same UUID, but different types", equalUUID1.equals(equalUUID2));

		DefinitionItem inequal = new DefinitionItem(UUID.randomUUID(), DefinitionItem.class);
		assertTrue("QueueItem.equals() inequality different UUID, but same types", !equalUUID1.equals(inequal));
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.DefinitionItem#write(com.badlogic.gdx.utils.Json)}.
	 */
	@Test
	public void writeAndRead() {
		DefinitionItem writeItem = new DefinitionItem(UUID.randomUUID(), ActorDef.class);
		Json json = new Json();

		String jsonString = json.toJson(writeItem);
		DefinitionItem readItem = json.fromJson(DefinitionItem.class, jsonString);

		assertTrue("QueueItem.write()/read(): uuid equals", writeItem.resourceId.equals(readItem.resourceId));
		assertTrue("QueueItem.write()/read(): type equals", writeItem.resourceType.equals(readItem.resourceType));
		assertTrue("QueueItem.write()/read(): fullname equals", writeItem.fullName.equals(readItem.fullName));
	}
}
