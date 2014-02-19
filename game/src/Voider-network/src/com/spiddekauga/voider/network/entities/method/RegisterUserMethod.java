package com.spiddekauga.voider.network.entities.method;

/**
 * Registers a new user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
}
