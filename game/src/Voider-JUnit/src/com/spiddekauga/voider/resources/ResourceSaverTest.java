package com.spiddekauga.voider.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.repo.ApplicationStub;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.repo.ResourceLocalRepo;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tests the ResourceSaver if it works to save files.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceSaverTest {

	/**
	 * Initialize the saver
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Gdx.app = new ApplicationStub();
		Config.Debug.JUNIT_TEST = true;
	}

	/**
	 * Tears down the class
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Config.dispose();
	}

	/**
	 * Test method for
	 * {@link com.spiddekauga.voider.repo.ResourceRepo#save(com.spiddekauga.voider.resources.IResource[])}
	 * .
	 */
	@Test
	public void testSave() {
		Def def = new PickupActorDef();

		// Test to save it and then load
		// ResourceSaver.save(def);

		String relativePath = ResourceLocalRepo.getFilepath(def);
		FileHandle savedFile = Gdx.files.external(relativePath);
		assertTrue("saved file exist", savedFile.exists());


		byte[] encryptedDef = savedFile.readBytes();
		ObjectCrypter crypter = new ObjectCrypter(Config.Crypto.getFileKey());
		byte[] decryptedDef = null;
		try {
			decryptedDef = crypter.decrypt(encryptedDef, byte[].class);
		} catch (Exception e) {
			fail("Could not decrypt file");
		}

		Kryo kryo = Pools.kryo.obtain();
		Input input = new Input(decryptedDef);
		Def savedDef = kryo.readObject(input, PickupActorDef.class);
		assertEquals("saved def equals()", def, savedDef);


		// Save it again, now a new revision should be created
		def.addDependency(InternalNames.IMAGE_SPLASH_SCREEN);
		// ResourceSaver.save(def);

		relativePath = ResourceLocalRepo.getFilepath(def);
		savedFile = Gdx.files.external(relativePath);
		encryptedDef = savedFile.readBytes();

		decryptedDef = null;
		try {
			decryptedDef = crypter.decrypt(encryptedDef, byte[].class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		input = new Input(decryptedDef);
		savedDef = kryo.readObject(input, PickupActorDef.class);
		assertEquals("new saved file shall have one dependency", 1, savedDef.getInternalDependencies().size());


		// Delete the files
		// ResourceSaver.clearResources();
	}

}
