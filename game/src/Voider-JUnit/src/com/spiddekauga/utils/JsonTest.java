package com.spiddekauga.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.Def;
/**
 * Tests the extended json class
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class JsonTest {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LwjglNativesLoader.load();
	}


	/**
	 * Tests maps with keys
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void maps() {
		Json json = new JsonWrapper();

		// String - Integer
		ObjectMap<String, Integer> stringMap = new ObjectMap<String, Integer>();
		stringMap.put("First", 1);
		stringMap.put("Second", 2);
		String jsonString = json.toJson(stringMap);
		ObjectMap<String, Integer> jsonStringMap = json.fromJson(ObjectMap.class, jsonString);
		assertTrue(jsonStringMap.containsKey("First"));
		assertTrue(jsonStringMap.containsKey("Second"));
		assertTrue(jsonStringMap.containsValue(1, false));
		assertTrue(jsonStringMap.containsValue(2, false));


		// Integer - Integer
		ObjectMap<Integer, Integer> intMap = new ObjectMap<Integer, Integer>();
		intMap.put(1, 1);
		intMap.put(2, 2);
		jsonString = json.toJson(intMap);
		ObjectMap<Integer, Integer> jsonIntMap = json.fromJson(ObjectMap.class, jsonString);
		assertTrue(jsonIntMap.containsKey(1));
		assertTrue(jsonIntMap.containsKey(2));
		assertTrue(jsonIntMap.containsValue(1, false));
		assertTrue(jsonIntMap.containsValue(2, false));
	}

	/**
	 * Tests if the extended json can write/read uuid correctly
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void uuid() {
		Json json = new JsonWrapper();
		String jsonString = null;
		UUID testUuid = UUID.randomUUID();
		try {
			jsonString = json.toJson(testUuid);
			assertTrue("Could write uuid to json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not write uuid to json");
		}

		UUID jsonUuid = null;
		try {
			jsonUuid = json.fromJson(UUID.class, jsonString);
			assertTrue("Could read uuid from json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read uuid from json");
		}

		assertEquals("Same uuid", testUuid, jsonUuid);


		// Test null
		jsonString = null;
		testUuid = null;
		try {
			jsonString = json.toJson(testUuid);
			assertTrue("Could write null uuid to json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not write null uuid to json");
		}

		jsonUuid = null;
		try {
			jsonUuid = json.fromJson(UUID.class, jsonString);
			assertTrue("Could read null uuid from json", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read null uuid from json");
		}

		assertNull("UUID was null", jsonUuid);


		Array<Def> testArray = new Array<Def>();
		testArray.add(new LevelDef());
		jsonString = json.toJson(testArray);


		// Test when inside a collection
		Array<UUID> uuidArray = new Array<UUID>();
		uuidArray.add(UUID.randomUUID());
		jsonString = json.toJson(uuidArray);
		Array<UUID> jsonArray = json.fromJson(Array.class, jsonString);
		assertEquals("Array the same", uuidArray.get(0), jsonArray.get(0));


		// Test when used as a key
		ObjectMap<UUID, String> uuidMap = new ObjectMap<UUID, String>();
		testUuid = UUID.randomUUID();
		uuidMap.put(testUuid, "test");
		jsonString = json.toJson(uuidMap);
		ObjectMap<UUID, String> jsonMap = json.fromJson(ObjectMap.class, jsonString);
		assertTrue(jsonMap.containsKey(testUuid));
	}

	/**
	 * Tests box2d shapes
	 */
	@Test
	public void box2dShape() {
		// CIRCLE SHAPE
		CircleShape circle = new CircleShape();
		circle.setRadius(2f);
		circle.setPosition(new Vector2(1, 2));

		Json json = new JsonWrapper();
		String jsonString = json.toJson(circle);
		Shape testShape = json.fromJson(Shape.class, jsonString);


		// Appended tests
		assertNotNull("Shape not null", testShape);
		assertEquals("Shape type", testShape.getType(), Shape.Type.Circle);
		CircleShape testCircle = (CircleShape) testShape;
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


		jsonString = json.toJson(polygon);
		testShape = json.fromJson(Shape.class, jsonString);


		// Appended tests
		assertNotNull("Shape not null", testShape);
		assertEquals("Shape type", testShape.getType(), Shape.Type.Polygon);
		PolygonShape testPolygon = (PolygonShape) testShape;
		assertEquals("number of vertices", 3, testPolygon.getVertexCount());
		Vector2 testVertex = new Vector2();
		testPolygon.getVertex(0, testVertex);
		assertEquals("Polygon vertex 1", testVertex, vertices[2]);
		testPolygon.getVertex(1, testVertex);
		assertEquals("Polygon vertex 2", testVertex, vertices[0]);
		testPolygon.getVertex(2, testVertex);
		assertEquals("Polygon vertex 3", testVertex, vertices[1]);

		// Cleanup
		polygon.dispose();
		testPolygon.dispose();


		// POLYGON SHAPE (null vertices)
		polygon = new PolygonShape();
		jsonString = json.toJson(polygon);
		testShape = json.fromJson(Shape.class, jsonString);

		// Appended tests
		assertNotNull("Shape not null", testShape);
		assertEquals("Shape type", testShape.getType(), Shape.Type.Polygon);
		testPolygon = (PolygonShape) testShape;
		assertEquals("number of vertices", 0, testPolygon.getVertexCount());

		// Cleanup
		polygon.dispose();
		testPolygon.dispose();


		// EDGE SHAPE
		EdgeShape edge = new EdgeShape();
		edge.set(new Vector2(1, 2), new Vector2(11, 12));
		jsonString = json.toJson(edge);
		testShape = json.fromJson(Shape.class, jsonString);


		// Appended tests
		assertNotNull("Shape not null", testShape);
		assertEquals("Shape type", testShape.getType(), Shape.Type.Edge);
		EdgeShape testEdge = (EdgeShape) testShape;
		testEdge.getVertex1(testVertex);
		assertEquals("Edge vertex 1", testVertex, new Vector2(1, 2));
		testEdge.getVertex2(testVertex);
		assertEquals("Edge vertex 2", testVertex, new Vector2(11, 12));

		// Cleanup
		edge.dispose();
		testEdge.dispose();


		// CHAIN SHAPE (looped)
		ChainShape chain = new ChainShape();
		vertices = new Vector2[4];
		vertices[0] = new Vector2(2,0);
		vertices[1] = new Vector2(10,10);
		vertices[2] = new Vector2(5, 10);
		vertices[3] = new Vector2(0, 10);
		chain.createLoop(vertices);

		jsonString = json.toJson(chain);
		testShape = json.fromJson(Shape.class, jsonString);


		// Appended tests
		assertNotNull("Shape not null", testShape);
		assertEquals("Shape type", Shape.Type.Chain, testShape.getType());
		ChainShape testChain = (ChainShape) testShape;

		// +1 because that it's a loop
		assertEquals("chain size", vertices.length + 1, testChain.getVertexCount());
		for (int i = 0; i < vertices.length; ++i) {
			testChain.getVertex(i, testVertex);
			assertEquals("vertex 1", vertices[i], testVertex);
		}
		chain.dispose();
		testShape.dispose();


		// CHAIN SHAPE (chained)
		chain = new ChainShape();
		vertices = new Vector2[4];
		vertices[0] = new Vector2(2,0);
		vertices[1] = new Vector2(10,10);
		vertices[2] = new Vector2(5, 10);
		vertices[3] = new Vector2(0, 10);
		chain.createChain(vertices);

		jsonString = json.toJson(chain);
		testShape = json.fromJson(Shape.class, jsonString);

		// Appended tests
		assertNotNull("Shape not null", testShape);
		assertEquals("Shape type", Shape.Type.Chain, testShape.getType());
		testChain = (ChainShape) testShape;

		assertEquals("chain size", 4, testChain.getVertexCount());
		for (int i = 0; i < vertices.length; ++i) {
			testChain.getVertex(i, testVertex);
			assertEquals("vertex 1", vertices[i], testVertex);
		}
		chain.dispose();
		testShape.dispose();


		// CHAIN SHAPE (null vertices)
		chain = new ChainShape();

		jsonString = json.toJson(chain);
		testShape = json.fromJson(Shape.class, jsonString);

		// Appended tests
		assertNotNull("Shape not null", testShape);
		assertEquals("Shape type", Shape.Type.Chain, testShape.getType());
		testChain = (ChainShape) testShape;

		assertEquals("chain size", 0, testChain.getVertexCount());
		chain.dispose();
	}

	/**
	 * Tests box2d fixtureDef
	 */
	@Test
	public void box2dFixtureDef() {
		// Fixture, but no shape
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = null;
		fixtureDef.density = 1.5f;
		fixtureDef.isSensor = true;
		fixtureDef.restitution = 70.6f;
		fixtureDef.friction = 15f;

		Json json = new JsonWrapper();
		String jsonString = json.toJson(fixtureDef);
		FixtureDef testFixtureDef = json.fromJson(FixtureDef.class, jsonString);

		assertNotNull("Fixture not null", testFixtureDef);
		assertEquals("Fixture friction", testFixtureDef.friction, fixtureDef.friction, 0.0f);
		assertEquals("Fixture restitution", testFixtureDef.restitution, fixtureDef.restitution, 0.0f);
		assertEquals("Fixture density", testFixtureDef.density, fixtureDef.density, 0.0f);
		assertEquals("Fixture isSensor", testFixtureDef.isSensor, fixtureDef.isSensor);
		assertEquals("Filter category bits", testFixtureDef.filter.categoryBits, fixtureDef.filter.categoryBits);
		assertEquals("Filter group index", testFixtureDef.filter.groupIndex, fixtureDef.filter.groupIndex);
		assertEquals("Filter mask bits", testFixtureDef.filter.maskBits, fixtureDef.filter.maskBits);
		assertNull("Shape null", testFixtureDef.shape);

		// Fixture, with shape
		CircleShape circleShape = new CircleShape();
		circleShape.setPosition(new Vector2(1, 2));
		circleShape.setRadius(1.5f);
		fixtureDef.shape = circleShape;

		jsonString = json.toJson(fixtureDef);
		testFixtureDef = json.fromJson(FixtureDef.class, jsonString);

		assertNotNull("Shape null", testFixtureDef.shape);
		assertEquals("Shape type", Shape.Type.Circle, circleShape.getType());

	}

}
