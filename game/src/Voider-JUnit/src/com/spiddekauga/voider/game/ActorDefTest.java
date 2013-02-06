package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

/**
 * Tester for ActorDef class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorDefTest {

	/**
	 * Initializes the native libraries
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		LwjglNativesLoader.load();
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.ActorDef#write(com.spiddekauga.utils.Json)}.
	 */
	@Test
	public void writeRead() {
		// No Fixture
		ActorDef actor = new PlayerActorDef();

		Json json = new Json();
		String jsonString = json.toJson(actor);
		ActorDef testActor = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("ActorDefs equals", actor, testActor);
		assertEquals("ActorDefs' fixture size", 1, testActor.getFixtureDefs().size());
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());


		/** @TODO write other tests for fixtures */
		actor.clearFixtures();
		for (int i = 0; i < 5; ++i) {
			actor.addFixtureDef(new FixtureDef());
		}

		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);
		assertEquals("ActorDefs' fixtureDefs", 5, testActor.getFixtureDefs().size());
	}

}
