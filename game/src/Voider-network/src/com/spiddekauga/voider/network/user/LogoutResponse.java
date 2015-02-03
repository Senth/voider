package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Logout response
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LogoutResponse implements IEntity, ISuccessStatuses {
	/** Logout status */
	public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}
}
