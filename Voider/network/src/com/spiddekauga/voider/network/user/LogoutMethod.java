package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Logout method
 */
public class LogoutMethod implements IMethodEntity {
@Override
public MethodNames getMethodName() {
	return MethodNames.LOGOUT;
}
}
