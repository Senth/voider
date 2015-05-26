package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for resetting the password
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PasswordResetMethod implements IMethodEntity {
	private static final long serialVersionUID = 1L;
	/** Email of the user */
	public String email;
	/** New password for the user */
	public String password;
	/** Token when resetting password */
	public String token;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.PASSWORD_RESET;
	}
}
