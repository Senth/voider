package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tests the def class so that it works.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class DefTest {

	/**
	 * Setup def class; make some variables public
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@BeforeClass
	static public void setUpBeforeClass() throws SecurityException, NoSuchFieldException {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		mfCreator = Def.class.getDeclaredField("mCreator");
		mfCreator.setAccessible(true);

		mfOriginalCreator = Def.class.getDeclaredField("mOriginalCreator");
		mfOriginalCreator.setAccessible(true);
	}

	/**
	 * Test method for
	 * {@link com.spiddekauga.voider.resources.Def#equals(java.lang.Object)}.
	 */
	@Test
	public void equalsObject() {
		Def def = new PlayerActorDef();
		Def def2 = new PlayerActorDef();

		assertEquals("equals()", def, def);
		assertTrue("not equals()", !def.equals(def2));
	}

	/**
	 * Test to write and read definition
	 */
	@Test
	public void writeRead() {
		PlayerActorDef def = new PlayerActorDef();
		Def dependency1 = new PickupActorDef();
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
		def.addDependency(InternalNames.IMAGE_SPLASH_SCREEN);
		def.addDependency(InternalNames.THEME_RED_PLANET_CORE);

		Kryo kryo = Pools.kryo.obtain();
		PlayerActorDef testDef = KryoPrototypeTest.copy(def, PlayerActorDef.class, kryo);
		testEquals(def, testDef);
		testDef.dispose();

		// Test copy
		testDef = def.copy();
		testEquals(def, testDef);
		// Test parent id
		assertEquals(null, def.getCopyParentId());
		testDef.dispose();

		// Test copy new
		testDef = def.copyNewResource();
		def.setName(def.getName() + " (copy)");
		testEquals(def, testDef);
		// But new id
		assertFalse(def.getId().equals(testDef.getId()));

		Pools.kryo.free(kryo);
	}

	/**
	 * Test whether two definitions are equal
	 * @param expected the original definition to test with
	 * @param actual the copied or loaded definition
	 */
	public static void testEquals(Def expected, Def actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getOriginalCreator(), actual.getOriginalCreator());
		assertEquals(expected.getDescription(), actual.getDescription());
		assertEquals(expected.getDate(), actual.getDate());
		assertEquals(expected.getRevision(), actual.getRevision());

		// External dependencies
		assertEquals(expected.getExternalDependenciesCount(), actual.getExternalDependenciesCount());
		for (Map.Entry<UUID, AtomicInteger> dependency : expected.getExternalDependencies().entrySet()) {
			AtomicInteger foundDependency = actual.getExternalDependencies().get(dependency.getKey());
			assertNotNull(foundDependency);
			assertEquals(dependency.getValue().get(), foundDependency.get());
		}

		// Internal dependencies
		assertEquals(expected.getInternalDependenciesCount(), actual.getInternalDependenciesCount());
		for (InternalNames dependency : expected.getInternalDependencies()) {
			assertTrue(actual.getInternalDependencies().contains(dependency));
		}
	}

	/**
	 * Test method for
	 * {@link com.spiddekauga.voider.resources.Def#addDependency(com.spiddekauga.voider.resources.IResource)}
	 * .
	 */
	@Test
	public void addDependencyDef() {
		Def def = new PlayerActorDef();
		Def dependency1 = new PickupActorDef();
		Def dependency2 = new BulletActorDef();
		def.addDependency(dependency1);
		def.addDependency(dependency2);

		assertNotNull("def dependencies not null", def.getExternalDependencies());
		assertEquals("def dependencies size", def.getExternalDependencies().size(), 2);
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies", def.getInternalDependencies().size(), 0);


		// Test to add the same dependency again, should remain the same
		def.addDependency(dependency1);
		assertEquals("def dependencies added twite", def.getExternalDependencies().size(), 2);
	}

	/**
	 * Test method for
	 * {@link com.spiddekauga.voider.resources.Def#addDependency(com.spiddekauga.voider.repo.resource.InternalNames)}
	 * .
	 */
	@Test
	public void addDependencyResourceNames() {
		Def def = new PlayerActorDef();
		def.addDependency(InternalNames.IMAGE_SPLASH_SCREEN);
		def.addDependency(InternalNames.SHADER_DEFAULT);
		def.addDependency(InternalNames.THEME_RED_PLANET_CORE);

		assertNotNull("def dependencies null", def.getExternalDependencies());
		assertEquals("def dependencies", def.getExternalDependencies().size(), 0);
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies size", def.getInternalDependencies().size(), 3);

		// Test to add the same dependency again, should remain the same
		def.addDependency(InternalNames.THEME_RED_PLANET_CORE);
		assertEquals("res dependencies added twice", def.getInternalDependencies().size(), 3);
	}

	/**
	 * Test method for
	 * {@link com.spiddekauga.voider.resources.Def#removeDependency(java.util.UUID)}.
	 */
	@Test
	public void removeDependencyUUID() {
		Def def = new PlayerActorDef();
		Def dependency1 = new PickupActorDef();
		Def dependency2 = new BulletActorDef();
		def.addDependency(dependency1);
		def.addDependency(dependency2);

		def.removeDependency(dependency1.getId());

		assertNotNull("def dependencies not null", def.getExternalDependencies());
		assertEquals("def dependencies size", def.getExternalDependencies().size(), 1);
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies", def.getInternalDependencies().size(), 0);

		// Test to remove a dependency that doesn't exist
		try {
			def.removeDependency(UUID.randomUUID());
		} catch (Exception e) {
			// Does nothing
		}
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
	 * Test method for
	 * {@link com.spiddekauga.voider.resources.Def#removeDependency(com.spiddekauga.voider.repo.resource.InternalNames)}
	 * .
	 */
	@Test
	public void removeDependencyResourceNames() {
		Def def = new PlayerActorDef();
		def.addDependency(InternalNames.IMAGE_SPLASH_SCREEN);
		def.addDependency(InternalNames.SHADER_DEFAULT);
		def.removeDependency(InternalNames.THEME_RED_PLANET_CORE);

		assertNotNull("def dependencies null", def.getExternalDependencies());
		assertEquals("def dependencies", def.getExternalDependencies().size(), 0);
		assertNotNull("res dependencies not null", def.getInternalDependencies());
		assertEquals("res dependencies size", def.getInternalDependencies().size(), 1);

		// Test to remove a dependency that doesn't exist
		def.removeDependency(InternalNames.THEME_RED_PLANET_SURFACE);
		assertEquals("res dependencies size removed unknown", def.getInternalDependencies().size(), 1);

		// Remove last dependency
		def.removeDependency(InternalNames.THEME_RED_PLANET_CORE);
		assertNotNull("res not null afetr all removed", def.getInternalDependencies());
		assertEquals("res dependencies size all removed", def.getInternalDependencies().size(), 0);

		// Readd a dependency
		def.addDependency(InternalNames.THEME_RED_PLANET_CORE);
		assertEquals("res dependencies size readded one", def.getInternalDependencies().size(), 1);
	}

	/** Field to the original creator in def class */
	private static Field mfOriginalCreator = null;
	/** Field to the creator in the def class */
	private static Field mfCreator = null;
}
