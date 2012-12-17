package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.game.actors.ActorTypes;

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
		Config.init();
		ResourceSaver.init();
		ResourceNames.useTestPath();
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();

		mAssetManager = new AssetManager();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		mAssetManager.dispose();
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceDependencyLoader#load(java.util.UUID, java.lang.Class)}.
	 */
	@Test
	public void loadUnload() {
		ActorDef def = new ActorDef(100, ActorTypes.BULLET, null, "bullet", null);
		ActorDef dep1 = new ActorDef(200, ActorTypes.PLAYER, null, "player", null);
		ActorDef dep2 = new ActorDef(300, ActorTypes.BOSS, null, "boss", null);
		ActorDef depdep = new ActorDef(1000, ActorTypes.PICKUP, null, "pickup", null);

		dep1.addDependency(depdep);
		def.addDependency(dep1);
		def.addDependency(dep2);

		ResourceSaver.save(def);
		ResourceSaver.save(dep1);
		ResourceSaver.save(dep2);
		ResourceSaver.save(depdep);

		ResourceDependencyLoader dependencyLoader = new ResourceDependencyLoader(mAssetManager);

		// Try to load all actors via resource depnedency loader
		try {
			dependencyLoader.load(def.getId(), def.getClass());

			// Wait until all resources have been loaded
			while (!dependencyLoader.update() || !mAssetManager.update()) {
				// Do nothing
			}

			assertTrue("def is loaded", mAssetManager.isLoaded(getPath(def)));
			assertTrue("dep1 is loaded", mAssetManager.isLoaded(getPath(dep1)));
			assertTrue("dep2 is loaded", mAssetManager.isLoaded(getPath(dep2)));
			assertTrue("depdep is loaded", mAssetManager.isLoaded(getPath(depdep)));
			assertEquals("number of resources", mAssetManager.getLoadedAssets(), 4);
		} catch (UndefinedResourceTypeException e) {
			e.printStackTrace();
			fail("Exception when loading!");
		}

		// Unload
		dependencyLoader.unload(def);
		assertTrue("def is loaded", !mAssetManager.isLoaded(getPath(def)));
		assertTrue("dep1 is loaded", !mAssetManager.isLoaded(getPath(dep1)));
		assertTrue("dep2 is loaded", !mAssetManager.isLoaded(getPath(dep2)));
		assertTrue("depdep is loaded", !mAssetManager.isLoaded(getPath(depdep)));
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
			FileHandle saveFile = Gdx.files.external(getPath(resource));

			saveFile.delete();

		} catch (Exception e) {
			Gdx.app.error("Delete file", "Could not delete your file!");
		}
	}

	/**
	 * Returns the path to the resource
	 * @param resource the resource we want the path to
	 * @return the path to the resource
	 */
	private static String getPath(IResource resource) {
		try {
			return ResourceNames.getDirPath(resource.getClass()) + resource.getId().toString();
		} catch (Exception e) {

		}
		return null;
	}

	/** Asset manager for all files */
	private static AssetManager mAssetManager = null;
}
