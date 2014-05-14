package com.spiddekauga.voider.server;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.User.UserEvents;

import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI.ChannelException;
import edu.gvsu.cis.masl.channelAPI.ChannelService;

/**
 * Gateway for all channel messages to and from the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MessageGateway implements ChannelService, Observer {
	/**
	 * Creates an empty (invalid) message gateway.
	 */
	private MessageGateway() {
		User user = User.getGlobalUser();
		user.addObserver(this);

		if (user.isOnline()) {
			connect();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		disconnect();
	};

	/**
	 * @return instance of this class.
	 * @note this instance could still be invalid to use. Check
	 * {@link #isConnected()} to see if the instance is valid to use.
	 */
	public static MessageGateway getInstance() {
		if (mInstance == null) {
			mInstance = new MessageGateway();
		}
		return mInstance;
	}

	/**
	 * Connect to the server
	 */
	private void connect() {
		if (User.getGlobalUser().isOnline()) {
			String username = User.getGlobalUser().getUsername();
			try {
				mChannel = new ChannelAPI(Config.Network.SERVER_HOST, username, this);
				mChannel.open();
			} catch (IOException | ChannelException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Disconnect from the server
	 */
	private void disconnect() {
		if (mChannel != null) {
			try {
				mChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		mChannel = null;
	}

	@Override
	public void update(Observable object, Object arg) {
		if (object instanceof User) {
			if (arg instanceof UserEvents) {
				switch ((UserEvents)arg) {
				case LOGIN:
					connect();
					break;

				case LOGOUT:
					disconnect();
					break;
				}
			}
		}
	}

	/**
	 * @return true if this message gateway is connected to the server
	 */
	public boolean isConnected() {
		return mChannel != null;
	}

	@Override
	public void onOpen() {
		Gdx.app.debug("MessageGateway", "Channel opened");
	}

	@Override
	public void onMessage(String message) {
		Gdx.app.debug("MessageGateway", "Message: " + message);
	}

	@Override
	public void onClose() {
		Gdx.app.debug("MessageGateway", "Channel closed");
	}

	@Override
	public void onError(Integer errorCode, String description) {
		Gdx.app.error("MessageGateway", "Error code: " + errorCode + ", desc: " + description);
	}

	/** Channel API */
	private ChannelAPI mChannel = null;

	/** Singleton instance of this class */
	private static MessageGateway mInstance = null;
}
