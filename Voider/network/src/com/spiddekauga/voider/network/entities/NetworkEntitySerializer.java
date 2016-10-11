package com.spiddekauga.voider.network.entities;


import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.network.misc.ServerMessage;

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
	public synchronized static IEntity deserializeEntity(byte[] bytes) {
		Kryo kryo = mKryoPool.obtain();

		if (bytes == null || bytes.length == 0) {
			return null;
		}

		try {
			Input input = new Input(bytes);
			Object readObject = kryo.readClassAndObject(input);
			if (readObject instanceof IEntity) {
				return (IEntity) readObject;
			} else {
				mLogger.warning("Read object was not an entity");
			}

		} catch (IllegalArgumentException | KryoException e) {
			mLogger.severe("Failed to deserialize entity\n" + Strings.exceptionToString(e));
		}

		mKryoPool.free(kryo);

		return null;
	}

	/**
	 * Serialize the entity into a byte array
	 * @param entity the entity to serialize into a byte array
	 * @return entity as byte array
	 */
	public synchronized static byte[] serializeEntity(IEntity entity) {
		Kryo kryo = mKryoPool.obtain();

		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			Output output = new Output(byteOutputStream);
			kryo.writeClassAndObject(output, entity);
			output.close();
			byte[] entityBytes = byteOutputStream.toByteArray();
			return entityBytes;

		} catch (IllegalArgumentException | KryoException e) {
			mLogger.severe("Failed to serialize entity\n" + Strings.exceptionToString(e));
		}

		mKryoPool.free(kryo);

		return null;
	}

	/**
	 * Serialize server message
	 * @param message the server message
	 * @return the server message as a base64 message
	 */
	public synchronized static String serializeServerMessage(ServerMessage<?> message) {
		byte[] byteMessage = serializeEntity(message);
		return DatatypeConverter.printBase64Binary(byteMessage);
	}

	/**
	 * Deserialize a server message
	 * @param message the server message in base64 format
	 * @return the original server message
	 */
	public synchronized static ServerMessage<?> deserializeServerMessage(String message) {
		byte[] byteMessage = DatatypeConverter.parseBase64Binary(message);
		return (ServerMessage<?>) deserializeEntity(byteMessage);
	}

	private static final KryoNetPool mKryoPool = new KryoNetPool();
	private static final Logger mLogger = Logger.getLogger(NetworkEntitySerializer.class.getSimpleName());
}
