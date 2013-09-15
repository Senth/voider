package com.esotericsoftware.kryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
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

		RegisterClasses.createSerializers(mKryo);
		RegisterClasses.registerAll(mKryo);


		// Register testing classes
		mKryo.register(EdgeShape[].class);
		mKryo.register(TestGeneral.class);
		mKryo.register(TaggedFieldClass1.class, new TaggedFieldSerializer<TaggedFieldClass1>(mKryo, TaggedFieldClass1.class));
		mKryo.register(TaggedFieldClass2.class, new TaggedFieldSerializer<TaggedFieldClass2>(mKryo, TaggedFieldClass2.class));
		mKryo.register(TaggedFieldClass3.class, new TaggedFieldSerializer<TaggedFieldClass3>(mKryo, TaggedFieldClass3.class));
		mKryo.register(CompatFieldClass1.class, new CompatibleFieldSerializer<CompatFieldClass1>(mKryo, CompatFieldClass1.class));
		mKryo.register(CompatFieldClass2.class, new CompatibleFieldSerializer<CompatFieldClass2>(mKryo, CompatFieldClass2.class));
		mKryo.register(CompatFieldClass3.class, new CompatibleFieldSerializer<CompatFieldClass3>(mKryo, CompatFieldClass3.class));
		mKryo.register(OptionalFieldClass.class);
		mKryo.register(CompatOptionalFieldClass1.class, new CompatibleFieldSerializer<CompatOptionalFieldClass1>(mKryo, CompatOptionalFieldClass1.class));
		mKryo.register(CompatOptionalFieldClass2.class, new CompatibleFieldSerializer<CompatOptionalFieldClass2>(mKryo, CompatOptionalFieldClass2.class));
		mKryo.register(CompatOptionalFieldClass3.class, new CompatibleFieldSerializer<CompatOptionalFieldClass3>(mKryo, CompatOptionalFieldClass3.class));
		mKryo.register(TaggedChangeFieldTypeInt.class, new TaggedFieldSerializer<TaggedChangeFieldTypeInt>(mKryo, TaggedChangeFieldTypeInt.class));
		mKryo.register(TaggedChangeFieldTypeString.class, new TaggedFieldSerializer<TaggedChangeFieldTypeString>(mKryo, TaggedChangeFieldTypeString.class));
	}

	/**
	 * Contains all classes that should be registered.
	 * Adding new classes shall only be done at the end of the enumeration. If a class isn't
	 * used any longer, don't remove it but set it as null instead.
	 */
	private enum RegisterClasses {
		OBJECT_ARRAY(Object[].class),
		OBJECT_MAP(ObjectMap.class),
		FLOAT_ARRAY(float[].class),
		FIXTURE_DEF(FixtureDef.class),
		FILTER(Filter.class),
		CIRCLE_SHAPE(CircleShape.class, new CircleShapeSerializer()),
		POLYGON_SHAPE(PolygonShape.class, new PolygonShapeSerializer()),
		EDGE_SHAPE(EdgeShape.class, new EdgeShapeSerializer()),
		CHAIN_SHAPE(ChainShape.class, new ChainShapeSerializer()),
		UUID_TYPE(UUID.class),
		VECTOR_2(Vector2.class),
		VECTOR_2_ARRAY(Vector2[].class),

		;

		/**
		 * Constructor which takes the type to register with Kryo using {@link #registerAll(Kryo)}
		 * @param type the type to register, if null it won't register it. Setting to null is useful
		 * when the class isn't used anymore (doesn't exist) but we still need to keep the register
		 * order.
		 */
		private RegisterClasses(Class<?> type) {
			this(type, null);
		}

		/**
		 * Constructor which takes the type to register with Kryo using {@link #registerAll(Kryo)}
		 * @param type the type to register, if null it won't register it. Setting to null is useful
		 * when the class isn't used anymore (doesn't exist) but we still need to keep the register
		 * order.
		 * @param serializer the serializer to use for the specified type, if null the default
		 * serializer will be used instead.
		 */
		private RegisterClasses(Class<?> type, Serializer<?> serializer) {
			mType = type;
			mSerializer = serializer;
		}

		/**
		 * Some classes needs a serializer that requires Kryo in the constructor. These serializers
		 * are created with this method instead.
		 * @param kryo creates the serializers for this Kryo instance.
		 */
		public static void createSerializers(Kryo kryo) {
			// UUID
			UUID_TYPE.mSerializer = new FieldSerializer<UUID>(mKryo, UUID.class) {
				@Override
				public UUID create(Kryo kryo, Input input, Class<UUID> type) {
					return UUID.randomUUID();
				}
			};

			// Vector2
			VECTOR_2.mSerializer = new FieldSerializer<Vector2>(mKryo, Vector2.class) {
				@Override
				public Vector2 create(Kryo kryo, Input input, Class<Vector2> type) {
					return Pools.vector2.obtain();
				}
			};
		}

		/**
		 * Registers all classes with serializers.
		 * @param kryo registers the serializers for this Kryo instance.
		 */
		public static void registerAll(Kryo kryo) {
			for (RegisterClasses registerClass : RegisterClasses.values()) {
				if (registerClass.mType != null) {
					if (registerClass.mSerializer == null) {
						kryo.register(registerClass.mType, registerClass.ordinal()+OFFSET);
					} else {
						kryo.register(registerClass.mType, registerClass.mSerializer, registerClass.ordinal()+OFFSET);
					}
				}
			}
		}

		/** Offset for register id, as there exists some default registered types */
		private static int OFFSET = 50;
		/** Class type to register, if null it is not registered */
		private Class<?> mType;
		/** Serializer to use, if null it uses the default serializer */
		private Serializer<?> mSerializer;
	}

	/**
	 * Serializes box2d circle shapes
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
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

	/**
	 * Serializes box2d polygon shapes
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
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

	/**
	 * Serializes box2d edge shapes
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
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

	/**
	 * Serializes box2d chain shapes
	 * 
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
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
		input.close();
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
		TaggedFieldClass1 fieldClass1 = new TaggedFieldClass1();
		fieldClass1.field1 = 2;
		fieldClass1.field2 = 4;
		fieldClass1.field3 = 6;
		fieldClass1.field4 = 8;

		TaggedFieldClass1 readFieldClass1 = copy(fieldClass1, TaggedFieldClass1.class);
		assertEquals(2, readFieldClass1.field1);
		assertEquals(4, readFieldClass1.field2);
		assertEquals(6, readFieldClass1.field3);
		assertEquals(8, readFieldClass1.field4);

		TaggedFieldClass2 fieldClass2 = copy(fieldClass1, TaggedFieldClass2.class);
		assertEquals(2, fieldClass2.field1);
		assertEquals(4, fieldClass2.field2);
		assertEquals(6, fieldClass2.field3);
		assertEquals(8, fieldClass2.field4);
		assertEquals(5, fieldClass2.field5);
		fieldClass2.field5 = 10;

		TaggedFieldClass3 fieldClass3 = copy(fieldClass2, TaggedFieldClass3.class);
		assertEquals(2, fieldClass3.field1);
		assertEquals(4, fieldClass3.field2);
		assertEquals(6, fieldClass3.field3);
		assertEquals(8, fieldClass3.field4);
		assertEquals(10, fieldClass3.field5);
		assertEquals(1337, fieldClass3.neverUsed);
		fieldClass3.neverUsed = 0;

		TaggedFieldClass3 readFieldClass3 = copy(fieldClass3, TaggedFieldClass3.class);
		assertEquals(2, readFieldClass3.field1);
		assertEquals(2, readFieldClass3.field2);
		assertEquals(6, readFieldClass3.field3);
		assertEquals(4, readFieldClass3.field4);
		assertEquals(10, readFieldClass3.field5);
		assertEquals(1337, readFieldClass3.neverUsed);
	}

	private static class TaggedFieldClass1 {
		@Tag(1)	int field1 = 1;
		@Tag(2)	int field2 = 2;
		@Tag(3)	int field3 = 3;
		@Tag(4)	int field4 = 4;
	}

	private static class TaggedFieldClass2 {
		@Tag(1)	int field1 = 1;
		@Tag(2)	int field2 = 2;
		@Tag(3)	int field3 = 3;
		@Tag(4)	int field4 = 4;
		@Tag(5)	int field5 = 5;
	}

	private static class TaggedFieldClass3 {
		@Tag(1)	int field1 = 1;
		@Tag(2) @Deprecated	int field2 = 2;
		@Tag(3)	int field3 = 3;
		@Tag(4) @Deprecated	int field4 = 4;
		@Tag(5)	int field5 = 5;
		int neverUsed = 1337;
	}

	@Test
	public void testCompatibleFieldSerializer() {
		CompatFieldClass1 compatClass1 = new CompatFieldClass1();
		compatClass1.field1 = 2;
		compatClass1.field2 = 4;
		compatClass1.field3 = 6;
		compatClass1.field4 = 8;

		// 1 -> 1
		CompatFieldClass1 readCompatClass1 = copy(compatClass1, CompatFieldClass1.class);
		assertEquals(2, readCompatClass1.field1);
		assertEquals(4, readCompatClass1.field2);
		assertEquals(6, readCompatClass1.field3);
		assertEquals(8, readCompatClass1.field4);

		// 1 -> 2
		CompatFieldClass2 compatClass2 = copy(compatClass1, CompatFieldClass2.class);
		assertEquals(2, compatClass2.field1);
		assertEquals(4, compatClass2.field2);
		assertEquals(6, compatClass2.field3);
		assertEquals(8, compatClass2.field4);
		assertEquals(5, compatClass2.field5);
		compatClass2.field5 = 10;

		// 2 -> 3
		CompatFieldClass3 compatClass3 = copy(compatClass2, CompatFieldClass3.class);
		assertEquals(2, compatClass3.field1);
		assertEquals(6, compatClass3.field3);
		assertEquals(10, compatClass3.field5);
	}

	private static class CompatFieldClass1 {
		int field1 = 1;
		int field2 = 2;
		int field3 = 3;
		int field4 = 4;
	}

	private static class CompatFieldClass2 {
		int field1 = 1;
		int field2 = 2;
		int field3 = 3;
		int field4 = 4;
		int field5 = 5;
	}

	private static class CompatFieldClass3 {
		int field1 = 1;
		int field3 = 3;
		int field5 = 5;
	}

	@Test
	public void testOptionalFields() {
		OptionalFieldClass optionalClass = new OptionalFieldClass();
		optionalClass.field1 = 2;
		optionalClass.field2 = 4;
		optionalClass.field3 = 6;
		optionalClass.field4 = 8;
		optionalClass.field5 = 10;

		OptionalFieldClass readOptionalClass = copy(optionalClass, OptionalFieldClass.class);
		assertEquals(2, readOptionalClass.field1);
		assertEquals(2, readOptionalClass.field2);
		assertEquals(6, readOptionalClass.field3);
		assertEquals(4, readOptionalClass.field4);
		assertEquals(10, readOptionalClass.field5);
	}

	private static class OptionalFieldClass {
		int field1 = 1;
		@Optional("not used") int field2 = 2;
		int field3 = 3;
		@Optional("another") int field4 = 4;
		int field5 = 5;
	}



	@Test
	public void testOptionalWithCompatible() {
		CompatOptionalFieldClass1 optionalClass1 = new CompatOptionalFieldClass1();
		optionalClass1.field1 = 2;
		optionalClass1.field2 = 4;
		optionalClass1.field3 = 6;
		optionalClass1.field4 = 8;

		CompatOptionalFieldClass1 readOptionalClass1 = copy(optionalClass1, CompatOptionalFieldClass1.class);
		assertEquals(2, readOptionalClass1.field1);
		assertEquals(4, readOptionalClass1.field2);
		assertEquals(3, readOptionalClass1.field3);
		assertEquals(8, readOptionalClass1.field4);

		// 1 -> 2
		CompatOptionalFieldClass2 optionalClass2 = copy(optionalClass1, CompatOptionalFieldClass2.class);
		assertEquals(2, optionalClass2.field1);
		assertEquals(4, optionalClass2.field2);
		assertEquals(3, optionalClass2.field3);
		assertEquals(8, optionalClass2.field4);
		assertEquals(5, optionalClass2.field5);
		optionalClass2.field5 = 10;
		optionalClass2.field3 = 6;

		// 2 -> 2
		CompatOptionalFieldClass2 readOptionalClass2 = copy(optionalClass1, CompatOptionalFieldClass2.class);
		assertEquals(2, readOptionalClass2.field1);
		assertEquals(4, readOptionalClass2.field2);
		assertEquals(3, readOptionalClass2.field3);
		assertEquals(8, readOptionalClass2.field4);
		assertEquals(5, readOptionalClass2.field5);

		// 1 -> 3
		CompatOptionalFieldClass3 optionalClass3 = copy(optionalClass1, CompatOptionalFieldClass3.class);
		assertEquals(2, optionalClass3.field1);
		assertEquals(3, optionalClass3.field3);
		assertEquals(5, optionalClass3.field5);

		// 2 -> 3
		CompatOptionalFieldClass3 readOptionalClass3 = copy(optionalClass1, CompatOptionalFieldClass3.class);
		assertEquals(2, readOptionalClass3.field1);
		assertEquals(3, readOptionalClass3.field3);
		assertEquals(5, readOptionalClass3.field5);
		optionalClass3.field3 = 6;
		optionalClass3.field5 = 10;

		// 3 -> 3
		readOptionalClass3 = copy(optionalClass3, CompatOptionalFieldClass3.class);
		assertEquals(2, readOptionalClass3.field1);
		assertEquals(3, readOptionalClass3.field3);
		assertEquals(10, readOptionalClass3.field5);
	}

	private static class CompatOptionalFieldClass1 {
		int field1 = 1;
		int field2 = 2;
		@Optional("test") int field3 = 3;
		int field4 = 4;
	}

	private static class CompatOptionalFieldClass2 {
		int field1 = 1;
		int field2 = 2;
		@Optional("test") int field3 = 3;
		int field4 = 4;
		@Optional("not used") int field5 = 5;
	}

	private static class CompatOptionalFieldClass3 {
		int field1 = 1;
		@Optional("test") int field3 = 3;
		int field5 = 5;
	}

	@Test
	public void testTaggedChangeFieldType() {
		TaggedChangeFieldTypeInt intVersion = new TaggedChangeFieldTypeInt();
		intVersion.version = 15354886;
		intVersion.test = 15;
		intVersion.dep = 1;
		intVersion.dep2 = 2;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		mKryo.writeObject(output, intVersion);
		output.close();

		Input input = new Input(byteOut.toByteArray());
		TaggedChangeFieldTypeString stringVersion = mKryo.readObject(input, TaggedChangeFieldTypeString.class, new TaggedChangeFieldSerializer(mKryo, TaggedChangeFieldTypeString.class));

		assertEquals(1337, stringVersion.added);
	}

	private static class TaggedChangeFieldTypeInt {
		@Tag(1) int test = 0;
		@Tag(2) int dep = 0;
		@Tag(3) int version = 0;
		@Tag(4) int dep2 = 0;
	}

	private static class TaggedChangeFieldTypeString {
		@Tag(1) int test = 10;
		@Tag(2) @Deprecated int dep = 0;
		@Tag(3) int version = 1;
		@Tag(4) @Deprecated int dep2 = 2;
		@Tag(5) int added = 1337;
		@Tag(6) String versionString = "0";
	}

	private static class TaggedChangeFieldSerializer extends TaggedFieldSerializer<TaggedChangeFieldTypeString> {

		/**
		 * @param kryo
		 * @param type
		 */
		public TaggedChangeFieldSerializer(
				Kryo kryo, Class type) {
			super(kryo, type);
			// TODO Auto-generated constructor stub
		}

		@Override
		public TaggedChangeFieldTypeString read(Kryo kryo, Input input, Class<TaggedChangeFieldTypeString> type) {
			TaggedChangeFieldTypeString object = super.read(kryo, input, type);

			object.versionString = String.valueOf(object.version);

			return object;
		}
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
	public void testEdgeReferences() {
		EdgeShape edge1 = new EdgeShape();
		edge1.set(new Vector2(1, 2), new Vector2(11, 12));
		EdgeShape edge2 = new EdgeShape();
		edge2.set(new Vector2(2, 3), new Vector2(22, 23));

		EdgeShape[] edges = new EdgeShape[4];
		edges[0] = edge1;
		edges[1] = edge1;
		edges[2] = edge2;
		edges[3] = edge2;

		EdgeShape[] readEdges = copy(edges, EdgeShape[].class);

		assertEquals(4, readEdges.length);
		assertSame(readEdges[0], readEdges[1]);
		assertSame(readEdges[2], readEdges[3]);
		assertNotSame(readEdges[0], readEdges[2]);
		assertNotSame(readEdges[0], readEdges[3]);
		assertNotSame(readEdges[1], readEdges[2]);
		assertNotSame(readEdges[1], readEdges[3]);
	}

	@Test
	public void testFixtureDefWithShape() {
		FixtureDef fixtureDef = new FixtureDef();
		CircleShape circleShape = new CircleShape();
		circleShape.setPosition(new Vector2(12, 11));
		circleShape.setRadius(2);
		fixtureDef.shape = circleShape;

		FixtureDef readFixtureDef = copy(fixtureDef, FixtureDef.class);

		assertNotNull(readFixtureDef.shape);
		CircleShape readCircleShape = (CircleShape) readFixtureDef.shape;
		assertEquals("Circle position", new Vector2(12, 11),  readCircleShape.getPosition());
		assertEquals("radius", 2, readCircleShape.getRadius(), 0f);
	}

	@Test
	public void testObjectMap() {
		ObjectMap<Integer, String> objectMap = new ObjectMap<Integer, String>();

		objectMap.put(1, "One");
		objectMap.put(11, "Eleven");
		objectMap.put(5, "Five");

		@SuppressWarnings("unchecked")
		ObjectMap<Integer, String> readObjectMap = copy(objectMap, ObjectMap.class);
		assertEquals("size", 3, readObjectMap.size);
		assertEquals("[1]", "One", readObjectMap.get(1));
		assertEquals("[11]", "Eleven", readObjectMap.get(11));
		assertEquals("[5]", "Five", readObjectMap.get(5));
	}

	private <CopyType,ReturnType> ReturnType copy(CopyType toCopy, Class<ReturnType> type) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		mKryo.writeObject(output, toCopy);
		output.close();
		Input input = new Input(byteOut.toByteArray());
		ReturnType readObject = mKryo.readObject(input, type);
		input.close();
		return readObject;
	}

	static private Kryo mKryo = new Kryo();
}
