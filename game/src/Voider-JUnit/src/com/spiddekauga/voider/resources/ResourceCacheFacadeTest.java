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
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

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
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Config.init();
		ResourceSaver.init();
		ResourceNames.useTestPath();

		mDepWithDep.addDependency(mUnderDep);
		mUsingDefDeps.addDependency(mDep);
		mUsingDefDeps.addDependency(mDepWithDep);

		// Delete the ActorDef directory, else previous saved but not deleted actors
		// might be left, which will fail some of these tests
		String actorPath = ResourceNames.getDirPath(PlayerActorDef.class);
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

		Config.dispose();
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
			ResourceCacheFacade.loadAllOf(PlayerActorDef.class, false);
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		}

		assertEquals("number of actors loaded", ACTORS, ResourceCacheFacade.getLoadedCount());

		try {
			ResourceCacheFacade.unloadAllOf(PlayerActorDef.class, false);
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
		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);

		ResourceSaver.save(level);
		ResourceSaver.save(levelDef);

		try {
			ResourceCacheFacade.load(levelDef.getLevelId(), Level.class, levelDef);
		} catch (UndefinedResourceTypeException e) {
			fail("undefined resource type exception");
		}

		ResourceDependencyLoaderTest.delete(level);
		ResourceDependencyLoaderTest.delete(levelDef);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceCacheFacade#load(com.spiddekauga.voider.resources.Def, boolean)}.
	 */
	@Test
	public void loadDef() {
		try {
			ResourceCacheFacade.load(mDef1.getId(), PlayerActorDef.class, false);
			ResourceCacheFacade.load(mDef2.getId(), PlayerActorDef.class, false);
			ResourceCacheFacade.load(mUsingDefDeps.getId(), PlayerActorDef.class, false);
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			fail("Failed to load the definition, undefinined type");
		}

		assertEquals("Loaded three resources", 3, ResourceCacheFacade.getLoadedCount());
		assertTrue("Loaded def 1", ResourceCacheFacade.isLoaded(mDef1.getId(), PlayerActorDef.class));
		assertTrue("Loaded def 2", ResourceCacheFacade.isLoaded(mDef2.getId(), PlayerActorDef.class));
		assertTrue("Loaded def using def dependencies", ResourceCacheFacade.isLoaded(mUsingDefDeps.getId(), PlayerActorDef.class));
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
		ActorDef tempDef = new PlayerActorDef(100f, "test", null);
		ResourceSaver.save(tempDef);

		try {
			FileHandle file = Gdx.files.external(ResourceNames.getDirPath(PlayerActorDef.class) + tempDef.getId().toString());
			file.writeString("Corrupting the actor HAHAHAHA!", false);
		} catch (UndefinedResourceTypeException e) {
			fail("Undefined resource type exception");
		}

		try {
			ResourceCacheFacade.load(tempDef.getId(), PlayerActorDef.class, false);
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
		PlayerActorDef tempDef = new PlayerActorDef(100, "test", null);
		try {
			ResourceCacheFacade.load(tempDef.getId(), PlayerActorDef.class, false);
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
			ResourceCacheFacade.load(mUsingDefDeps.getId(), PlayerActorDef.class, true);
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
		//		try {
		//			ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		//			ResourceCacheFacade.finishLoading();
		//			assertEquals("Loaded internal resource", 1, ResourceCacheFacade.getLoadedCount());
		//
		//			// Unload
		//			ResourceCacheFacade.unload(ResourceNames.EDITOR_BUTTONS);
		//			assertEquals("Unloaded internal resource", 0, ResourceCacheFacade.getLoadedCount());
		//
		//		} catch (UndefinedResourceTypeException e) {
		//			fail("Undefined resource type exception");
		//		}
	}

	/** Regular actor with no dependencies */
	private static PlayerActorDef mDef1 = new PlayerActorDef(100, "def1", null);
	/** Regular actor with no dependencies */
	private static PlayerActorDef mDef2 = new PlayerActorDef(150, "def2", null);
	/** Actor using dependencies */
	private static PlayerActorDef mUsingDefDeps = new PlayerActorDef(155, "using dep", null);
	/** Actor dependency with a dependency */
	private static PlayerActorDef mDepWithDep = new PlayerActorDef(200, "player", null);
	/** Actor dependency */
	private static PlayerActorDef mDep = new PlayerActorDef(300, "boss", null);
	/** Actor under dependency, i.e. UsingDefDeps -> DepWithDep -> UnderDep */
	private static PlayerActorDef mUnderDep = new PlayerActorDef(1000, "pickup", null);

	/** Total number of actors */
	private static int ACTORS = 6;
}
