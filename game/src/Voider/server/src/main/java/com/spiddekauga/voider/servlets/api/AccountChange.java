package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.AccountChangeMethod;
import com.spiddekauga.voider.network.user.AccountChangeResponse;
import com.spiddekauga.voider.network.user.AccountChangeResponse.AccountChangeStatuses;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Change account settings of a user
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class AccountChange extends VoiderApiServlet {
	@Override
	protected void onInit() {
		mResponse = new AccountChangeResponse();
		mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
		mParameters = null;
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof AccountChangeMethod) {
				mParameters = (AccountChangeMethod) methodEntity;
				changeAccountSettings();
			}
		} else {
			mResponse.status = GeneralResponseStatuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	/**
	 * Change account settings
	 */
	private void changeAccountSettings() {
		// Password was changed
		if (mParameters.oldPassword != null) {
			changePassword();
		}

		if (mResponse.status == GeneralResponseStatuses.FAILED_SERVER_ERROR) {
			mResponse.status = GeneralResponseStatuses.SUCCESS;
		}
	}

	/**
	 * Try to change password
	 */
	private void changePassword() {
		// Old password correct
		if (isUserPasswordCorrect(mParameters.oldPassword)) {
			// Valid length -> Change password
			if (RegisterUser.isPasswordLengthValid(mParameters.newPassword)) {
				Entity entity = DatastoreUtils.getEntity(mUser.getKey());

				// New private key
				UUID privateKey = UUID.randomUUID();
				mResponse.privateKey = privateKey;
				DatastoreUtils.setProperty(entity, CUsers.PRIVATE_KEY, privateKey);

				// Hashed password
				String salt = BCrypt.gensalt();
				String hashedPassword = BCrypt.hashpw(mParameters.newPassword, salt);
				entity.setProperty(CUsers.PASSWORD, hashedPassword);

				DatastoreUtils.put(entity);

				mResponse.changeStatuses.add(AccountChangeStatuses.PASSWORD_SUCCESS);
			} else {
				mResponse.status = GeneralResponseStatuses.SUCCESS_PARTIAL;
				mResponse.changeStatuses.add(AccountChangeStatuses.PASSWORD_NEW_TOO_SHORT);
			}
		} else {
			mResponse.status = GeneralResponseStatuses.SUCCESS_PARTIAL;
			mResponse.changeStatuses.add(AccountChangeStatuses.PASSWORD_OLD_MISMATCH);
		}
	}

	/**
	 * @param password
	 * @return true if the password matches the one in the datastore
	 */
	private boolean isUserPasswordCorrect(String password) {
		Entity entity = DatastoreUtils.getEntity(mUser.getKey());

		String storedPassword = (String) entity.getProperty(CUsers.PASSWORD);
		return BCrypt.checkpw(password, storedPassword);
	}

	private AccountChangeMethod mParameters = null;
	private AccountChangeResponse mResponse = null;
}
