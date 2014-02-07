package com.spiddekauga.prototype.network;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.spiddekauga.utils.kryo.SerializableTaggedFieldSerializer;
import com.spiddekauga.utils.kryo.UUIDSerializer;

/**
 * Creates kryo objects correctly with all classes registered
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class KryoFactory {
	/**
	 * @return new kryo object
	 */
	public static Kryo createKryo() {
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(true);

		RegisterClasses.registerAll(kryo);

		return kryo;
	}


	/**
	 * Contains all classes that should be registered.
	 * Adding new classes shall only be done at the end of the enumeration.
	 * If a class isn't used any longer, don't remove it but set it as null instead.
	 */
	private enum RegisterClasses {
		/** ActorDef */
		ACTOR_DEF(OldActorDef.class, SerializerType.TAGGED),
		/** byte[] */
		BYTE_ARRAY(byte[].class),
		/** Def */
		DEF(OldDef.class),
		/** HashMap */
		HASH_MAP(HashMap.class),
		/** HashSet */
		HASH_SET(HashSet.class),
		/** Date */
		DATE(Date.class),
		/** UUID */
		UUID(UUID.class, new UUIDSerializer()),
		/** EnemyDef */
		ENEMY_DEF(OldEnemyDef.class, SerializerType.TAGGED),
		/** WeaponDef */
		WEAPON_DEF(OldWeaponDef.class, SerializerType.TAGGED),
		/** LevelDef */
		LEVEL_DEF(OldLevelDef.class, SerializerType.TAGGED),
		/** Resource */
		RESOURCE(OldResource.class, SerializerType.TAGGED),


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
		 * @param type the type to register, if null it won't register it. Setting to null is useful
		 * when the class isn't used anymore (doesn't exist) but we still need to keep the register
		 * order.
		 */
		private RegisterClasses(Class<?> type) {
			mType = type;
		}

		/**
		 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
		 * and when {@link #createSerializers(Kryo)} is called will created the
		 * specified serializer type
		 * @param type the type to register, if null it won't register it. Setting to null is useful
		 * when the class isn't used anymore (doesn't exist) but we still need to keep the register
		 * order.
		 * @param createSerializerType the type of serializer to create when {@link #createSerializers(Kryo)}
		 * is called.
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
		 * Some classes needs a serializer that requires Kryo in the constructor. These serializers
		 * are created with this method instead.
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
		private Serializer<?> mSerializer = null;
		/** If a serializer of the specified type should be created for this class.
		 * If null, no serializer will be created for this type. */
		private SerializerType mSerializerType = null;
	}
}
