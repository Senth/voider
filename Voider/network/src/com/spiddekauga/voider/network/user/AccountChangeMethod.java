package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Change account settings such as password

 */
public class AccountChangeMethod implements IMethodEntity {
	/** Old password of the user, set as null to skip changing password */
	public String oldPassword = null;
	/** New password of the user */
	public String newPassword = null;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.ACCOUNT_CHANGE;
	}

}
