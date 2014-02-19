package com.spiddekauga.voider.network.entities.method;

/**
 * Logout method
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LogoutMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return "logout";
	}
}
