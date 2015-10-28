package com.spiddekauga.voider.network.user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;


/**
 * Log in method. Can use either private key or password.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginMethod implements IMethodEntity {
	private static final long serialVersionUID = 2L;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.LOGIN;
	}

	/** Client id */
	public UUID clientId = null;
	/** Username */
	public String username;
	/** Password */
	public String password = null;
	/** Private key, alternative login method */
	public UUID privateKey = null;
	/** Client version */
	public int clientVersion = ClientVersions.getLatest().ordinal();

	/** Extra parameters */
	private transient HashMap<String, IEntity> mParameters = new HashMap<>();
	/** Extra parameters that are serialized */
	private HashMap<String, byte[]> mSerializedParameters = new HashMap<>();

	/**
	 * Serialize extra object before serializing this instance
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Write objects to serialized objects variable
		for (Entry<String, IEntity> entry : mParameters.entrySet()) {
			byte[] serializedEntity = NetworkEntitySerializer.serializeEntity(entry.getValue());
			mSerializedParameters.put(entry.getKey(), serializedEntity);
		}

		out.defaultWriteObject();
	}

	/**
	 * Deserialize extra objects after deserializing this instance
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		for (Entry<String, byte[]> entry : mSerializedParameters.entrySet()) {
			IEntity object = NetworkEntitySerializer.deserializeEntity(entry.getValue());
			if (object != null) {
				mParameters.put(entry.getKey(), object);
			}
		}
	}

	/**
	 * Class for client version
	 */
	public static class ClientVersion implements IEntity {
		private static final long serialVersionUID = 1L;


	}
}
