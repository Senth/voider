package com.spiddekauga.voider.network.user;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Registers a new user
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RegisterUserMethod implements IMethodEntity {
	@Override
	public MethodNames getMethodName() {
		return MethodNames.REGISTER_USER;
	}

	/** Register key; for the beta */
	public String key;
	/** Username */
	public String username;
	/** Password */
	public String password;
	/** email */
	public String email;
	/** Client id */
	public UUID clientId;
}
