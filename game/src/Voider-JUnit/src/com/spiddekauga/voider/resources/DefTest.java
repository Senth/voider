package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.game.actors.Types;

/**
 * Tests the def class so that it works.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DefTest {

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#equals(java.lang.Object)}.
	 */
	@Test
	public void equalsObject() {
		Def def = new ActorDef(100, Types.PLAYER, null, "player", null);
		Def def2 = new ActorDef(100, Types.PICKUP, null, "player", null);

		assertEquals("equals()", def, def);
		assertTrue("not equals()", !def.equals(def2));


		// Use JSON to create a second definition with the same UUID
		Json json = new Json();
		String jsonString = json.toJson(def);
		Def testDef = json.fromJson(ActorDef.class, jsonString);
		assertEquals("equals() from json", testDef, def);

		// Change dependencies
		testDef.addDependency(def2);
		testDef.addDependency(ResourceNames.TEXTURE_PLAYER);
		assertEquals("equals() from json, added dependencies", testDef, def);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#write(com.badlogic.gdx.utils.Json)}.
	 */
	@Test
	public void writeRead() {
		Def def = new ActorDef(100, Types.PLAYER, null, "player", null);
		Def dependency1 = new ActorDef(200, Types.BOSS, null, "boss", null);
		Def dependency2 = new ActorDef(300, Types.BULLET, null, "bullet", null);
		def.addDependency(dependency1);
		def.addDependency(dependency2);
		def.addDependency(ResourceNames.PARTICLE_TEST);
		def.addDependency(ResourceNames.TEXTURE_PLAYER);

		Json json = new Json();
		String jsonString = json.toJson(def);
		Def testDef = json.fromJson(ActorDef.class, jsonString);

		assertEquals("UUID equals", testDef.getId(), def.getId());

		// Def dependencies
		assertEquals("Dep def size", testDef.getExternalDependencies().size(), def.getExternalDependencies().size());
		for (DefItem item : def.getExternalDependencies()) {
			assertTrue("Dep def item", testDef.getExternalDependencies().contains(item));
		}

		// ResourceNames dependencies
		assertEquals("Dep res size", testDef.getInternalDependencies().size(), def.getInternalDependencies().size());
		for (ResourceNames item : def.getInternalDependencies()) {
			assertTrue("Dep res item", testDef.getInternalDependencies().contains(item));
		}
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#addDependency(com.spiddekauga.voider.resources.Def)}.
	 */
	@Test
	public void addDependencyDef() {
		Def def = new ActorDef(100, Types.PLAYER, null, "player", null);
		Def dependency1 = new ActorDef(200, Types.BOSS, null, "boss", null);
		Def dependency2 = new ActorDef(300, Types.BULLET, null, "bullet", null);
		def.addDependency(dependency1);
		def.addDependency(dependency2);

		assertNotNull("def dependencies not null", def.getExternalDependencies());
		assertEquals("def dependencies size", def.getExternalDependencies().size(), 2);
		assertNull("res dependencies", def.getInternalDependencies());


		// Test to add the same dependency again, should remain the same
		def.addDependency(dependency1);
		assertEquals("def dependencies added twite", def.getExternalDependencies().size(), 2);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#addDependency(com.spiddekauga.voider.resources.ResourceNames)}.
	 */
	@Test
	public void addDependencyResourceNames() {
		Def def = new ActorDef(100, Types.PLAYER, null, "player", null);
		def.addDependency(ResourceNames.PARTICLE_TEST);
		def.addDependency(ResourceNames.TEXTURE_PLAYER);
		def.addDependency(ResourceNames.SOUND_TEST);

		assertNull("def dependencies", def.getExternalDependencies());
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies size", def.getInternalDependencies().size(), 3);

		// Test to add the same dependency again, should remain the same
		def.addDependency(ResourceNames.TEXTURE_PLAYER);
		assertEquals("res dependencies added twice", def.getInternalDependencies().size(), 3);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#removeDependency(java.util.UUID)}.
	 */
	@Test
	public void removeDependencyUUID() {
		Def def = new ActorDef(100, Types.PLAYER, null, "player", null);
		Def dependency1 = new ActorDef(200, Types.BOSS, null, "boss", null);
		Def dependency2 = new ActorDef(300, Types.BULLET, null, "bullet", null);
		def.addDependency(dependency1);
		def.addDependency(dependency2);

		def.removeDependency(dependency1.getId());

		assertNotNull("def dependencies not null", def.getExternalDependencies());
		assertEquals("def dependencies size", def.getExternalDependencies().size(), 1);
		assertNull("res dependencies", def.getInternalDependencies());

		// Test to remove a dependency that doesn't exist
		def.removeDependency(UUID.randomUUID());
		assertEquals("def dependencies size removed unknown", def.getExternalDependencies().size(), 1);

		// Remove the last dependency
		def.removeDependency(dependency2.getId());
		assertNotNull("def not null after all removed", def.getExternalDependencies());
		assertEquals("def dependencies size all removed", def.getExternalDependencies().size(), 0);

		// Readd a dependency
		def.addDependency(dependency1);
		assertEquals("def dependencies size readded one", def.getExternalDependencies().size(), 1);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#removeDependency(com.spiddekauga.voider.resources.ResourceNames)}.
	 */
	@Test
	public void removeDependencyResourceNames() {
		Def def = new ActorDef(100, Types.PLAYER, null, "player", null);
		def.addDependency(ResourceNames.PARTICLE_TEST);
		def.addDependency(ResourceNames.SOUND_TEST);
		def.removeDependency(ResourceNames.PARTICLE_TEST);

		assertNull("def dependencies", def.getExternalDependencies());
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies size", def.getInternalDependencies().size(), 1);

		// Test to remove a dependency that doesn't exist
		def.removeDependency(ResourceNames.TEXTURE_PLAYER);
		assertEquals("res dependencies size removed unknown", def.getInternalDependencies().size(), 1);

		// Remove last dependency
		def.removeDependency(ResourceNames.SOUND_TEST);
		assertNotNull("res not null afetr all removed", def.getInternalDependencies());
		assertEquals("res dependencies size all removed", def.getInternalDependencies().size(), 0);

		// Readd a dependency
		def.addDependency(ResourceNames.PARTICLE_TEST);
		assertEquals("res dependencies size readded one", def.getInternalDependencies().size(), 1);
	}

}
