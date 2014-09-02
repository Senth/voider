package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.repo.ApplicationStub;
import com.spiddekauga.voider.repo.SqliteResetter;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneStub;

/**
 * Tests the resource depnedency loader
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceDependencyLoaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Gdx.app = new ApplicationStub();
		Config.Debug.JUNIT_TEST = true;

		try {
			Field fAssetManager = ResourceCacheFacade.class.getDeclaredField("mAssetManager");
			fAssetManager.setAccessible(true);
			mAssetManager = (AssetManager) fAssetManager.get(null);
			mAssetManager.clear();
		} catch (Exception e) {
			// Does nothing
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		mAssetManager.dispose();
		Config.dispose();
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.repo.ResourceDependencyLoader#load(com.spiddekauga.voider.scene.Scene, java.util.UUID, int)}.
	 */
	@Test
	public void loadUnload() {
		SqliteResetter.reset();

		ActorDef loadingDef = new PlayerActorDef();
		loadingDef.setName("loading def");
		ActorDef dep1 = new PlayerActorDef();
		dep1.setName("dep1 with dep");
		ActorDef dep2 = new PlayerActorDef();
		dep2.setName("dep2");
		ActorDef depdep = new PlayerActorDef();
		depdep.setName("dependency of a dependency");

		dep1.addDependency(depdep);
		loadingDef.addDependency(dep1);
		loadingDef.addDependency(dep2);

		//		ResourceSaver.save(loadingDef);
		//		ResourceSaver.save(dep1);
		//		ResourceSaver.save(dep2);
		//		ResourceSaver.save(depdep);

		Scene scene = new SceneStub();

		// Try to load all actors via resource dependency loader
		ResourceCacheFacade.load(scene, loadingDef.getId(), true, loadingDef.getRevision());
		ResourceCacheFacade.finishLoading();

		assertTrue("def is not loaded", mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(loadingDef)));
		assertTrue("dep1 is not loaded", mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(dep1)));
		assertTrue("dep2 is not loaded", mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(dep2)));
		assertTrue("depdep is not loaded", mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(depdep)));
		assertEquals(4, mAssetManager.getLoadedAssets());

		// Unload
		ResourceCacheFacade.unload(scene);
		assertTrue("def is loaded", !mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(loadingDef)));
		assertTrue("dep1 is loaded", !mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(dep1)));
		assertTrue("dep2 is loaded", !mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(dep2)));
		assertTrue("depdep is loaded", !mAssetManager.isLoaded(ResourceLocalRepo.getFilepath(depdep)));
		assertEquals(0, mAssetManager.getLoadedAssets());

		//		ResourceSaver.clearResources();
	}

	/** Asset manager for all files */
	private static AssetManager mAssetManager = null;
}
