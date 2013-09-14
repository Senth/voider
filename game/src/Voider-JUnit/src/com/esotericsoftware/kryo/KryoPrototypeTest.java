package com.esotericsoftware.kryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.spiddekauga.voider.utils.Pools;

/**
 * Prototype tests for Kryo
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("javadoc")
public class KryoPrototypeTest {
	@BeforeClass
	public static void beforeClass() {
		LwjglNativesLoader.load();

		mKryo.setRegistrationRequired(true);

		mKryo.register(float[].class);
		mKryo.register(TestGeneral.class);
		mKryo.register(FixtureDef.class);
		mKryo.register(Filter.class);
		mKryo.register(CircleShape.class, new CircleShapeSerializer());
		mKryo.register(PolygonShape.class, new PolygonShapeSerializer());
		mKryo.register(EdgeShape.class, new EdgeShapeSerializer());
		mKryo.register(ChainShape.class, new ChainShapeSerializer());

		mKryo.register(UUID.class, new FieldSerializer<UUID>(mKryo, UUID.class) {
			@Override
			public UUID create(Kryo kryo, Input input, Class<UUID> type) {
				return UUID.randomUUID();
			}
		});

		mKryo.register(Vector2.class, new FieldSerializer<Vector2>(mKryo, Vector2.class) {
			@Override
			public Vector2 create(Kryo kryo, Input input, Class<Vector2> type) {
				return Pools.vector2.obtain();
			}
		});
		mKryo.register(Vector2[].class);



	}

	private static class CircleShapeSerializer extends Serializer<CircleShape> {
		@Override
		public void write(Kryo kryo, Output output, CircleShape object) {
			kryo.writeObject(output, object.getPosition());
			output.writeFloat(object.getRadius());
		}

		@Override
		public CircleShape read(Kryo kryo, Input input, Class<CircleShape> type) {
			CircleShape circleShape = new CircleShape();
			circleShape.setPosition(kryo.readObject(input, Vector2.class));
			circleShape.setRadius(input.readFloat());
			return circleShape;
		}
	}

	private static class PolygonShapeSerializer extends Serializer<PolygonShape> {
		@Override
		public void write(Kryo kryo, Output output, PolygonShape object) {
			Vector2[] vertices = new Vector2[object.getVertexCount()];
			for (int i = 0; i < vertices.length; i++) {
				vertices[i] = Pools.vector2.obtain();
				object.getVertex(i, vertices[i]);
			}
			kryo.writeObject(output, vertices);

			Pools.vector2.freeAll(vertices);
		}

		@Override
		public PolygonShape read(Kryo kryo, Input input, Class<PolygonShape> type) {
			PolygonShape polygonShape = new PolygonShape();

			Vector2[] vertices = kryo.readObject(input, Vector2[].class);
			if (vertices.length > 0) {
				polygonShape.set(vertices);
			}

			Pools.vector2.freeAll(vertices);

			return polygonShape;
		}
	}

	private static class EdgeShapeSerializer extends Serializer<EdgeShape>  {
		@Override
		public void write(Kryo kryo, Output output, EdgeShape object) {
			Vector2 tempVector = Pools.vector2.obtain();

			kryo.setReferences(false);
			object.getVertex1(tempVector);
			kryo.writeObject(output, tempVector);
			object.getVertex2(tempVector);
			kryo.writeObject(output, tempVector);
			kryo.setReferences(true);

			Pools.vector2.free(tempVector);
		}

		@Override
		public EdgeShape read(Kryo kryo, Input input, Class<EdgeShape> type) {
			EdgeShape edgeShape = new EdgeShape();

			kryo.setReferences(false);
			Vector2 vertex1 = kryo.readObject(input, Vector2.class);
			Vector2 vertex2 = kryo.readObject(input, Vector2.class);
			kryo.setReferences(true);

			edgeShape.set(vertex1, vertex2);

			Pools.vector2.freeAll(vertex1, vertex2);

			return edgeShape;
		}
	}

	private static class ChainShapeSerializer extends Serializer<ChainShape> {
		@Override
		public void write(Kryo kryo, Output output, ChainShape object) {
			// Write if the shape contains vertices or not
			int cVertices = object.getVertexCount();
			output.writeBoolean(cVertices > 0);

			if (cVertices > 0) {
				Vector2 firstVertex = Pools.vector2.obtain();
				Vector2 lastVertex = Pools.vector2.obtain();
				object.getVertex(0, firstVertex);
				object.getVertex(cVertices - 1, lastVertex);

				// Is the chain a loop?
				if (firstVertex.equals(lastVertex)) {
					cVertices--;
					output.writeBoolean(true);
				} else {
					output.writeBoolean(false);
				}

				// Write vertices
				Vector2[] vertices = new Vector2[cVertices];
				for (int i = 0; i < vertices.length; i++) {
					vertices[i] = Pools.vector2.obtain();
					object.getVertex(i, vertices[i]);
				}
				kryo.writeObject(output, vertices);

				Pools.vector2.freeAll(vertices);
			}
		}

		@Override
		public ChainShape read(Kryo kryo, Input input, Class<ChainShape> type) {
			ChainShape chainShape = new ChainShape();

			boolean hasVertices = input.readBoolean();
			if (hasVertices) {
				boolean isLoop = input.readBoolean();

				Vector2[] vertices = kryo.readObject(input, Vector2[].class);
				if (isLoop) {
					chainShape.createLoop(vertices);
				} else {
					chainShape.createChain(vertices);
				}
			}

			return chainShape;
		}
	}

	@Test
	public void testGeneral() {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);

		TestGeneral testGeneral = new TestGeneral();
		testGeneral.number = 1;
		testGeneral.anotherNumber = 2;
		testGeneral.text = "This is my text";

		mKryo.writeObject(output, testGeneral);
		output.close();

		Input input = new Input(byteOut.toByteArray());

		TestGeneral kryoGeneral = mKryo.readObject(input, TestGeneral.class);

		assertEquals("numbers", testGeneral.number, kryoGeneral.number);
		assertEquals("another number", testGeneral.anotherNumber, kryoGeneral.anotherNumber, 0.0f);
		assertEquals("Text", testGeneral.text, kryoGeneral.text);
	}

	private static class TestGeneral {
		int number;
		String text = null;
		float anotherNumber;
	}

	@Test
	public void testTaggedFieldSerializer() {

	}

	@Test
	public void testCompatibleFieldSerializer() {

	}

	@Test
	public void testUUID() {
		UUID writeUuid = UUID.randomUUID();

		UUID readUuuid = copy(writeUuid, UUID.class);

		assertEquals("uuid", writeUuid, readUuuid);
	}

	@Test
	public void testFixtureDef() {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = null;
		fixtureDef.density = 1.5f;
		fixtureDef.isSensor = true;
		fixtureDef.restitution = 70.6f;
		fixtureDef.friction = 15f;
		fixtureDef.filter.categoryBits = 1;
		fixtureDef.filter.groupIndex = 2;
		fixtureDef.filter.maskBits = 3;

		FixtureDef readFixtureDef = copy(fixtureDef, FixtureDef.class);

		assertNotNull("Fixture not null", readFixtureDef);
		assertEquals("Fixture friction", readFixtureDef.friction, fixtureDef.friction, 0.0f);
		assertEquals("Fixture restitution", readFixtureDef.restitution, fixtureDef.restitution, 0.0f);
		assertEquals("Fixture density", readFixtureDef.density, fixtureDef.density, 0.0f);
		assertEquals("Fixture isSensor", readFixtureDef.isSensor, fixtureDef.isSensor);
		assertEquals("Filter category bits", readFixtureDef.filter.categoryBits, fixtureDef.filter.categoryBits);
		assertEquals("Filter group index", readFixtureDef.filter.groupIndex, fixtureDef.filter.groupIndex);
		assertEquals("Filter mask bits", readFixtureDef.filter.maskBits, fixtureDef.filter.maskBits);
		assertNull("Shape null", readFixtureDef.shape);
	}

	@Test
	public void testBox2dShape() {
		// CIRCLE SHAPE
		CircleShape circle = new CircleShape();
		circle.setRadius(2f);
		circle.setPosition(new Vector2(1, 2));

		CircleShape readCircle = copy(circle, CircleShape.class);


		// Appended tests
		assertNotNull("Shape not null", readCircle);
		assertEquals("Shape type", readCircle.getType(), Shape.Type.Circle);
		assertEquals("Circle radius", 2f, readCircle.getRadius(), 0.0f);
		assertEquals("Circle position", new Vector2(1, 2), readCircle.getPosition());

		// Cleanup
		circle.dispose();
		readCircle.dispose();


		// POLYGON SHAPE
		PolygonShape polygon = new PolygonShape();
		Vector2[] vertices = new Vector2[3];
		vertices[0] = new Vector2(10, 11);
		vertices[1] = new Vector2(20, -22);
		vertices[2] = new Vector2(300, 13);
		polygon.set(vertices);


		PolygonShape readPolygon = copy(polygon, PolygonShape.class);


		// Appended tests
		assertNotNull("Shape not null", readPolygon);
		assertEquals("Shape type", readPolygon.getType(), Shape.Type.Polygon);
		assertEquals("number of vertices", 3, readPolygon.getVertexCount());
		Vector2 testVertex = new Vector2();
		readPolygon.getVertex(0, testVertex);
		assertEquals("Polygon vertex 1", vertices[2], testVertex);
		readPolygon.getVertex(1, testVertex);
		assertEquals("Polygon vertex 2", vertices[0], testVertex);
		readPolygon.getVertex(2, testVertex);
		assertEquals("Polygon vertex 3", vertices[1], testVertex);

		// Cleanup
		polygon.dispose();
		readPolygon.dispose();


		// POLYGON SHAPE (null vertices)
		polygon = new PolygonShape();
		readPolygon = copy(polygon, PolygonShape.class);

		// Appended tests
		assertNotNull("Shape not null", readPolygon);
		assertEquals("Shape type", Shape.Type.Polygon, readPolygon.getType());
		assertEquals("number of vertices", 0, readPolygon.getVertexCount());

		// Cleanup
		polygon.dispose();
		readPolygon.dispose();


		// EDGE SHAPE
		EdgeShape edge = new EdgeShape();
		edge.set(new Vector2(1, 2), new Vector2(11, 12));
		EdgeShape readEdge = copy(edge, EdgeShape.class);


		// Appended tests
		assertNotNull("Shape not null", readEdge);
		assertEquals("Shape type", readEdge.getType(), Shape.Type.Edge);
		readEdge.getVertex1(testVertex);
		assertEquals("Edge vertex 1", new Vector2(1, 2), testVertex);
		readEdge.getVertex2(testVertex);
		assertEquals("Edge vertex 2", new Vector2(11, 12), testVertex);

		// Cleanup
		edge.dispose();
		readEdge.dispose();


		// CHAIN SHAPE (looped)
		ChainShape chain = new ChainShape();
		vertices = new Vector2[4];
		vertices[0] = new Vector2(2,0);
		vertices[1] = new Vector2(10,10);
		vertices[2] = new Vector2(5, 10);
		vertices[3] = new Vector2(0, 10);
		chain.createLoop(vertices);

		ChainShape readChain = copy(chain, ChainShape.class);

		assertNotNull("Shape not null", readChain);
		assertEquals("Shape type", Shape.Type.Chain, readChain.getType());
		// +1 because that it's a loop
		assertEquals("chain size", vertices.length + 1, readChain.getVertexCount());
		for (int i = 0; i < vertices.length; ++i) {
			readChain.getVertex(i, testVertex);
			assertEquals("vertex 1", vertices[i], testVertex);
		}

		chain.dispose();
		readChain.dispose();


		// CHAIN SHAPE (chained)
		chain = new ChainShape();
		vertices = new Vector2[4];
		vertices[0] = new Vector2(2,0);
		vertices[1] = new Vector2(10,10);
		vertices[2] = new Vector2(5, 10);
		vertices[3] = new Vector2(0, 10);
		chain.createChain(vertices);

		readChain = copy(chain, ChainShape.class);

		assertNotNull("Shape not null", readChain);
		assertEquals("Shape type", Shape.Type.Chain, readChain.getType());
		assertEquals("chain size", 4, readChain.getVertexCount());
		for (int i = 0; i < vertices.length; ++i) {
			readChain.getVertex(i, testVertex);
			assertEquals("vertex 1", vertices[i], testVertex);
		}

		chain.dispose();
		readChain.dispose();


		// CHAIN SHAPE (null vertices)
		chain = new ChainShape();

		readChain = copy(chain, ChainShape.class);

		assertNotNull("Shape not null", readChain);
		assertEquals("Shape type", Shape.Type.Chain, readChain.getType());
		assertEquals("chain size", 0, readChain.getVertexCount());

		chain.dispose();
		readChain.dispose();
	}

	@Test
	public void testFixtureDefWithShape() {

	}

	@Test
	public void testObjectMap() {

	}

	private <T> T copy(T toCopy, Class<T> type) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		mKryo.writeObject(output, toCopy);
		output.close();
		Input input = new Input(byteOut.toByteArray());
		T readObject = mKryo.readObject(input, type);
		input.close();
		return readObject;
	}

	static private Kryo mKryo = new Kryo();
}
