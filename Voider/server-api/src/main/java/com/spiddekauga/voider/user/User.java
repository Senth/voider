package com.spiddekauga.voider.user;

import com.google.appengine.api.datastore.Key;
import com.spiddekauga.voider.server.util.SessionVariable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Logged in user
 */
public class User extends SessionVariable implements Serializable {
/** Serializable id */
private static final long serialVersionUID = -7213740820922710320L;
/** Username */
private String mUsername = null;
/** User identity key */
private Key mId = null;
/** If the user is logged in */
private boolean mIsLoggedIn = false;
/** Client id */
private UUID mClientId = null;
/** Channel id */
private UUID mChannelId = null;

/**
 * LoginServlet the user and set all parameters
 * @param userId id/key of the user in the datastore
 * @param username
 * @param clientId where the user logged in from
 */
public void login(Key userId, String username, UUID clientId) {
	mId = userId;
	mIsLoggedIn = true;
	mClientId = clientId;
	mUsername = username;
	setChanged();
}

/**
 * Logs out the user
 */
public void logout() {
	mId = null;
	mIsLoggedIn = false;
	mClientId = null;
	mUsername = null;
	mChannelId = null;
	setChanged();
}

/**
 * @return username
 */
public String getUsername() {
	return mUsername;
}

/**
 * @return true if the user is logged in
 */
public boolean isLoggedIn() {
	return mIsLoggedIn;
}

/**
 * @return user id/key in the datastore
 */
public Key getKey() {
	return mId;
}

/**
 * @return client the user is logged in from
 */
public UUID getClientId() {
	return mClientId;
}

/**
 * @return channel id of the user
 */
public UUID getChannelId() {
	return mChannelId;
}

/**
 * Set the channel id for the user
 * @param channelId id of the channel
 */
public void setChannelId(UUID channelId) {
	mChannelId = channelId;
	setChanged();
}
}
