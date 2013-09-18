package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.minlog.Log;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.LoadingTextScene;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

/**
 * Tests the resource depnedency loader
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceDependencyLoaderTest {

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
		ResourceCacheFacade.init();

		mAssetManager = new AssetManager();
		mAssetManager.setLoader(PlayerActorDef.class, new JsonLoaderAsync<PlayerActorDef>(new ExternalFileHandleResolver(), PlayerActorDef.class));
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
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceDependencyLoader#load(com.spiddekauga.voider.scene.Scene, java.util.UUID, java.lang.Class, int)}.
	 */
	@Test
	public void loadUnload() {
		ActorDef def = new PlayerActorDef();
		ActorDef dep1 = new PlayerActorDef();
		ActorDef dep2 = new PlayerActorDef();
		ActorDef depdep = new PlayerActorDef();

		dep1.addDependency(depdep);
		def.addDependency(dep1);
		def.addDependency(dep2);

		ResourceSaver.save(def);
		ResourceSaver.save(dep1);
		ResourceSaver.save(dep2);
		ResourceSaver.save(depdep);

		ResourceDependencyLoader dependencyLoader = new ResourceDependencyLoader(mAssetManager);
		LoadingTextScene scene = new LoadingTextScene("");

		// Try to load all actors via resource dependency loader
		try {
			dependencyLoader.load(scene, def.getId(), def.getClass(), def.getRevision());

			// Wait until all resources have been loaded
			while (!dependencyLoader.update() || !mAssetManager.update()) {
				// Do nothing
			}

			assertTrue("def is loaded", mAssetManager.isLoaded(ResourceDatabase.getFilePath(def)));
			assertTrue("dep1 is loaded", mAssetManager.isLoaded(ResourceDatabase.getFilePath(dep1)));
			assertTrue("dep2 is loaded", mAssetManager.isLoaded(ResourceDatabase.getFilePath(dep2)));
			assertTrue("depdep is loaded", mAssetManager.isLoaded(ResourceDatabase.getFilePath(depdep)));
			assertEquals("number of resources", mAssetManager.getLoadedAssets(), 4);
		} catch (UndefinedResourceTypeException e) {
			e.printStackTrace();
			fail("Exception when loading!");
		}

		// Unload
		dependencyLoader.unload(scene, def);
		assertTrue("def is loaded", !mAssetManager.isLoaded(ResourceDatabase.getFilePath(def)));
		assertTrue("dep1 is loaded", !mAssetManager.isLoaded(ResourceDatabase.getFilePath(dep1)));
		assertTrue("dep2 is loaded", !mAssetManager.isLoaded(ResourceDatabase.getFilePath(dep2)));
		assertTrue("depdep is loaded", !mAssetManager.isLoaded(ResourceDatabase.getFilePath(depdep)));
		assertEquals("number of resources", mAssetManager.getLoadedAssets(), 0);

		delete(def);
		delete(dep1);
		delete(dep2);
		delete(depdep);
	}

	/**
	 * Removes the specified definition from the hard drive. We don't want it to take up
	 * place and be amongst some of the valid actors.
	 * @param resource the resource to remove from the hard drive.
	 */
	public static void delete(IResource resource) {
		try {
			FileHandle saveFile = Gdx.files.external(ResourceDatabase.getFilePath(resource));

			saveFile.delete();

		} catch (Exception e) {
			Log.error("Could not delete your file!");
		}
	}

	/** Asset manager for all files */
	private static AssetManager mAssetManager = null;
}
