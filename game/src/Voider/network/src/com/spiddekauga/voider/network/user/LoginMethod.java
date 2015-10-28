package com.spiddekauga.voider.network.user;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IMethodEntity;


/**
 * Log in method. Can use either private key or password.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginMethod implements IMethodEntity {
	@Override
	public MethodNames getMethodName() {
		return MethodNames.LOGIN;
	}

	/** Client id */
	@Tag(1) public UUID clientId;
	/** Username */
	@Tag(2) public String username;
	/** Password */
	@Tag(3) public String password;
	/** Private key, alternative login method */
	@Tag(4) public UUID privateKey;
	/** Client version */
	@Tag(5) public int clientVersion = ClientVersions.getLatest().ordinal();
}
