package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.kryo.AtomicIntegerSerializer;
import com.spiddekauga.utils.kryo.SerializableTaggedFieldSerializer;
import com.spiddekauga.utils.kryo.UUIDSerializer;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.game.GameSave;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.ActorTypes;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AiMovementVars;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimRotateVars;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementVars;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.game.actors.PickupActor;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;
import com.spiddekauga.voider.game.actors.VisualVars;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.TriggerAction;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.resources.ResourceBinder;
import com.spiddekauga.voider.resources.ResourceItem;

/**
 * Pool for Kryo instances. When creating a new instance Kryo registers all necessary
 * classes used by Voider.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class KryoVoiderPool extends Pool<Kryo> {

	/**
	 * @param initialCapacity how many initial objects will be created
	 * @param max maximum stored objects
	 */
	public KryoVoiderPool(int initialCapacity, int max) {
		super(Kryo.class, initialCapacity, max);
	}

	/**
	 * Default constructor.
	 */
	public KryoVoiderPool() {
		this(1, 10);
	}

	@Override
	public Kryo newObject() {
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(true);

		RegisterClasses.registerAll(kryo);

		return kryo;
	}

	/**
	 * Contains all classes that should be registered. Adding new classes shall only be
	 * done at the end of the enumeration. If a class isn't used any longer, don't remove
	 * it but set it as null instead.
	 */
	private enum RegisterClasses {
		/** Object[] */
		OBJECT_ARRAY(Object[].class),
		/** Map */
		HASH_MAP(HashMap.class),
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
		UUID_TYPE(UUID.class, new UUIDSerializer()),
		/** Vector2 */
		VECTOR_2(Vector2.class), // Overrides obtain (uses pool)
		/** Vector2[] */
		VECTOR_2_ARRAY(Vector2[].class),
		/** BulletActor */
		BULLET_ACTOR(BulletActor.class, SerializerType.SERIALIZABLE_TAGGED),
		/** BulletActorDef */
		BULLET_ACTOR_DEF(BulletActorDef.class, SerializerType.TAGGED),
		/** EnemyActor */
		ENEMY_ACTOR(EnemyActor.class, SerializerType.SERIALIZABLE_TAGGED),
		/** EnemyActorDef */
		ENEMY_ACTOR_DEF(EnemyActorDef.class, SerializerType.TAGGED),
		/** PickupActor */
		PICKUP_ACTOR(PickupActor.class, SerializerType.SERIALIZABLE_TAGGED),
		/** PickupActorDef */
		PICKUP_ACTOR_DEF(PickupActorDef.class, SerializerType.TAGGED),
		/** PlayerActor */
		PLAYER_ACTOR(PlayerActor.class, SerializerType.SERIALIZABLE_TAGGED),
		/** PlayerActorDef */
		PLAYER_ACTOR_DEF(PlayerActorDef.class, SerializerType.TAGGED),
		/** StaticTerrainActor */
		STATIC_TERRAIN_ACTOR(StaticTerrainActor.class, SerializerType.SERIALIZABLE_TAGGED),
		/** StaticTerrainActorDef */
		STATIC_TERRAIN_ACTOR_DEF(StaticTerrainActorDef.class, SerializerType.TAGGED),
		/** VisualVars */
		VISUAL_VARS(VisualVars.class, SerializerType.SERIALIZABLE_TAGGED),
		/** BulletDestroyer */
		BULLET_DESTROYER(BulletDestroyer.class, SerializerType.TAGGED),
		/** Collectibles */
		COLLECTIBLES(Collectibles.class),
		/** GameSave */
		GAME_SAVE(GameSave.class, SerializerType.SERIALIZABLE_TAGGED),
		/** GameSaveDef */
		GAME_SAVE_DEF(GameSaveDef.class, SerializerType.TAGGED),
		/** Level */
		LEVEL(Level.class, SerializerType.SERIALIZABLE_TAGGED),
		/** LevelDef */
		LEVEL_DEF(LevelDef.class, SerializerType.SERIALIZABLE_TAGGED),
		/** Path */
		PATH(Path.class, SerializerType.SERIALIZABLE_TAGGED),
		/** PlayerStats */
		PLAYER_STATS(PlayerStats.class, SerializerType.TAGGED),
		/** Weapon */
		WEAPON(Weapon.class, SerializerType.TAGGED),
		/** WeaponDef */
		WEAPON_DEF(WeaponDef.class, SerializerType.TAGGED),
		/** TActorActivated */
		T_ACTOR_ACTIVATED(TActorActivated.class, SerializerType.SERIALIZABLE_TAGGED),
		/** TriggerAction */
		TRIGGER_ACTION(TriggerAction.class, SerializerType.TAGGED),
		/** TriggerInfo */
		TRIGGER_INFO(TriggerInfo.class, SerializerType.TAGGED),
		/** TScreenAt */
		T_SCREEN_AT(TScreenAt.class, SerializerType.TAGGED),
		/** TimeBullet */
		TIME_BULLET(TimeBullet.class, SerializerType.TAGGED),
		/** TimePos */
		TIME_POS(TimePos.class),
		/** ArrayList */
		ARRAY_LIST(ArrayList.class), // Created from pool
		/** BodyDef */
		BODY_DEF(BodyDef.class),
		/** BodyType */
		BODY_TYPE(BodyType.class),
		/** HashSet */
		HASH_SET(HashSet.class), // Created from pool
		/** Date */
		DATE(Date.class),
		/** ActorShapeTypes */
		ACTOR_SHAPE_TYPES(ActorShapeTypes.class),
		/** Color */
		COLOR(Color.class),
		/** ActorTypes */
		ACTOR_TYPES(ActorTypes.class),
		/** EnemyGroup */
		ENEMY_GROUP(EnemyGroup.class, SerializerType.TAGGED),
		/** MovementType */
		MOVEMENT_TYPE(MovementTypes.class),
		/** MovementVars */
		MOVEMENT_VARS(MovementVars.class, SerializerType.TAGGED),
		/** AiMovementVars */
		AI_MOVEMENT_VARS(AiMovementVars.class, SerializerType.TAGGED),
		/** AimRotateVars */
		AIM_ROTATE_VARS(AimRotateVars.class, SerializerType.TAGGED),
		/** ResourceNames */
		RESOURCE_NAMES(InternalNames.class),
		/** ResourceItem */
		RESOURCE_ITEM(ResourceItem.class, SerializerType.TAGGED),
		/** Class */
		CLASS(Class.class),
		/** EnemyActor.AimTypes */
		ENEMY_ACTOR_AIM_TYPES(AimTypes.class),
		/** PathTypes */
		PATH_TYPES(PathTypes.class),
		/** ResourceBinder */
		RESOURCE_BINDER(ResourceBinder.class, SerializerType.TAGGED),
		/** TriggerAction.Actions */
		TRIGGER_ACTION_ACTIONS(TriggerAction.Actions.class),
		/** TriggerAction.Reasons */
		TRIGGER_ACTION_REASONS(TriggerAction.Reasons.class),
		/** AtomicInteger */
		ATOMIC_INTEGER(AtomicInteger.class, new AtomicIntegerSerializer()),
		/** GameTime */
		GAME_TIME(GameTime.class, SerializerType.TAGGED),
		/** Byte array */
		BYTE_ARRAY(byte[].class),
		/** Themes */
		THEMES(Themes.class),
		/** BugReportDef */
		BUG_REPORT_DEF(BugReportDef.class, SerializerType.TAGGED),
		/** InternalDeps */
		INTERNAL_DEPS(InternalDeps.class),

		;

		/**
		 * Serializer types
		 */
		private enum SerializerType {
			/** Creates a TaggedFieldSerializer for the type */
			TAGGED,
			/** Creates a CompatibleFieldSerializer for the type */
			COMPATIBLE,
			/** Creates a SerializableTaggedFieldSerialize for the type */
			SERIALIZABLE_TAGGED,
		}

		/**
		 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
		 * @param type the type to register, if null it won't register it. Setting to null
		 *        is useful when the class isn't used anymore (doesn't exist) but we still
		 *        need to keep the register order.
		 */
		private RegisterClasses(Class<?> type) {
			mType = type;
		}

		/**
		 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
		 * and when {@link #createSerializers(Kryo)} is called will created the specified
		 * serializer type
		 * @param type the type to register, if null it won't register it. Setting to null
		 *        is useful when the class isn't used anymore (doesn't exist) but we still
		 *        need to keep the register order.
		 * @param createSerializerType the type of serializer to create when
		 *        {@link #createSerializers(Kryo)} is called.
		 */
		private RegisterClasses(Class<?> type, SerializerType createSerializerType) {
			mType = type;
			mSerializerType = createSerializerType;
		}

		/**
		 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
		 * @param type the type to register, if null it won't register it. Setting to null
		 *        is useful when the class isn't used anymore (doesn't exist) but we still
		 *        need to keep the register order.
		 * @param serializer the serializer to use for the specified type, if null the
		 *        default serializer will be used instead.
		 */
		private RegisterClasses(Class<?> type, Serializer<?> serializer) {
			mType = type;
			mSerializer = serializer;
		}

		/**
		 * Some classes needs a serializer that requires Kryo in the constructor. These
		 * serializers are created with this method instead.
		 * @param kryo creates the serializers for this Kryo instance.
		 */
		@SuppressWarnings("rawtypes")
		private static void createSerializers(Kryo kryo) {
			// Vector2
			VECTOR_2.mSerializer = new FieldSerializer<Vector2>(kryo, Vector2.class) {
				@Override
				public Vector2 create(Kryo kryo, Input input, Class<Vector2> type) {
					return Pools.vector2.obtain();
				}
			};

			// ArrayList
			ARRAY_LIST.mSerializer = new CollectionSerializer() {
				@Override
				protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
					ArrayList arrayList = Pools.arrayList.obtain();
					return arrayList;
				}
			};

			// HashSet
			HASH_SET.mSerializer = new CollectionSerializer() {
				@Override
				protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
					HashSet hashSet = Pools.hashSet.obtain();
					return hashSet;
				}
			};


			// Create tagged or compatible serializers
			for (RegisterClasses registerClass : RegisterClasses.values()) {
				if (registerClass.mSerializerType != null) {
					switch (registerClass.mSerializerType) {
					case TAGGED:
						registerClass.mSerializer = new TaggedFieldSerializer<Object>(kryo, registerClass.mType);
						break;

					case COMPATIBLE:
						registerClass.mSerializer = new CompatibleFieldSerializer<Object>(kryo, registerClass.mType);
						break;

					case SERIALIZABLE_TAGGED:
						registerClass.mSerializer = new SerializableTaggedFieldSerializer(kryo, registerClass.mType);
						break;
					}
				}
			}
		}

		/**
		 * Registers all classes with serializers.
		 * @param kryo registers the serializers for this Kryo instance.
		 */
		public static void registerAll(Kryo kryo) {
			createSerializers(kryo);

			for (RegisterClasses registerClass : RegisterClasses.values()) {
				if (registerClass.mType != null) {
					if (registerClass.mSerializer == null) {
						kryo.register(registerClass.mType, registerClass.ordinal() + OFFSET);
					} else {
						kryo.register(registerClass.mType, registerClass.mSerializer, registerClass.ordinal() + OFFSET);
					}
				}
			}
		}

		/** Offset for register id, as there exists some default registered types */
		private static int OFFSET = 50;
		/** Class type to register, if null it is not registered */
		private Class<?> mType;
		/** Serializer to use, if null it uses the default serializer */
		private Serializer<?> mSerializer = null;
		/**
		 * If a serializer of the specified type should be created for this class. If
		 * null, no serializer will be created for this type.
		 */
		private SerializerType mSerializerType = null;
	}


	// ------------------------------------
	// Serializers
	// ------------------------------------
	/**
	 * Serializes box2d circle shapes
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

		@Override
		public CircleShape copy(Kryo kryo, CircleShape original) {
			CircleShape circleShape = new CircleShape();
			circleShape.setPosition(original.getPosition());
			circleShape.setRadius(original.getRadius());
			return circleShape;
		}
	}

	/**
	 * Serializes box2d polygon shapes
	 */
	private static class PolygonShapeSerializer extends Serializer<PolygonShape> {
		@Override
		public void write(Kryo kryo, Output output, PolygonShape object) {
			Vector2[] vertices = new Vector2[object.getVertexCount()];
			for (int i = 0; i < vertices.length; ++i) {
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

		@Override
		public PolygonShape copy(Kryo kryo, PolygonShape original) {
			PolygonShape polygonShape = new PolygonShape();

			Vector2[] vertices = new Vector2[original.getVertexCount()];
			for (int i = 0; i < vertices.length; ++i) {
				vertices[i] = Pools.vector2.obtain();
				original.getVertex(i, vertices[i]);
			}
			polygonShape.set(vertices);

			Pools.vector2.freeAll(vertices);

			return polygonShape;
		}
	}

	/**
	 * Serializes box2d edge shapes
	 */
	private static class EdgeShapeSerializer extends Serializer<EdgeShape> {
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

		@Override
		public EdgeShape copy(Kryo kryo, EdgeShape original) {
			EdgeShape edgeShape = new EdgeShape();

			Vector2 v1 = Pools.vector2.obtain();
			Vector2 v2 = Pools.vector2.obtain();

			original.getVertex1(v1);
			original.getVertex2(v2);
			edgeShape.set(v1, v2);

			Pools.vector2.freeAll(v1, v2);

			return edgeShape;
		}
	}

	/**
	 * Serializes box2d chain shapes
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
				Pools.vector2.freeAll(vertices);
			}

			return chainShape;
		}

		@Override
		public ChainShape copy(Kryo kryo, ChainShape original) {
			ChainShape chainShape = new ChainShape();

			int cVertices = original.getVertexCount();
			boolean isLoop;

			if (cVertices > 0) {
				Vector2 firstVertex = Pools.vector2.obtain();
				Vector2 lastVertex = Pools.vector2.obtain();
				original.getVertex(0, firstVertex);
				original.getVertex(cVertices - 1, lastVertex);

				// Is the chain a loop?
				if (firstVertex.equals(lastVertex)) {
					cVertices--;
					isLoop = true;
				} else {
					isLoop = false;
				}

				// Write vertices
				Vector2[] vertices = new Vector2[cVertices];
				for (int i = 0; i < vertices.length; i++) {
					vertices[i] = Pools.vector2.obtain();
					original.getVertex(i, vertices[i]);
				}

				if (isLoop) {
					chainShape.createLoop(vertices);
				} else {
					chainShape.createChain(vertices);
				}

				Pools.vector2.freeAll(vertices);
			}

			return chainShape;
		}
	}
}
