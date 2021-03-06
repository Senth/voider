package com.spiddekauga.voider.utils;

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
import com.spiddekauga.voider.game.PlayerStats.ScorePart;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.ActorTypes;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.DrawImages;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AiMovementVars;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimRotateVars;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementVars;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.game.actors.PickupActor;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.actors.Shape;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.TriggerAction;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.resources.ResourceContainer;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.sound.Music;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pool for Kryo instances. When creating a new instance Kryo registers all necessary classes used
 * by Voider.
 */
public class KryoVoiderPool extends Pool<Kryo> {

/**
 * Default constructor.
 */
public KryoVoiderPool() {
	this(1, 10);
}

/**
 * @param initialCapacity how many initial objects will be created
 * @param max maximum stored objects
 */
public KryoVoiderPool(int initialCapacity, int max) {
	super(Kryo.class, initialCapacity, max);
}

@Override
public Kryo newObject() {
	Kryo kryo = new Kryo();
	kryo.setRegistrationRequired(true);

	RegisterClasses.registerAll(kryo);

	return kryo;
}

/**
 * Contains all classes that should be registered. Adding new classes shall only be done at the end
 * of the enumeration. If a class isn't used any longer, don't remove it but set it as null
 * instead.
 */
private enum RegisterClasses {
	OBJECT_ARRAY(Object[].class),
	HASH_MAP(HashMap.class),
	FLOAT_ARRAY(float[].class),
	FIXTURE_DEF(FixtureDef.class),
	FILTER(Filter.class),
	CIRCLE_SHAPE(CircleShape.class, new CircleShapeSerializer()),
	POLYGON_SHAPE(PolygonShape.class, new PolygonShapeSerializer()),
	EDGE_SHAPE(EdgeShape.class, new EdgeShapeSerializer()),
	CHAIN_SHAPE(ChainShape.class, new ChainShapeSerializer()),
	UUID_TYPE(UUID.class, new UUIDSerializer()),
	VECTOR_2(Vector2.class), // Overrides obtain (uses pool)
	VECTOR_2_ARRAY(Vector2[].class),
	BULLET_ACTOR(BulletActor.class, SerializerType.SERIALIZABLE_TAGGED),
	BULLET_ACTOR_DEF(BulletActorDef.class, SerializerType.SERIALIZABLE_TAGGED),
	ENEMY_ACTOR(EnemyActor.class, SerializerType.SERIALIZABLE_TAGGED),
	ENEMY_ACTOR_DEF(EnemyActorDef.class, SerializerType.SERIALIZABLE_TAGGED),
	PICKUP_ACTOR(PickupActor.class, SerializerType.SERIALIZABLE_TAGGED),
	PICKUP_ACTOR_DEF(PickupActorDef.class, SerializerType.SERIALIZABLE_TAGGED),
	PLAYER_ACTOR(PlayerActor.class, SerializerType.SERIALIZABLE_TAGGED),
	PLAYER_ACTOR_DEF(PlayerActorDef.class, SerializerType.SERIALIZABLE_TAGGED),
	STATIC_TERRAIN_ACTOR(StaticTerrainActor.class, SerializerType.SERIALIZABLE_TAGGED),
	STATIC_TERRAIN_ACTOR_DEF(StaticTerrainActorDef.class, SerializerType.SERIALIZABLE_TAGGED),
	VISUAL_VARS(Shape.class, SerializerType.SERIALIZABLE_TAGGED),
	BULLET_DESTROYER(BulletDestroyer.class, SerializerType.TAGGED),
	COLLECTIBLES(Collectibles.class),
	GAME_SAVE(GameSave.class, SerializerType.SERIALIZABLE_TAGGED),
	GAME_SAVE_DEF(GameSaveDef.class, SerializerType.TAGGED),
	LEVEL(Level.class, SerializerType.SERIALIZABLE_TAGGED),
	LEVEL_DEF(LevelDef.class, SerializerType.SERIALIZABLE_TAGGED),
	PATH(Path.class, SerializerType.SERIALIZABLE_TAGGED),
	PLAYER_STATS(PlayerStats.class, SerializerType.TAGGED),
	WEAPON(Weapon.class, SerializerType.TAGGED),
	WEAPON_DEF(WeaponDef.class, SerializerType.TAGGED),
	T_ACTOR_ACTIVATED(TActorActivated.class, SerializerType.SERIALIZABLE_TAGGED),
	TRIGGER_ACTION(TriggerAction.class, SerializerType.TAGGED),
	TRIGGER_INFO(TriggerInfo.class, SerializerType.TAGGED),
	T_SCREEN_AT(TScreenAt.class, SerializerType.TAGGED),
	TIME_BULLET(TimeBullet.class, SerializerType.TAGGED),
	TIME_POS(TimePos.class),
	ARRAY_LIST(ArrayList.class),
	BODY_DEF(BodyDef.class),
	BODY_TYPE(BodyType.class),
	HASH_SET(HashSet.class),
	DATE(Date.class),
	ACTOR_SHAPE_TYPES(ActorShapeTypes.class),
	COLOR(Color.class),
	ACTOR_TYPES(ActorTypes.class),
	ENEMY_GROUP(EnemyGroup.class, SerializerType.TAGGED),
	MOVEMENT_TYPE(MovementTypes.class),
	MOVEMENT_VARS(MovementVars.class, SerializerType.TAGGED),
	AI_MOVEMENT_VARS(AiMovementVars.class, SerializerType.TAGGED),
	AIM_ROTATE_VARS(AimRotateVars.class, SerializerType.TAGGED),
	RESOURCE_NAMES(InternalNames.class),
	RESOURCE_ITEM(ResourceItem.class, SerializerType.TAGGED),
	CLASS(Class.class),
	ENEMY_ACTOR_AIM_TYPES(AimTypes.class),
	PATH_TYPES(PathTypes.class),
	RESOURCE_BINDER(ResourceContainer.class, SerializerType.TAGGED),
	TRIGGER_ACTION_ACTIONS(TriggerAction.Actions.class),
	TRIGGER_ACTION_REASONS(TriggerAction.Reasons.class),
	ATOMIC_INTEGER(AtomicInteger.class, new AtomicIntegerSerializer()),
	GAME_TIME(GameTime.class, SerializerType.TAGGED),
	BYTE_ARRAY(byte[].class),
	THEMES(Themes.class),
	BUG_REPORT_DEF(BugReportDef.class, SerializerType.TAGGED),
	INTERNAL_DEPS(InternalDeps.class),
	IMAGE_NAMES(IImageNames.class),
	GENERAL_IMAGES(SkinNames.GeneralImages.class),
	MUSIC(Music.class),
	SCORE_PART(ScorePart.class, SerializerType.TAGGED),
	STACK(Stack.class),
	DRAW_IMAGES(DrawImages.class),;

	/** Offset for register id, as there exists some default registered types */
	private static final int OFFSET = 50;
	/** Class type to register, if null it is not registered */
	private Class<?> mType;
	/** Serializer to use, if null it uses the default serializer */
	private Serializer<?> mSerializer = null;
	/**
	 * If a serializer of the specified type should be created for this class. If null, no
	 * serializer will be created for this type.
	 */
	private SerializerType mSerializerType = null;

	/**
	 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
	 * @param type the type to register, if null it won't register it. Setting to null is useful
	 * when the class isn't used anymore (doesn't exist) but we still need to keep the register
	 * order.
	 */
	private RegisterClasses(Class<?> type) {
		mType = type;
	}

	/**
	 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)} and when
	 * {@link #createSerializers(Kryo)} is called will created the specified serializer type
	 * @param type the type to register, if null it won't register it. Setting to null is useful
	 * when the class isn't used anymore (doesn't exist) but we still need to keep the register
	 * order.
	 * @param createSerializerType the type of serializer to create when {@link
	 * #createSerializers(Kryo)} is called.
	 */
	private RegisterClasses(Class<?> type, SerializerType createSerializerType) {
		mType = type;
		mSerializerType = createSerializerType;
	}

	/**
	 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
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

	/**
	 * Some classes needs a serializer that requires Kryo in the constructor. These serializers are
	 * created with this method instead.
	 * @param kryo creates the serializers for this Kryo instance.
	 */
	private static void createSerializers(Kryo kryo) {
		// Create tagged or compatible serializers
		for (RegisterClasses registerClass : RegisterClasses.values()) {
			if (registerClass.mSerializerType != null) {
				switch (registerClass.mSerializerType) {
				case TAGGED:
					registerClass.mSerializer = new TaggedFieldSerializer<Object>(kryo, registerClass.mType);
					break;

				case SERIALIZABLE_TAGGED:
					registerClass.mSerializer = new SerializableTaggedFieldSerializer(kryo, registerClass.mType);
					break;
				}
			}
		}
	}
	/**
	 * Serializer types
	 */
	private enum SerializerType {
		/** Creates a TaggedFieldSerializer for the type */
		TAGGED,
		/** Creates a SerializableTaggedFieldSerialize for the type */
		SERIALIZABLE_TAGGED,
	}
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
			vertices[i] = new Vector2();
			object.getVertex(i, vertices[i]);
		}
		kryo.writeObject(output, vertices);
	}

	@Override
	public PolygonShape read(Kryo kryo, Input input, Class<PolygonShape> type) {
		PolygonShape polygonShape = new PolygonShape();

		Vector2[] vertices = kryo.readObject(input, Vector2[].class);
		if (vertices.length > 0) {
			polygonShape.set(vertices);
		}

		return polygonShape;
	}

	@Override
	public PolygonShape copy(Kryo kryo, PolygonShape original) {
		PolygonShape polygonShape = new PolygonShape();

		Vector2[] vertices = new Vector2[original.getVertexCount()];
		for (int i = 0; i < vertices.length; ++i) {
			vertices[i] = new Vector2();
			original.getVertex(i, vertices[i]);
		}
		polygonShape.set(vertices);

		return polygonShape;
	}
}

/**
 * Serializes box2d edge shapes
 */
private static class EdgeShapeSerializer extends Serializer<EdgeShape> {
	@Override
	public void write(Kryo kryo, Output output, EdgeShape object) {
		Vector2 tempVector = new Vector2();

		kryo.setReferences(false);
		object.getVertex1(tempVector);
		kryo.writeObject(output, tempVector);
		object.getVertex2(tempVector);
		kryo.writeObject(output, tempVector);
		kryo.setReferences(true);
	}

	@Override
	public EdgeShape read(Kryo kryo, Input input, Class<EdgeShape> type) {
		EdgeShape edgeShape = new EdgeShape();

		kryo.setReferences(false);
		Vector2 vertex1 = kryo.readObject(input, Vector2.class);
		Vector2 vertex2 = kryo.readObject(input, Vector2.class);
		kryo.setReferences(true);

		edgeShape.set(vertex1, vertex2);

		return edgeShape;
	}

	@Override
	public EdgeShape copy(Kryo kryo, EdgeShape original) {
		EdgeShape edgeShape = new EdgeShape();

		Vector2 v1 = new Vector2();
		Vector2 v2 = new Vector2();

		original.getVertex1(v1);
		original.getVertex2(v2);
		edgeShape.set(v1, v2);

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
			Vector2 firstVertex = new Vector2();
			Vector2 lastVertex = new Vector2();
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
				vertices[i] = new Vector2();
				object.getVertex(i, vertices[i]);
			}
			kryo.writeObject(output, vertices);
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

	@Override
	public ChainShape copy(Kryo kryo, ChainShape original) {
		ChainShape chainShape = new ChainShape();

		int cVertices = original.getVertexCount();
		boolean isLoop;

		if (cVertices > 0) {
			Vector2 firstVertex = new Vector2();
			Vector2 lastVertex = new Vector2();
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
				vertices[i] = new Vector2();
				original.getVertex(i, vertices[i]);
			}

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
