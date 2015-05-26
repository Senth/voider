package com.spiddekauga.voider.network.entities;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import com.spiddekauga.utils.Strings;

/**
 * Serializes the entity into a byte string or vice versa
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class NetworkEntitySerializer {
	/**
	 * Deserializes an entity from bytes to an entity
	 * @param bytes all bytes that represents an entity
	 * @return entity, or null if it could not deserialize
	 */
	public static IEntity deserializeEntity(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		try {
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);

			Object readObject = objectInputStream.readObject();
			objectInputStream.close();
			if (readObject instanceof IEntity) {
				return (IEntity) readObject;
			} else {
				mLogger.warning("Read object was not an entity");
			}

		} catch (IOException | ClassNotFoundException e) {
			mLogger.severe("Failed to deserialize entity\n" + Strings.exceptionToString(e));
		}

		return null;
	}

	/**
	 * Serialize the entity into a byte array
	 * @param entity the entity to serialize into a byte array
	 * @return entity as byte array
	 */
	public static byte[] serializeEntity(IEntity entity) {
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

			objectOutputStream.writeObject(entity);
			objectOutputStream.flush();
			byte[] entityBytes = byteOutputStream.toByteArray();
			objectOutputStream.close();

			return entityBytes;
		} catch (IOException e) {
			mLogger.severe("Failed to serialize entity\n" + Strings.exceptionToString(e));
		}

		return null;
	}

	private static final Logger mLogger = Logger.getLogger(NetworkEntitySerializer.class.getSimpleName());
}
