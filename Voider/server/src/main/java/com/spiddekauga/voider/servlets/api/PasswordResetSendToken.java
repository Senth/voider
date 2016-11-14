package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.xml.bind.DatatypeConverter;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenMethod;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenResponse;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPasswordReset;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Sends a key to the user's mail to

 */
@SuppressWarnings("serial")
public class PasswordResetSendToken extends VoiderApiServlet<PasswordResetSendTokenMethod> {

	@Override
	protected void onInit() {
		mResponse = new PasswordResetSendTokenResponse();
		mResponse.status = PasswordResetSendTokenResponse.Statuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(PasswordResetSendTokenMethod method) throws ServletException, IOException {
		Key userKey = getUserKey(method.email);
		if (userKey != null) {
			String token = generateToken(userKey);
			if (saveToken(userKey, token)) {
				if (sendToken(userKey, token)) {
					mResponse.status = Statuses.SUCCESS;
				}
			}
		} else {
			mResponse.status = Statuses.FAILED_EMAIL;
		}

		return mResponse;
	}

	/**
	 * Get user key from the email
	 * @param email
	 * @return user datastore key that has this email, null if none was found
	 */
	private Key getUserKey(String email) {
		return DatastoreUtils.getSingleKey(DatastoreTables.USERS, new FilterWrapper(CUsers.EMAIL, email));
	}

	/**
	 * Generate and a password token
	 * @param userKey user key to generate a new token for
	 * @return password reset token
	 */
	private String generateToken(Key userKey) {
		UUID uuidToken = UUID.randomUUID();
		ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
		byteBuffer.putLong(uuidToken.getLeastSignificantBits());
		byteBuffer.putLong(uuidToken.getMostSignificantBits());
		return DatatypeConverter.printBase64Binary(byteBuffer.array()).split("=")[0];
	}

	/**
	 * Save token to the datastore
	 * @param userKey
	 * @param token
	 * @return true if token was saved
	 */
	private boolean saveToken(Key userKey, String token) {
		if (token != null) {
			// Delete old tokens
			Key existingKey = DatastoreUtils.getSingleKey(DatastoreTables.PASSWORD_RESET, userKey);
			if (existingKey != null) {
				DatastoreUtils.delete(existingKey);
			}


			Date expires = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(ServerConfig.UserInfo.PASSWORD_RESET_EXPIRE_HOURS));

			Entity passwordEntity = new Entity(DatastoreTables.PASSWORD_RESET, userKey);
			passwordEntity.setProperty(CPasswordReset.TOKEN, token);
			passwordEntity.setProperty(CPasswordReset.EXPIRES, expires);

			Key passwordKey = DatastoreUtils.put(passwordEntity);

			return passwordKey != null;
		} else {
			return false;
		}
	}

	/**
	 * Send password token to the user
	 * @param userKey the user to send the password token to
	 * @param token password token
	 * @return true if the token was sent successfully
	 */
	private boolean sendToken(Key userKey, String token) {
		Entity userEntity = DatastoreUtils.getEntity(userKey);
		String sendToEmail = (String) userEntity.getProperty(CUsers.EMAIL);
		String sendToName = (String) userEntity.getProperty(CUsers.USERNAME);

		String body = "";
		body += "Hi " + sendToName + "!<br/><br/>";
		body += "A password reset has been initiated for your Voider account. If you "
				+ "haven't initiated this, the token will automatically expire in 24 hours.<br/><br/>";
		body += "<strong>YOUR KEY</strong><br/>";
		body += "----------------------<br/>";
		body += token + "<br/>";
		body += "----------------------<br/><br/>";

		body += "Start the game, press lost password, click on 'have token' and paste the token into the game.<br/><br/>";
		body += "Have a nice day! :)";

		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(ServerConfig.EMAIL_ADMIN);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(sendToEmail, sendToName));
			message.setSubject("[Voider] Password Reset");
			message.setContent(body, "text/html");
			Transport.send(message);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private PasswordResetSendTokenResponse mResponse = null;
}
