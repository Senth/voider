package com.spiddekauga.voider.utils;

import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

/**
 * Pool for Kryo instances. When creating a new instance Kryo registers all
 * necessary classes used by Voider.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class KryoPool extends Pool<Kryo> {

	/**
	 * @param initialCapacity how many initial objects will be created
	 * @param max maximum stored objects
	 */
	public KryoPool(int initialCapacity, int max) {
		super(Kryo.class, initialCapacity, max);
	}

	/**
	 * Default constructor.
	 */
	public KryoPool() {
		this(5, 20);
	}

	@Override
	public Kryo newObject() {
		Kryo kryo = new Kryo();

		RegisterClasses.createSerializers(kryo);
		RegisterClasses.registerAll(kryo);

		return kryo;
	}

	/**
	 * Contains all classes that should be registered.
	 * Adding new classes shall only be done at the end of the enumeration. If a class isn't
	 * used any longer, don't remove it but set it as null instead.
	 */
	private enum RegisterClasses {
		/** Object[] */
		OBJECT_ARRAY(Object[].class),
		/** ObjectMap */
		OBJECT_MAP(ObjectMap.class),
		/** float[] */
		FLOAT_ARRAY(float[].class),
		/** FixtureDef */
		FIXTURE_DEF(FixtureDef.class),
		/** Filter */
		FILTER(Filter.class),
		/** CircleShape */
		CIRCLE_SHAPE(CircleShape.class, new CircleShapeSerializer()),
		/** PolygonShape */
		POLYGON_SHAPE(PolygonShape.class, new PolygonShapeSerializer()),
		/** EdgeShape */
		EDGE_SHAPE(EdgeShape.class, new EdgeShapeSerializer()),
		/** ChainShape */
		CHAIN_SHAPE(ChainShape.class, new ChainShapeSerializer()),
		/** UUID */
		UUID_TYPE(UUID.class), // Has serializer
		/** Vector2 */
		VECTOR_2(Vector2.class), // Has serializer
		/** Vector2[] */
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
			UUID_TYPE.mSerializer = new FieldSerializer<UUID>(kryo, UUID.class) {
				@Override
				public UUID create(Kryo kryo, Input input, Class<UUID> type) {
					return UUID.randomUUID();
				}
			};

			// Vector2
			VECTOR_2.mSerializer = new FieldSerializer<Vector2>(kryo, Vector2.class) {
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
}
