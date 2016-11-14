package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.UUID;

/**
 * Registers a new user
 */
public class RegisterUserMethod implements IMethodEntity {
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

@Override
public MethodNames getMethodName() {
	return MethodNames.REGISTER_USER;
}
}
