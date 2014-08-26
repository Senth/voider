package com.spiddekauga.voider.prototype.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Serializes the entity into a byte string or vice versa
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class NetworkEntitySerializer {
	/**
	 * Deserializes an entity from bytes to an entity
	 * @param bytes all bytes that represents an entity
	 * @return entity or null if it could not deserialize
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
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}

		return null;
	}
}
