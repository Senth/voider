package com.spiddekauga.voider.network.entities;


/**
 * Log in method
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LoginMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return "login";
	}

	/** Username */
	public String username;
	/** Password */
	public String password;
}
