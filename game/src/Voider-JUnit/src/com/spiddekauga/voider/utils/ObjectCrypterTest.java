package com.spiddekauga.voider.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.JsonWrapper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

/**
 * Test cases for encrypting objects
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ObjectCrypterTest {

	/**
	 * Initializes the config class that has the key
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		LwjglNativesLoader.load();
		Config.init();
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
	 * Test method for encrypt and decrypt
	 */
	@Test
	public void encryptDecrypt() {
		ActorDef actorDef = new PlayerActorDef();

		Json json = new JsonWrapper();
		String jsonString = json.toJson(actorDef);


		// Encrypt
		ObjectCrypter crypter = new ObjectCrypter(Config.Crypto.getFileKey());

		byte[] encrypted = null;
		try {
			encrypted = crypter.encrypt(jsonString);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			fail("InvalidKeyException\n" + e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			fail("InvalidAlgorithmParameterException\n" + e.getMessage());
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			fail("InvalidBlockSizeException\n" + e.getMessage());
		} catch (BadPaddingException e) {
			e.printStackTrace();
			fail("BadPaddingException\n" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException\n" + e.getMessage());
		} catch (ShortBufferException e) {
			e.printStackTrace();
			fail("ShortBufferException\n" + e.getMessage());
		}
		assertTrue("Encrypted not null", encrypted != null);


		// Decrypt
		String decryptedJson = null;
		try {
			decryptedJson = crypter.decrypt(encrypted, String.class);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			fail("InvalidKeyException\n" + e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			fail("InvalidAlgorithmParameterException\n" + e.getMessage());
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			fail("InvalidBlockSizeException\n" + e.getMessage());
		} catch (BadPaddingException e) {
			e.printStackTrace();
			fail("BadPaddingException\n" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException\n" + e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("ClassNotFoundException\n" + e.getMessage());
		}

		assertTrue("decrypted json not null", decryptedJson != null);
		assertEquals("decrypted json equal original json string", decryptedJson, jsonString);

		ActorDef newActorDef = json.fromJson(PlayerActorDef.class, decryptedJson);

		assertEquals("new actor def equals original", newActorDef, actorDef);
		assertEquals("ActorDefs' max life", newActorDef.getMaxLife(), actorDef.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", newActorDef.getName(), actorDef.getName());
	}
}
