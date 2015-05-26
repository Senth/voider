package com.spiddekauga.voider.network.analytics;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Server response from adding analytics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsResponse implements IEntity, ISuccessStatuses {
	/** Status */
	public GeneralResponseStatuses status = null;

	private static final long serialVersionUID = 1L;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

}
