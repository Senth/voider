package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

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
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.repo.ApplicationStub;
import com.spiddekauga.voider.repo.ResourceLocalRepo;
import com.spiddekauga.voider.repo.SqliteResetter;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneStub;


/**
 * Tests whether the cache facade works correctly
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @todo implement
 */
public class ResourceCacheFacadeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Gdx.app = new ApplicationStub();

		mDepWithDep.addDependency(mUnderDep);
		mUsingDefDeps.addDependency(mDep);
		mUsingDefDeps.addDependency(mDepWithDep);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Config.dispose();
	}

	/**
	 * Clear the cache facade before each test
	 */
	@Before
	public void setUp() {
		SqliteResetter.reset();

		// ResourceSaver.save(mDef1);
		// ResourceSaver.save(mDef2);
		// ResourceSaver.save(mDep);
		// ResourceSaver.save(mDepWithDep);
		// ResourceSaver.save(mUnderDep);
		// ResourceSaver.save(mUsingDefDeps);
	}

	/**
	 * Clear the cache facade after each test
	 */
	@After
	public void tearDown() {
		ResourceCacheFacade.unload(mScene);
		// ResourceSaver.clearResources();
	}

	/**
	 * Test to load all ActorDefs (and unload)
	 */
	@Test
	public void loadAllOf() {
		ResourceCacheFacade.loadAllOf(mScene, ExternalTypes.PLAYER_DEF, false);
		ResourceCacheFacade.finishLoading();

		assertEquals("number of actors loaded", ACTORS, ResourceCacheFacade.getLoadedCount());

		ResourceCacheFacade.unload(mScene);

		assertEquals("number of actor, after unload()", 0, ResourceCacheFacade.getLoadedCount());
	}

	/**
	 * Test loading and unloading older revisions
	 */
	@Test
	public void loadDifferentRevisions() {
		PlayerActorDef playerActorDef = new PlayerActorDef();

		UUID id = playerActorDef.getId();

		// ResourceSaver.save(playerActorDef);
		// ResourceSaver.save(playerActorDef);
		// ResourceSaver.save(playerActorDef);

		// Load latest revision
		ResourceCacheFacade.load(mScene, id, false);
		ResourceCacheFacade.finishLoading();
		assertTrue(ResourceCacheFacade.isLoaded(id));
		assertTrue(ResourceCacheFacade.isLoaded(id, 3));

		// Load an older revision
		assertFalse(ResourceCacheFacade.isLoaded(id, 2));
		ResourceCacheFacade.load(mScene, id, false, 2);
		ResourceCacheFacade.finishLoading();
		assertTrue(ResourceCacheFacade.isLoaded(id, 2));

		// Unload all and test so that they aren't loaded
		ResourceCacheFacade.unload(mScene);
		assertFalse(ResourceCacheFacade.isLoaded(id));
		assertFalse(ResourceCacheFacade.isLoaded(id, 3));
		assertFalse(ResourceCacheFacade.isLoaded(id, 2));
		assertEquals(0, ResourceCacheFacade.getAll(ExternalTypes.PLAYER_DEF).size());

	}

	/**
	 * Test method for
	 */
	@Test
	public void loadLevel() {
		LevelDef levelDef = new LevelDef();
		levelDef.removeDependency(InternalNames.THEME_SPACE);
		// Level level = new Level(levelDef);

		// ResourceSaver.save(level);
		// ResourceSaver.save(levelDef);

		ResourceCacheFacade.load(mScene, levelDef.getLevelId(), levelDef.getId());
		ResourceCacheFacade.finishLoading();
	}

	/**
	 * Test method for
	 */
	@Test
	public void loadDef() {
		ResourceCacheFacade.load(mScene, mDef1.getId(), false);
		ResourceCacheFacade.load(mScene, mDef2.getId(), false);
		ResourceCacheFacade.load(mScene, mUsingDefDeps.getId(), false);
		ResourceCacheFacade.finishLoading();

		assertEquals("Loaded three resources", 3, ResourceCacheFacade.getLoadedCount());
		assertTrue("Loaded def 1", ResourceCacheFacade.isLoaded(mDef1.getId()));
		assertTrue("Loaded def 2", ResourceCacheFacade.isLoaded(mDef2.getId()));
		assertTrue("Loaded def using def dependencies", ResourceCacheFacade.isLoaded(mUsingDefDeps.getId()));
	}

	/**
	 * Test to load a corrupted resource
	 */
	@Test(expected = ResourceCorruptException.class)
	public void loadCorruptResource() {
		ActorDef tempDef = new PlayerActorDef();
		// ResourceSaver.save(tempDef);

		FileHandle file = Gdx.files.external(ResourceLocalRepo.getFilepath(tempDef));
		file.writeString("Corrupting the actor HAHAHAHA!", false);

		ResourceCacheFacade.load(mScene, tempDef.getId(), false);
		ResourceCacheFacade.finishLoading();
	}

	/**
	 * Test to load a resource that doesn't exist
	 */
	@Test(expected = ResourceNotFoundException.class)
	public void loadNoneExistingResource() {
		PlayerActorDef tempDef = new PlayerActorDef();
		ResourceCacheFacade.load(mScene, tempDef.getId(), false);
		ResourceCacheFacade.finishLoading();
	}

	/**
	 * Test to load a definition with some definition dependencies
	 */
	@Test
	public void loadDefWithDefDependencies() {
		// Load
		ResourceCacheFacade.load(mScene, mUsingDefDeps.getId(), true);
		ResourceCacheFacade.finishLoading();
		assertEquals("Loaded actors with dependencies", 4, ResourceCacheFacade.getLoadedCount());
	}

	static {
		Debug.JUNIT_TEST = true;
	}

	/** Stub scene */
	private static Scene mScene = new SceneStub();
	/** Regular actor with no dependencies */
	private static PlayerActorDef mDef1 = new PlayerActorDef();
	/** Regular actor with no dependencies */
	private static PlayerActorDef mDef2 = new PlayerActorDef();
	/** Actor using dependencies */
	private static PlayerActorDef mUsingDefDeps = new PlayerActorDef();
	/** Actor dependency with a dependency */
	private static PlayerActorDef mDepWithDep = new PlayerActorDef();
	/** Actor dependency */
	private static PlayerActorDef mDep = new PlayerActorDef();
	/** Actor under dependency, i.e. UsingDefDeps -> DepWithDep -> UnderDep */
	private static PlayerActorDef mUnderDep = new PlayerActorDef();

	/** Total number of actors */
	private static int ACTORS = 6;
}
