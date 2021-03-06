package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Bug report response from the server
 */
public class BugReportResponse implements IEntity, ISuccessStatuses {
/** Bug reports that failed to send */
public ArrayList<UUID> failedBugReports = new ArrayList<>();
/** Response status */
public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;

@Override
public boolean isSuccessful() {
	return status != null && status.isSuccessful();
}
}
