package com.spiddekauga.voider.network.user;

import java.util.UUID;

import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IMethodEntity;


/**
 * Log in method. Can use either private key or password.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginMethod implements IMethodEntity {
	private static final long serialVersionUID = 1L;

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
}
