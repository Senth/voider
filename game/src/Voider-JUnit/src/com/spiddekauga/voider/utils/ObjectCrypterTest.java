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

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ActorDef;
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
	public static void init() {
		Config.init();
	}

	/**
	 * Test method for encrypt and decrypt
	 */
	@Test
	public void encryptDecrypt() {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = null;
		fixtureDef.density = 0.1f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.3f;
		ActorDef actorDef = new PlayerActorDef(100, null, "player", fixtureDef);

		Json json = new Json();
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
			decryptedJson = (String) crypter.decrypt(encrypted);
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
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().shape, actorDef.getFixtureDef().shape);
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().friction, actorDef.getFixtureDef().friction, 0.0f);
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().restitution, actorDef.getFixtureDef().restitution, 0.0f);
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().density, actorDef.getFixtureDef().density, 0.0f);
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().isSensor, actorDef.getFixtureDef().isSensor);
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().filter.categoryBits, actorDef.getFixtureDef().filter.categoryBits);
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().filter.groupIndex, actorDef.getFixtureDef().filter.groupIndex);
		assertEquals("ActorDefs' fixture def", newActorDef.getFixtureDef().filter.maskBits, actorDef.getFixtureDef().filter.maskBits);
		assertEquals("ActorDefs' max life", newActorDef.getMaxLife(), actorDef.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", newActorDef.getName(), actorDef.getName());
	}
}
