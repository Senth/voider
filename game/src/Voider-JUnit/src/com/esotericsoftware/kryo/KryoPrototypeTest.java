package com.esotericsoftware.kryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.ApplicationStub;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.utils.Pools;

/**
 * Prototype tests for Kryo
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class KryoPrototypeTest {
	@BeforeClass
	public static void beforeClass() {
		LwjglNativesLoader.load();
		Gdx.app = new ApplicationStub();
		Config.Debug.JUNIT_TEST = true;

		mKryo = Pools.kryo.obtain();


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
		mKryo.register(SavesValue.class, new BaseTaggedSerializer(mKryo, SavesValue.class));
		mKryo.register(NoSave.class, new BaseTaggedSerializer(mKryo, NoSave.class));
		mKryo.register(ClassWithFinal1.class, new TaggedFieldSerializer<ClassWithFinal1>(mKryo, ClassWithFinal1.class));
		mKryo.register(ClassWithFinal2.class, new TaggedFieldSerializer<ClassWithFinal2>(mKryo, ClassWithFinal2.class));
	}

	@AfterClass
	public static void afterClass() {
		Pools.kryo.free(mKryo);
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
		@SuppressWarnings("rawtypes")
		public TaggedChangeFieldSerializer(Kryo kryo, Class type) {
			super(kryo, type);
		}

		@Override
		public TaggedChangeFieldTypeString read(Kryo kryo, Input input, Class<TaggedChangeFieldTypeString> type) {
			TaggedChangeFieldTypeString object = super.read(kryo, input, type);

			object.versionString = String.valueOf(object.version);

			return object;
		}
	}

	@Test
	public void testTaggedWithCustom() {
		// Save
		SavesValue savesValue = new SavesValue();
		savesValue.id = 10;
		savesValue.justAFloat = 11;
		savesValue.value = "12";

		SavesValue readSavesValue = copy(savesValue, SavesValue.class);
		assertEquals(10, readSavesValue.id);
		assertEquals(11, readSavesValue.justAFloat, 0);
		assertEquals("12", savesValue.value);

		// No Save
		NoSave noSave = new NoSave();
		noSave.id = 20;
		noSave.justADouble = 21;
		noSave.value = "22";

		NoSave readNoSave = copy(noSave, NoSave.class);
		assertEquals(20, readNoSave.id);
		assertEquals(21, readNoSave.justADouble, 0);
		assertEquals("", readNoSave.value);
	}

	private static abstract class Base implements KryoSerializable {
		@Tag(1) int id = 0;
		String value = "";

		boolean savesValue() {
			return false;
		}

		@Override
		public void write(Kryo kryo, Output output) {
			if (savesValue()) {
				output.writeString(value);
			}
		}

		@Override
		public void read(Kryo kryo, Input input) {
			if (savesValue()) {
				value = input.readString();
			}
		}
	}

	private static class SavesValue extends Base {
		@Tag(50) float justAFloat = 1.0f;

		@Override
		boolean savesValue() {
			return true;
		}
	}

	private static class NoSave extends Base {
		@Tag(50) double justADouble = 2.0f;
	}

	private static class BaseTaggedSerializer extends TaggedFieldSerializer<Base> {
		public BaseTaggedSerializer(Kryo kryo, Class<? extends Base> type) {
			super(kryo, type);
		}

		@Override
		public void write(Kryo kryo, Output output, Base object) {
			super.write(kryo, output, object);
			object.write(kryo, output);
		}

		@Override
		public Base read(Kryo kryo, Input input, Class<Base> type) {
			Base base = super.read(kryo, input, type);
			base.read(kryo, input);
			return base;
		}
	}

	@Test
	public void testFinal() {
		ClassWithFinal1 final1 = new ClassWithFinal1();

		ClassWithFinal2 readFinal2 = copy(final1, ClassWithFinal2.class);

		// Changing final does not work!
		assertEquals(10, readFinal2.VERSION);
	}

	private static class ClassWithFinal1 {
		@Tag(1) final int VERSION = 1;
	}

	private static class ClassWithFinal2 {
		@Tag(1) final int VERSION = 10;
	}

	@Test
	public void testUUID() {
		UUID writeUuid = UUID.randomUUID();

		UUID readUuuid = copy(writeUuid, UUID.class);

		assertEquals("uuid", writeUuid, readUuuid);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHashSet() {
		HashSet<Integer> intSet = Pools.hashSet.obtain();
		intSet.clear();
		intSet.add(1);
		intSet.add(15);
		intSet.add(10);

		HashSet<Integer> readIntSet = copy(intSet, HashSet.class);
		assertEquals(3, readIntSet.size());
		assertTrue(readIntSet.contains(1));
		assertTrue(readIntSet.contains(10));
		assertTrue(readIntSet.contains(15));

		Pools.hashSet.freeAll(readIntSet);

		// Same again
		readIntSet = copy(intSet, HashSet.class);
		assertEquals(3, readIntSet.size());
		assertTrue(readIntSet.contains(1));
		assertTrue(readIntSet.contains(10));
		assertTrue(readIntSet.contains(15));

		Pools.hashSet.freeAll(intSet, readIntSet);


		// Test Resource Names
		HashSet<InternalNames> resourceSet = Pools.hashSet.obtain();
		resourceSet.clear();
		resourceSet.add(InternalNames.UI_EDITOR_BUTTONS);
		resourceSet.add(InternalNames.UI_GENERAL);

		HashSet<InternalNames> readReasourceSet = copy(resourceSet, HashSet.class);
		assertEquals(2, readReasourceSet.size());
		assertTrue(readReasourceSet.contains(InternalNames.UI_EDITOR_BUTTONS));
		assertTrue(readReasourceSet.contains(InternalNames.UI_GENERAL));
	}

	@Test
	public void testArrayList() {
		@SuppressWarnings("unchecked")
		ArrayList<Integer> intList = Pools.arrayList.obtain();
		intList.clear();
		intList.add(1);
		intList.add(3);
		intList.add(2);

		@SuppressWarnings("unchecked")
		ArrayList<Integer> readIntList = copy(intList, ArrayList.class);
		assertEquals(3, readIntList.size());
		assertEquals((Integer) 1, readIntList.get(0));
		assertEquals((Integer) 3, readIntList.get(1));
		assertEquals((Integer) 2, readIntList.get(2));
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
	public void testMap() {
		Map<Integer, String> objectMap = new HashMap<Integer, String>();

		objectMap.put(1, "One");
		objectMap.put(11, "Eleven");
		objectMap.put(5, "Five");

		@SuppressWarnings("unchecked")
		Map<Integer, String> readMap = copy(objectMap, HashMap.class);
		assertEquals("size", 3, readMap.size());
		assertEquals("[1]", "One", readMap.get(1));
		assertEquals("[11]", "Eleven", readMap.get(11));
		assertEquals("[5]", "Five", readMap.get(5));
	}

	private static <CopyType,ReturnType> ReturnType copy(CopyType toCopy, Class<ReturnType> type) {
		return copy(toCopy, type, mKryo);
	}

	public static <CopyType,ReturnType> ReturnType copy(CopyType toCopy, Class<ReturnType> type, Kryo kryo) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);
		kryo.writeObject(output, toCopy);
		output.close();
		Input input = new Input(byteOut.toByteArray());
		ReturnType readObject = kryo.readObject(input, type);
		input.close();
		return readObject;
	}

	static private Kryo mKryo = new Kryo();
}
