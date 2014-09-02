package com.spiddekauga.voider.network.entities.user;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Logout method
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LogoutMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return "logout";
	}
}
