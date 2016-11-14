package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for resetting the password
 */
public class PasswordResetMethod implements IMethodEntity {
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
