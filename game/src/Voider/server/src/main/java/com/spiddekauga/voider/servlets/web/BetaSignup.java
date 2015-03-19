package com.spiddekauga.voider.servlets.web;

import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.utils.SystemProperty;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.server.util.ServerConfig;
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
		// Forward to beta server if this is the release server
		if (SystemProperty.applicationId.get().equals("voider-release")) {
			String parameters = "";

			for (Entry<String, String[]> post : getParameters().entrySet()) {
				if (!parameters.isEmpty()) {
					parameters += "&";
				}
				parameters += post.getKey();

				boolean first = true;
				for (String value : post.getValue()) {
					if (first) {
						parameters += "=";
					} else {
						parameters += "&" + post.getKey() + "=";
					}
					parameters += value;
				}
			}

			super.redirect(ServerConfig.BETA_URL + "beta-signup?" + parameters);
		}


		if (isParameterSet("email")) {
			// Resend key
			if (isParameterSet("resend_key")) {
				handleResendKey();
			}
			// Resend confirmation key
			else if (isParameterSet("resend_confirm")) {
				handleResendConfirm();
			}
			// New sign-up
			else {
				handleSignUp();
			}
		}
		// Confirmation
		else if (isParameterSet("confirm_key")) {
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

			// Already confirmed
			if (confirmExpires == null) {
				// Have the player gotten a key?
				if (entity.getProperty(CBetaSignUp.BETA_KEY) != null) {
					redirect("key_gotten&email=" + email);
				} else {
					redirect("in_queue");
				}
				return;
			}
			// Not expired, send error
			else if (confirmExpires.after(new Date())) {
				redirect("not_confirmed");
				return;
			}
		}
		// New entity
		else {
			entity = new Entity(DatastoreTables.BETA_SIGNUP);
		}

		Date curDate = new Date();
		Date expireDate = new Date(curDate.getTime() + TimeUnit.DAYS.toMillis(7));
		String confirmKey = toBase64(UUID.randomUUID());

		entity.setProperty(CBetaSignUp.EMAIL, email);
		entity.setProperty(CBetaSignUp.CONFIRM_KEY, confirmKey);
		entity.setProperty(CBetaSignUp.DATE, curDate);
		entity.setProperty(CBetaSignUp.CONFIRM_EXPIRES, expireDate);

		DatastoreUtils.put(entity);

		sendConfirmationMail(email, confirmKey);

		redirect("success");
	}

	/**
	 * Send the confirmation mail
	 * @param email the email to send the confirmation mail to
	 * @param confirmKey
	 */
	private void sendConfirmationMail(String email, String confirmKey) {
		String body = "";

		body += "You have signed up to the Voider beta. Please confirm this by clicking the link below within one week. "
				+ "If you haven't signed up for the beta you can casually ignore this email :)";

		body += "<br /><br />";
		body += "-----------------------------------------<br />";
		body += "<a href=\"http://voider-game.com/beta-signup?confirm_key=" + confirmKey + "\">CLICK TO CONFIRM</a><br />";
		body += "-----------------------------------------<br /><br />";

		body += "After the confirmation you've been placed in the queue for attaining a beta key";

		sendEmail(email, "Beta Sign-up Confirmation Link", body);
	}

	@Override
	protected void redirect(String url) {
		String redirectUrl = "";
		// Beta client should always redirect to the release home page
		if (SystemProperty.applicationId.get().equals("voider-beta")) {
			redirectUrl = ServerConfig.RELEASE_URL;
		}

		redirectUrl += "beta-signup-confirmation.jsp?" + url;


		super.redirect(redirectUrl);
	}

	/**
	 * Resend the key
	 */
	private void handleResendKey() {
		String email = getParameter("email");
		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.BETA_SIGNUP, new FilterWrapper(CBetaSignUp.EMAIL, email));

		// Check if expired
		if (entity != null) {
			Date confirmExpires = (Date) entity.getProperty(CBetaSignUp.CONFIRM_EXPIRES);

			// Already confirmed
			if (confirmExpires == null) {
				// Have the player gotten a key?
				String betaKey = (String) entity.getProperty(CBetaSignUp.BETA_KEY);
				if (betaKey != null) {
					sendBetaKey(email, betaKey);
				}
			}
		}

		redirect("resend_key");
	}

	/**
	 * Resend confirmation key
	 */
	private void handleResendConfirm() {
		String email = getParameter("email");
		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.BETA_SIGNUP, new FilterWrapper(CBetaSignUp.EMAIL, email));

		// Check if expired
		if (entity != null) {
			Date confirmExpires = (Date) entity.getProperty(CBetaSignUp.CONFIRM_EXPIRES);

			// Not expired, send error
			if (confirmExpires != null && confirmExpires.after(new Date())) {
				String confirmKey = (String) entity.getProperty(CBetaSignUp.CONFIRM_KEY);

				if (confirmKey != null) {
					sendConfirmationMail(email, confirmKey);
				}
			}
		}

		redirect("resend_confirm");
	}

	/**
	 * Resend the beta key
	 * @param email
	 * @param betaKey
	 */
	private void sendBetaKey(String email, String betaKey) {
		String body = "";

		body += "YOUR BETA KEY:<br />";
		body += "-----------------------------------------<br />";
		body += "<b>" + betaKey + "</b><br />";
		body += "-----------------------------------------<br />";
		body += "This key is used when regestring the game.<br /><br />";

		body += "<a href=\"" + ServerConfig.BETA_CLIENT_URL + "\">Download Voider Beta PC Client</a><br />";
		body += "Check out useful beta information tutorials at <a href=\"" + ServerConfig.BETA_INFO_URL + "\">" + ServerConfig.BETA_INFO_URL
				+ "</a>";

		sendEmail(email, "Voider Beta Key", body);
	}

	/**
	 * Handle sign-up confirmation
	 */
	private void handleConfirmation() {
		String confirmKey = getParameter("confirm_key");

		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.BETA_SIGNUP, new FilterWrapper(CBetaSignUp.CONFIRM_KEY, confirmKey));

		if (entity != null) {
			entity.removeProperty(CBetaSignUp.CONFIRM_EXPIRES);
			entity.removeProperty(CBetaSignUp.CONFIRM_KEY);

			DatastoreUtils.put(entity);
		}

		redirect("confirm_success");
	}
}
