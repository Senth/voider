package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Server response from changing account
 */
public class AccountChangeResponse implements IEntity, ISuccessStatuses {
/** Response status */
public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;
/** What succeeded and failed to be changed */
public ArrayList<AccountChangeStatuses> changeStatuses = new ArrayList<>();
/** New password private key for auto-login if password was changed successfully */
public UUID privateKey = null;

@Override
public boolean isSuccessful() {
	if (!status.isSuccessful()) {
		return false;
	}

	for (AccountChangeStatuses changeStatus : changeStatuses) {
		if (!changeStatus.name().contains("SUCCESS")) {
			return false;
		}
	}

	return true;
}

/**
 * What can succeed and fail to change and why
 */
public enum AccountChangeStatuses {
	/** Old password mismatch */
	PASSWORD_OLD_MISMATCH,
	/** New password too short */
	PASSWORD_NEW_TOO_SHORT,
	/** Password was changed successful */
	PASSWORD_SUCCESS
}

}
