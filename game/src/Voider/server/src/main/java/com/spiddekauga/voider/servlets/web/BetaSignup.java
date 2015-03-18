package com.spiddekauga.voider.servlets.web;

import java.util.Date;
import java.util.UUID;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CBetaSignUp;
import com.spiddekauga.voider.server.util.VoiderController;

/**
 * Signup for the beta :)
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BetaSignup extends VoiderController {

	@Override
	protected void onRequest() {
		// TODO forward to beta server if this is the release server

		// New sign-up
		if (isParameterExists("email")) {
			handleSignUp();
		}
		// Confirmation
		else if (isParameterExists("confirmation")) {
			handleConfirmation();
		}
	}

	/**
	 * Handle new sign up
	 */
	private void handleSignUp() {
		// Check if the email already exists and hasn't expired
		String email = getParameter("email");

		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.BETA_SIGNUP, new FilterWrapper(CBetaSignUp.EMAIL, email));

		// Check if expired
		if (entity != null) {
			Date confirmExpires = (Date) entity.getProperty(CBetaSignUp.CONFIRM_EXPIRES);

			// Not expired, send error
			if (confirmExpires.after(new Date())) {
				redirect("beta.jsp?already_signed_up");
				return;
			}
		}
		// New entity
		else {
			entity = new Entity(DatastoreTables.BETA_SIGNUP);
		}

		entity.setProperty(CBetaSignUp.EMAIL, email);
		entity.setProperty(CBetaSignUp.CONFIRM_KEY, toBase64(UUID.randomUUID()));
		entity.setProperty(CBetaSignUp.DATE, new Date());

		// TODO calculate when the beta key expires


		DatastoreUtils.put(entity);
	}

	/**
	 * Handle sign-up confirmation
	 */
	private void handleConfirmation() {

	}
}
