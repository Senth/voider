package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.game.actors.Types;

/**
 * Tests whether the cache facade works correctly
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceCacheFacadeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.init();
		ResourceSaver.init();
		ResourceNames.useTestPath();
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();

		mDepWithDep.addDependency(mUnderDep);
		mUsingDefDeps.addDependency(mDep);
		mUsingDefDeps.addDependency(mDepWithDep);

		// Delete the ActorDef directory, else previous saved but not deleted actors
		// might be left, which will fail some of these tests
		String actorPath = ResourceNames.getDirPath(ActorDef.class);
		Gdx.files.external(actorPath).deleteDirectory();

		ResourceSaver.save(mDef1);
		ResourceSaver.save(mDef2);
		ResourceSaver.save(mDep);
		ResourceSaver.save(mDepWithDep);
		ResourceSaver.save(mUnderDep);
		ResourceSaver.save(mUsingDefDeps);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourceDependencyLoaderTest.delete(mDef1);
		ResourceDependencyLoaderTest.delete(mDef2);
		ResourceDependencyLoaderTest.delete(mDep);
		ResourceDependencyLoaderTest.delete(mDepWithDep);
		ResourceDependencyLoaderTest.delete(mUnderDep);
		ResourceDependencyLoaderTest.delete(mUsingDefDeps);
	}

	/**
	 * Clear the cache facade before each test
	 */
	@Before
	public void setUp() {
		ResourceCacheFacade.init();
	}

	/**
	 * Clear the cache facade after each test
	 */
	@After
	public void tearDown() {
		ResourceCacheFacade.dispose();
	}

	/**
	 * Test to load all ActorDefs (and unload)
	 */
	@Test
	public void loadAllOf() {
		try {
			ResourceCacheFacade.loadAllOf(ActorDef.class, false);
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		}

		assertEquals("number of actors loaded", ACTORS, ResourceCacheFacade.getLoadedCount());

		try {
			ResourceCacheFacade.unloadAllOf(ActorDef.class, false);
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		}

		assertEquals("number of actor, after unload()", ACTORS, ResourceCacheFacade.getLoadedCount());
	}

	/**
	 * Test to load all ActorDefs including dependencies
	 */
	@Test
	public void loadAllOfIncludingDependencies() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceCacheFacade#load(java.util.UUID, java.lang.Class, com.spiddekauga.voider.resources.Def)}.
	 */
	@Test
	public void loadLevel() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceCacheFacade#load(com.spiddekauga.voider.resources.Def, boolean)}.
	 */
	@Test
	public void loadDef() {
		try {
			ResourceCacheFacade.load(mDef1.getId(), ActorDef.class, false);
			ResourceCacheFacade.load(mDef2.getId(), ActorDef.class, false);
			ResourceCacheFacade.load(mUsingDefDeps.getId(), ActorDef.class, false);
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			fail("Failed to load the definition, undefinined type");
		}

		assertEquals("Loaded three resources", 3, ResourceCacheFacade.getLoadedCount());
		assertTrue("Loaded def 1", ResourceCacheFacade.isLoaded(mDef1.getId(), ActorDef.class));
		assertTrue("Loaded def 2", ResourceCacheFacade.isLoaded(mDef2.getId(), ActorDef.class));
		assertTrue("Loaded def using def dependencies", ResourceCacheFacade.isLoaded(mUsingDefDeps.getId(), ActorDef.class));
	}

	/**
	 * Test to load an invalid resource type
	 * @throws UndefinedResourceTypeException
	 */
	@Test(expected = UndefinedResourceTypeException.class)
	public void loadInvalidResourceType() throws UndefinedResourceTypeException {
		ResourceCacheFacade.load(mDef1.getId(), String.class, false);
	}

	/**
	 * Test to load a corrupted resource
	 */
	@Test(expected = ResourceCorruptException.class)
	public void loadCorruptResource() {
		ActorDef tempDef = new ActorDef();
		ResourceSaver.save(tempDef);

		try {
			FileHandle file = Gdx.files.external(ResourceNames.getDirPath(ActorDef.class) + tempDef.getId().toString());
			file.writeString("Corrupting the actor HAHAHAHA!", false);
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		}

		try {
			ResourceCacheFacade.load(tempDef.getId(), ActorDef.class, false);
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		} catch (ResourceCorruptException e) {
			ResourceDependencyLoaderTest.delete(tempDef);
			throw e;
		}

		try {
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		} catch (ResourceCorruptException e) {
			ResourceDependencyLoaderTest.delete(tempDef);
			throw e;
		}
	}

	/**
	 * Test to load a resource that doesn't exist
	 */
	@Test(expected = ResourceNotFoundException.class)
	public void loadNoneExistingResource() {
		ActorDef tempDef = new ActorDef();
		try {
			ResourceCacheFacade.load(tempDef.getId(), ActorDef.class, false);
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		}

		try {
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		} catch (ResourceCorruptException e) {
			ResourceDependencyLoaderTest.delete(tempDef);
			throw e;
		}
	}

	/**
	 * Test to load a definition with some definition dependencies
	 */
	@Test
	public void loadDefWithDefDependencies() {
		try {
			// Load
			ResourceCacheFacade.load(mUsingDefDeps.getId(), ActorDef.class, true);
			ResourceCacheFacade.finishLoading();
			assertEquals("Loaded actors with dependencies", 4, ResourceCacheFacade.getLoadedCount());

			// Unload
			ResourceCacheFacade.unload(mUsingDefDeps, true);
			assertEquals("Unloaded actors with dependencies", 0, ResourceCacheFacade.getLoadedCount());

		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		}
	}

	/**
	 * Test to load a definition with resource (texture/sound) dependencies
	 */
	@Test
	public void loadDefWithResourceDependencies() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceCacheFacade#load(com.spiddekauga.voider.resources.ResourceNames)}.
	 */
	@Test
	public void loadResourceName() {
		fail("Not yet implemented");
	}

	/** Regular actor with no dependencies */
	private static ActorDef mDef1 = new ActorDef(100, Types.BULLET, null, "def1", null);
	/** Regular actor with no dependencies */
	private static ActorDef mDef2 = new ActorDef(150, Types.BULLET, null, "def2", null);
	/** Actor using dependencies */
	private static ActorDef mUsingDefDeps = new ActorDef(155, Types.BULLET, null, "using dep", null);
	/** Actor dependency with a dependency */
	private static ActorDef mDepWithDep = new ActorDef(200, Types.PLAYER, null, "player", null);
	/** Actor dependency */
	private static ActorDef mDep = new ActorDef(300, Types.BOSS, null, "boss", null);
	/** Actor under dependency, i.e. UsingDefDeps -> DepWithDep -> UnderDep */
	private static ActorDef mUnderDep = new ActorDef(1000, Types.PICKUP, null, "pickup", null);

	/** Total number of actors */
	private static int ACTORS = 6;
}
