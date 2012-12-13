package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.game.actors.Types;
import com.spiddekauga.voider.utils.ObjectCrypter;

/**
 * Tests the ResourceSaver if it works to save files.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceSaverTest {

	/**
	 * Initialize the saver
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.init();
		ResourceSaver.init(true);
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.resources.ResourceSaver#save(com.spiddekauga.voider.resources.IUniqueId)}.
	 */
	@Test
	public void testSave() {
		Def def = new ActorDef(100, Types.PICKUP, null, "pickup", null);

		// Test to save it and then load
		ResourceSaver.save(def);

		String relativePath = null;
		try {
			relativePath = Config.File.STORAGE + ResourceNames.getDirPath(def.getClass()) + Config.File.TEST_PREFIX + def.getId().toString();
		} catch (UndefinedResourceTypeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileHandle savedFile = Gdx.files.external(relativePath);

		assertTrue("saved file exist", savedFile.exists());

		byte[] encryptedDef = savedFile.readBytes();

		ObjectCrypter crypter = new ObjectCrypter(Config.Crypto.getFileKey());
		String jsonString = null;
		try {
			jsonString = (String) crypter.decrypt(encryptedDef);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Json json = new Json();
		Def savedDef = json.fromJson(ActorDef.class, jsonString);

		assertEquals("saved def equals()", savedDef, def);


		// Save it again, now a backup should be created
		def.addDependency(ResourceNames.TEXTURE_PLAYER);
		ResourceSaver.save(def);

		savedFile = Gdx.files.external(relativePath);
		encryptedDef = savedFile.readBytes();

		jsonString = null;
		try {
			jsonString = (String) crypter.decrypt(encryptedDef);
		} catch (Exception e) {
			e.printStackTrace();
		}
		savedDef = json.fromJson(ActorDef.class, jsonString);
		assertEquals("new saved file shall have one dependency", savedDef.getInternalDependencies().size(), 1);

		// Check backup, should have 0 dependencies
		FileHandle savedFileBak = Gdx.files.external(relativePath + Config.File.BACKUP_EXT);
		encryptedDef = savedFileBak.readBytes();

		jsonString = null;
		try {
			jsonString = (String) crypter.decrypt(encryptedDef);
		} catch (Exception e) {
			e.printStackTrace();
		}
		savedDef = json.fromJson(ActorDef.class, jsonString);
		assertEquals("backup saved file shall have zero dependencies", savedDef.getInternalDependencies().size(), 0);

		// Delete the files

		savedFile.delete();
		savedFileBak.delete();
	}

}
