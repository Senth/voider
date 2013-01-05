package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
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
		assertNull("ActorDefs' fixture null", testActor.getFixtureDef());
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());


		// Fixture, but no shape
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = null;
		fixtureDef.density = 1.5f;
		fixtureDef.isSensor = true;
		fixtureDef.restitution = 70.6f;
		fixtureDef.friction = 15f;

		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("ActorDefs equals", actor, testActor);
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());

		// Appended tests
		assertNotNull("Fixture not null", testActor.getFixtureDef());
		assertEquals("Fixture friction", testActor.getFixtureDef().friction, actor.getFixtureDef().friction, 0.0f);
		assertEquals("Fixture restitution", testActor.getFixtureDef().restitution, actor.getFixtureDef().restitution, 0.0f);
		assertEquals("Fixture density", testActor.getFixtureDef().density, actor.getFixtureDef().density, 0.0f);
		assertEquals("Fixture isSensor", testActor.getFixtureDef().isSensor, actor.getFixtureDef().isSensor);
		assertEquals("Filter category bits", testActor.getFixtureDef().filter.categoryBits, actor.getFixtureDef().filter.categoryBits);
		assertEquals("Filter group index", testActor.getFixtureDef().filter.groupIndex, actor.getFixtureDef().filter.groupIndex);
		assertEquals("Filter mask bits", testActor.getFixtureDef().filter.maskBits, actor.getFixtureDef().filter.maskBits);
		assertNull("Shape null", testActor.getFixtureDef().shape);


		// CIRCLE SHAPE
		CircleShape circle = new CircleShape();
		circle.setRadius(2f);
		circle.setPosition(new Vector2(1, 2));
		fixtureDef.shape = circle;

		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("ActorDefs equals", actor, testActor);
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());
		assertNotNull("Fixture not null", testActor.getFixtureDef());
		assertEquals("Fixture friction", testActor.getFixtureDef().friction, actor.getFixtureDef().friction, 0.0f);
		assertEquals("Fixture restitution", testActor.getFixtureDef().restitution, actor.getFixtureDef().restitution, 0.0f);
		assertEquals("Fixture density", testActor.getFixtureDef().density, actor.getFixtureDef().density, 0.0f);
		assertEquals("Fixture isSensor", testActor.getFixtureDef().isSensor, actor.getFixtureDef().isSensor);
		assertEquals("Filter category bits", testActor.getFixtureDef().filter.categoryBits, actor.getFixtureDef().filter.categoryBits);
		assertEquals("Filter group index", testActor.getFixtureDef().filter.groupIndex, actor.getFixtureDef().filter.groupIndex);
		assertEquals("Filter mask bits", testActor.getFixtureDef().filter.maskBits, actor.getFixtureDef().filter.maskBits);

		// Appended tests
		assertNotNull("Shape not null", testActor.getFixtureDef().shape);
		assertEquals("Shape type", testActor.getFixtureDef().shape.getType(), Shape.Type.Circle);
		CircleShape testCircle = (CircleShape) testActor.getFixtureDef().shape;
		assertEquals("Circle radius", testCircle.getRadius(), 2f, 0.0f);
		assertEquals("Circle position", testCircle.getPosition(), new Vector2(1, 2));

		// Cleanup
		circle.dispose();
		testCircle.dispose();


		// POLYGON SHAPE
		PolygonShape polygon = new PolygonShape();
		Vector2[] vertices = new Vector2[3];
		vertices[0] = new Vector2(10, 11);
		vertices[1] = new Vector2(20, -22);
		vertices[2] = new Vector2(300, 13);
		polygon.set(vertices);
		fixtureDef.shape = polygon;

		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("ActorDefs equals", actor, testActor);
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());
		assertNotNull("Fixture not null", testActor.getFixtureDef());
		assertEquals("Fixture friction", testActor.getFixtureDef().friction, actor.getFixtureDef().friction, 0.0f);
		assertEquals("Fixture restitution", testActor.getFixtureDef().restitution, actor.getFixtureDef().restitution, 0.0f);
		assertEquals("Fixture density", testActor.getFixtureDef().density, actor.getFixtureDef().density, 0.0f);
		assertEquals("Fixture isSensor", testActor.getFixtureDef().isSensor, actor.getFixtureDef().isSensor);
		assertEquals("Filter category bits", testActor.getFixtureDef().filter.categoryBits, actor.getFixtureDef().filter.categoryBits);
		assertEquals("Filter group index", testActor.getFixtureDef().filter.groupIndex, actor.getFixtureDef().filter.groupIndex);
		assertEquals("Filter mask bits", testActor.getFixtureDef().filter.maskBits, actor.getFixtureDef().filter.maskBits);

		// Appended tests
		assertNotNull("Shape not null", testActor.getFixtureDef().shape);
		assertEquals("Shape type", testActor.getFixtureDef().shape.getType(), Shape.Type.Polygon);
		PolygonShape testPolygon = (PolygonShape) testActor.getFixtureDef().shape;
		assertEquals("number of vertices", 3, testPolygon.getVertexCount());
		Vector2 testVertex = new Vector2();
		testPolygon.getVertex(0, testVertex);
		assertEquals("Polygon vertex 1", testVertex, vertices[0]);
		testPolygon.getVertex(1, testVertex);
		assertEquals("Polygon vertex 2", testVertex, vertices[1]);
		testPolygon.getVertex(2, testVertex);
		assertEquals("Polygon vertex 3", testVertex, vertices[2]);

		// Cleanup
		polygon.dispose();
		testPolygon.dispose();


		// POLYGON SHAPE (null vertices)
		polygon = new PolygonShape();
		fixtureDef.shape = polygon;

		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);

		// Appended tests
		assertNotNull("Shape not null", testActor.getFixtureDef().shape);
		assertEquals("Shape type", testActor.getFixtureDef().shape.getType(), Shape.Type.Polygon);
		testPolygon = (PolygonShape) testActor.getFixtureDef().shape;
		assertEquals("number of vertices", 0, testPolygon.getVertexCount());

		// Cleanup
		polygon.dispose();
		testPolygon.dispose();


		// EDGE SHAPE
		EdgeShape edge = new EdgeShape();
		edge.set(new Vector2(1, 2), new Vector2(11, 12));
		fixtureDef.shape = edge;

		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("ActorDefs equals", actor, testActor);
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());
		assertNotNull("Fixture not null", testActor.getFixtureDef());
		assertEquals("Fixture friction", testActor.getFixtureDef().friction, actor.getFixtureDef().friction, 0.0f);
		assertEquals("Fixture restitution", testActor.getFixtureDef().restitution, actor.getFixtureDef().restitution, 0.0f);
		assertEquals("Fixture density", testActor.getFixtureDef().density, actor.getFixtureDef().density, 0.0f);
		assertEquals("Fixture isSensor", testActor.getFixtureDef().isSensor, actor.getFixtureDef().isSensor);
		assertEquals("Filter category bits", testActor.getFixtureDef().filter.categoryBits, actor.getFixtureDef().filter.categoryBits);
		assertEquals("Filter group index", testActor.getFixtureDef().filter.groupIndex, actor.getFixtureDef().filter.groupIndex);
		assertEquals("Filter mask bits", testActor.getFixtureDef().filter.maskBits, actor.getFixtureDef().filter.maskBits);

		// Appended tests
		assertNotNull("Shape not null", testActor.getFixtureDef().shape);
		assertEquals("Shape type", testActor.getFixtureDef().shape.getType(), Shape.Type.Edge);
		EdgeShape testEdge = (EdgeShape) testActor.getFixtureDef().shape;
		testEdge.getVertex1(testVertex);
		assertEquals("Edge vertex 1", testVertex, new Vector2(1, 2));
		testEdge.getVertex2(testVertex);
		assertEquals("Edge vertex 2", testVertex, new Vector2(11, 12));

		// Cleanup
		edge.dispose();
		testEdge.dispose();


		// CHAIN SHAPE
		ChainShape chain = new ChainShape();
		vertices = new Vector2[4];
		vertices[0] = new Vector2(2,0);
		vertices[1] = new Vector2(10,10);
		vertices[2] = new Vector2(5, 10);
		vertices[3] = new Vector2(0, 10);
		chain.createLoop(vertices);
		fixtureDef.shape = chain;

		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);

		assertEquals("ActorDefs equals", actor, testActor);
		assertEquals("ActorDefs' max life", testActor.getMaxLife(), actor.getMaxLife(), 0.0f);
		assertEquals("ActorDefs' name", testActor.getName(), actor.getName());
		assertNotNull("Fixture not null", testActor.getFixtureDef());
		assertEquals("Fixture friction", testActor.getFixtureDef().friction, actor.getFixtureDef().friction, 0.0f);
		assertEquals("Fixture restitution", testActor.getFixtureDef().restitution, actor.getFixtureDef().restitution, 0.0f);
		assertEquals("Fixture density", testActor.getFixtureDef().density, actor.getFixtureDef().density, 0.0f);
		assertEquals("Fixture isSensor", testActor.getFixtureDef().isSensor, actor.getFixtureDef().isSensor);
		assertEquals("Filter category bits", testActor.getFixtureDef().filter.categoryBits, actor.getFixtureDef().filter.categoryBits);
		assertEquals("Filter group index", testActor.getFixtureDef().filter.groupIndex, actor.getFixtureDef().filter.groupIndex);
		assertEquals("Filter mask bits", testActor.getFixtureDef().filter.maskBits, actor.getFixtureDef().filter.maskBits);

		// Appended tests
		assertNotNull("Shape not null", testActor.getFixtureDef().shape);
		assertEquals("Shape type", Shape.Type.Chain, testActor.getFixtureDef().shape.getType());
		ChainShape testChain = (ChainShape) testActor.getFixtureDef().shape;

		// +1 because that it's a loop
		assertEquals("chain size", vertices.length + 1, testChain.getVertexCount());
		for (int i = 0; i < vertices.length; ++i) {
			testChain.getVertex(i, testVertex);
			assertEquals("vertex 1", vertices[i], testVertex);
		}
		chain.dispose();
		testActor.dispose();

		// CHAIN SHAPE (null vertices)
		chain = new ChainShape();
		fixtureDef.shape = chain;

		actor = new PlayerActorDef(100, null, "player", fixtureDef);
		jsonString = json.toJson(actor);
		testActor = json.fromJson(PlayerActorDef.class, jsonString);

		// Appended tests
		assertNotNull("Shape not null", testActor.getFixtureDef().shape);
		assertEquals("Shape type", Shape.Type.Chain, testActor.getFixtureDef().shape.getType());
		testChain = (ChainShape) testActor.getFixtureDef().shape;

		assertEquals("chain size", 0, testChain.getVertexCount());
		chain.dispose();
		testActor.dispose();
	}

}
