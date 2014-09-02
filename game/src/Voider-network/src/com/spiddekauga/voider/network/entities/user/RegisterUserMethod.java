package com.spiddekauga.voider.network.entities.user;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity.MethodNames;

/**
 * Registers a new user
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class RegisterUserMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return MethodNames.REGISTER_USER.toString();
	}

	/** Username */
	public String username;
	/** Password */
	public String password;
	/** email */
	public String email;
	/** Client id */
	public UUID clientId;
}
