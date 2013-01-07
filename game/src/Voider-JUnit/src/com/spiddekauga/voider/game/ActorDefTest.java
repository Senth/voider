package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
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
		ActorDef actor = new PlayerActorDef(100,  null, "player", null);

		Json json = new Json();
		String jsonString = json.toJson(actor);
		ActorDef testActor = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("ActorDefs equals", actor, testActor);
		assertEquals("ActorDefs' fixture size", 0, testActor.getFixtureDefs().size());
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());
		assertEquals("ActorDefs' fixtureDefs", 0, testActor.getFixtureDefs().size());


		/** @TODO write other tests for fixtures */
		// Fixture, but no shape
		//		FixtureDef fixtureDef = new FixtureDef();
		//		fixtureDef.shape = null;
		//		fixtureDef.density = 1.5f;
		//		fixtureDef.isSensor = true;
		//		fixtureDef.restitution = 70.6f;
		//		fixtureDef.friction = 15f;
		//
		//		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		//		jsonString = json.toJson(actor);
		//		testActor = json.fromJson(PlayerActorDef.class, jsonString);
		//
		//		assertEquals("ActorDefs equals", actor, testActor);
		//		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		//		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());
		//
		//		// Appended tests
		//		assertNotNull("Fixture not null", testActor.getFixtureDef());
		//		assertEquals("Fixture friction", testActor.getFixtureDef().friction, actor.getFixtureDef().friction, 0.0f);
		//		assertEquals("Fixture restitution", testActor.getFixtureDef().restitution, actor.getFixtureDef().restitution, 0.0f);
		//		assertEquals("Fixture density", testActor.getFixtureDef().density, actor.getFixtureDef().density, 0.0f);
		//		assertEquals("Fixture isSensor", testActor.getFixtureDef().isSensor, actor.getFixtureDef().isSensor);
		//		assertEquals("Filter category bits", testActor.getFixtureDef().filter.categoryBits, actor.getFixtureDef().filter.categoryBits);
		//		assertEquals("Filter group index", testActor.getFixtureDef().filter.groupIndex, actor.getFixtureDef().filter.groupIndex);
		//		assertEquals("Filter mask bits", testActor.getFixtureDef().filter.maskBits, actor.getFixtureDef().filter.maskBits);
		//		assertNull("Shape null", testActor.getFixtureDef().shape);



		testActor.dispose();
	}

}
