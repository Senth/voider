package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Logout method
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LogoutMethod implements IMethodEntity {
	private static final long serialVersionUID = 1L;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.LOGOUT;
	}
}
