package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Reset password for the specified user

 */
public class PasswordResetSendTokenMethod implements IMethodEntity {
	/** User email to reset */
	public String email;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.PASSWORD_RESET_SEND_TOKEN;
	}
}
