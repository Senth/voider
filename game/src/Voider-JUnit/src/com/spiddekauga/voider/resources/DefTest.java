package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.actors.BossActorDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

/**
 * Tests the def class so that it works.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DefTest {

	/**
	 * Setup def class; make some variables public
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@BeforeClass
	static public void setUpBeforeClass() throws SecurityException, NoSuchFieldException {
		mfCreator = Def.class.getDeclaredField("mCreator");
		mfCreator.setAccessible(true);

		mfOriginalCreator = Def.class.getDeclaredField("mOriginalCreator");
		mfOriginalCreator.setAccessible(true);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#equals(java.lang.Object)}.
	 */
	@Test
	public void equalsObject() {
		Def def = new PlayerActorDef();
		Def def2 = new PlayerActorDef();

		assertEquals("equals()", def, def);
		assertTrue("not equals()", !def.equals(def2));


		// Use JSON to create a second definition with the same UUID
		Json json = new Json();
		String jsonString = json.toJson(def);
		Def testDef = json.fromJson(PlayerActorDef.class, jsonString);
		assertEquals("equals() from json", testDef, def);

		// Change dependencies
		testDef.addDependency(def2);
		testDef.addDependency(ResourceNames.TEXTURE_PLAYER);
		assertEquals("equals() from json, added dependencies", testDef, def);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#write(com.spiddekauga.utils.Json)}.
	 */
	@Test
	public void writeRead() {
		Def def = new PlayerActorDef();
		Def dependency1 = new BossActorDef();
		Def dependency2 = new BulletActorDef();
		def.setDescription("testComment");
		try {
			mfCreator.set(def, "originalCreator");
			mfOriginalCreator.set(def, "creator");
		} catch (Exception e) {
			fail("One of the creator field failed");
		}
		def.addDependency(dependency1);
		def.addDependency(dependency2);
		def.addDependency(ResourceNames.PARTICLE_TEST);
		def.addDependency(ResourceNames.TEXTURE_PLAYER);

		Json json = new Json();
		String jsonString = json.toJson(def);
		Def testDef = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("UUID equals", def.getId(), testDef.getId());
		assertEquals("Name", def.getName(), testDef.getName());
		assertEquals("Comment", def.getDescription(), testDef.getDescription());
		assertEquals("Creator", def.getCreator(), testDef.getCreator());
		assertEquals("Original Creator", def.getOriginalCreator(), testDef.getOriginalCreator());


		// Def dependencies
		assertEquals("Dep def size", def.getExternalDependencies().size, testDef.getExternalDependencies().size);
		for (ObjectMap.Entry<UUID, DefItem> entry : def.getExternalDependencies().entries()) {
			assertTrue("Dep def item", testDef.getExternalDependencies().containsKey(entry.key));
		}

		// ResourceNames dependencies
		assertEquals("Dep res size", def.getInternalDependencies().size(), testDef.getInternalDependencies().size());
		for (ResourceNames item : def.getInternalDependencies()) {
			assertTrue("Dep res item", testDef.getInternalDependencies().contains(item));
		}
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#addDependency(com.spiddekauga.voider.resources.Def)}.
	 */
	@Test
	public void addDependencyDef() {
		Def def = new PlayerActorDef();
		Def dependency1 = new BossActorDef();
		Def dependency2 = new BulletActorDef();
		def.addDependency(dependency1);
		def.addDependency(dependency2);

		assertNotNull("def dependencies not null", def.getExternalDependencies());
		assertEquals("def dependencies size", def.getExternalDependencies().size, 2);
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies", def.getInternalDependencies().size(), 0);


		// Test to add the same dependency again, should remain the same
		def.addDependency(dependency1);
		assertEquals("def dependencies added twite", def.getExternalDependencies().size, 2);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#addDependency(com.spiddekauga.voider.resources.ResourceNames)}.
	 */
	@Test
	public void addDependencyResourceNames() {
		Def def = new PlayerActorDef();
		def.addDependency(ResourceNames.PARTICLE_TEST);
		def.addDependency(ResourceNames.TEXTURE_PLAYER);
		def.addDependency(ResourceNames.SOUND_TEST);

		assertNotNull("def dependencies null", def.getExternalDependencies());
		assertEquals("def dependencies", def.getExternalDependencies().size, 0);
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
		Def def = new PlayerActorDef();
		Def dependency1 = new BossActorDef();
		Def dependency2 = new BulletActorDef();
		def.addDependency(dependency1);
		def.addDependency(dependency2);

		def.removeDependency(dependency1.getId());

		assertNotNull("def dependencies not null", def.getExternalDependencies());
		assertEquals("def dependencies size", def.getExternalDependencies().size, 1);
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies", def.getInternalDependencies().size(), 0);

		// Test to remove a dependency that doesn't exist
		def.removeDependency(UUID.randomUUID());
		assertEquals("def dependencies size removed unknown", def.getExternalDependencies().size, 1);

		// Remove the last dependency
		def.removeDependency(dependency2.getId());
		assertNotNull("def not null after all removed", def.getExternalDependencies());
		assertEquals("def dependencies size all removed", def.getExternalDependencies().size, 0);

		// Readd a dependency
		def.addDependency(dependency1);
		assertEquals("def dependencies size readded one", def.getExternalDependencies().size, 1);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.Def#removeDependency(com.spiddekauga.voider.resources.ResourceNames)}.
	 */
	@Test
	public void removeDependencyResourceNames() {
		Def def = new PlayerActorDef();
		def.addDependency(ResourceNames.PARTICLE_TEST);
		def.addDependency(ResourceNames.SOUND_TEST);
		def.removeDependency(ResourceNames.PARTICLE_TEST);

		assertNotNull("def dependencies null", def.getExternalDependencies());
		assertEquals("def dependencies", def.getExternalDependencies().size, 0);
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

	/** Field to the original creator in def class */
	private static Field mfOriginalCreator = null;
	/** Field to the creator in the def class */
	private static Field mfCreator = null;
}
