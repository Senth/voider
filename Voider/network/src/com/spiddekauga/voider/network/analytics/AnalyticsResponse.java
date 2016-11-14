package com.spiddekauga.voider.network.analytics;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Server response from adding analytics
 */
public class AnalyticsResponse implements IEntity, ISuccessStatuses {
/** Status */
public GeneralResponseStatuses status = null;

@Override
public boolean isSuccessful() {
	return status != null && status.isSuccessful();
}

}
