package com.spiddekauga.voider.network.entities.method;

import java.util.UUID;


/**
 * Log in method. Can use either private key or password.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LoginMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return MethodNames.LOGIN.toString();
	}

	/** Username */
	public String username;
	/** Password */
	public String password = null;
	/** Private key, alternative login method */
	public UUID privateKey = null;
}
