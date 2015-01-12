package com.spiddekauga.voider.network.entities.user;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Logout response
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LogoutMethodResponse implements IEntity, ISuccessStatuses {
	/** Logout status */
	public GeneralResponseStatuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}
}
