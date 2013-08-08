package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.spiddekauga.utils.JsonWrapper; import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.utils.ObjectCrypter;

/**
 * Tests if JsonLoader works correctly with the asset manager
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class JsonLoaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Config.init();
		ResourceNames.useTestPath();

		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
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
	 * Test to load actor definitions
	 */
	@Test
	public void load() {
		ActorDef def1 = new BulletActorDef();
		ActorDef def2 = new PlayerActorDef();
		ActorDef def3 = new PickupActorDef();

		save(def1);
		save(def2);
		save(def3);

		// Try to actually load the file using the asset manager
		mAssetManager.setLoader(PlayerActorDef.class, new JsonLoader<PlayerActorDef>(new ExternalFileHandleResolver(), PlayerActorDef.class));
		mAssetManager.setLoader(PickupActorDef.class, new JsonLoader<PickupActorDef>(new ExternalFileHandleResolver(), PickupActorDef.class));
		mAssetManager.setLoader(BulletActorDef.class, new JsonLoader<BulletActorDef>(new ExternalFileHandleResolver(), BulletActorDef.class));
		mAssetManager.load(getPath(def1), BulletActorDef.class);
		mAssetManager.load(getPath(def2), PlayerActorDef.class);
		mAssetManager.load(getPath(def3), PickupActorDef.class);

		// Wait until the loading has finished
		mAssetManager.finishLoading();

		assertTrue("Def 1 loaded()", mAssetManager.isLoaded(getPath(def1)));
		assertTrue("Def 2 loaded()", mAssetManager.isLoaded(getPath(def2)));
		assertTrue("Def 3 loaded()", mAssetManager.isLoaded(getPath(def3)));
		assertEquals("Def 1 equals()", mAssetManager.get(getPath(def1), BulletActorDef.class), def1);
		assertEquals("Def 2 equals()", mAssetManager.get(getPath(def2), PlayerActorDef.class), def2);
		assertEquals("Def 3 equals()", mAssetManager.get(getPath(def3), PickupActorDef.class), def3);

		mAssetManager.unload(getPath(def1));
		mAssetManager.unload(getPath(def2));
		mAssetManager.unload(getPath(def3));

		delete(def1);
		delete(def2);
		delete(def3);
	}

	/**
	 * Just used for easily save a definition
	 * @param resource the definition to save
	 */
	private void save(IResource resource) {
		assert(mCrypter != null);

		Json json = new Json();
		String jsonString = json.toJson(resource);
		try {
			byte[] encryptedDef = mCrypter.encrypt(jsonString);

			String relativePath = getPath(resource);
			FileHandle saveFile = Gdx.files.external(relativePath);

			// File already exist, create backup
			if (saveFile.exists()) {
				saveFile.moveTo(Gdx.files.external(relativePath + Config.File.BACKUP_EXT));
			}

			// Save the file
			saveFile.writeBytes(encryptedDef, false);

		} catch (Exception e) {
			Gdx.app.error("Save resource", "Could not encrypt message. Your file has not been saved!");
		}
	}

	/**
	 * Removes the specified definition from the hard drive. We don't want it to take up
	 * place and be amongst some of the valid actors.
	 * @param resource the resource to remove from the hard drive.
	 */
	private void delete(IResource resource) {
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
	private String getPath(IResource resource) {
		try {
			return ResourceNames.getDirPath(resource.getClass()) + resource.getId().toString();
		} catch (Exception e) {

		}
		return null;
	}

	/** For encrypting messages */
	private static ObjectCrypter mCrypter = null;
	/** The asset manager that actually handles the loading */
	private static AssetManager mAssetManager = null;
}
