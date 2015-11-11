package com.spiddekauga.voider.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.misc.ChatMessage;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.repo.user.UserRepo;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;
import com.spiddekauga.voider.utils.event.MotdEvent;

import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI.ChannelException;
import edu.gvsu.cis.masl.channelAPI.ChannelService;

/**
 * Gateway for all channel messages to and from the server. Converts all messages to game
 * events.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MessageGateway implements ChannelService {
	/**
	 * Creates an empty (invalid) message gateway.
	 */
	private MessageGateway() {
		User user = User.getGlobalUser();
		new GameEventListener();

		if (user.isOnline()) {
			connect();
		}
	}

	/**
	 * @return instance of this class.
	 * @note this instance could still be invalid to use. Check {@link #isConnected()} to
	 *       see if the instance is valid to use.
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
			try {
				mChannel = new ChannelAPI(Config.Network.SERVER_HOST, UUID.randomUUID().toString(), this);
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
	public void disconnect() {
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

		ChatMessage<?> chatMessage = mGson.fromJson(message, ChatMessage.class);
		if (chatMessage != null) {
			if (chatMessage.skipClient == null || !chatMessage.skipClient.equals(mUserRepo.getClientId())) {
				fireGameEvent(chatMessage);
			}
		}
	}

	/**
	 * Fires the appropriate messages
	 * @param chatMessage server message
	 */
	@SuppressWarnings("unchecked")
	private void fireGameEvent(ChatMessage<?> chatMessage) {
		switch (chatMessage.type) {
		case MOTD:
			extractMotd(chatMessage);
			fireMotdEvent((ChatMessage<Motd>) chatMessage);
			break;

		case SERVER_MAINTENANCE:
			extractMotd(chatMessage);
			fireMotdEvent((ChatMessage<Motd>) chatMessage);
			mEventDispatcher.fire(new GameEvent(EventTypes.SERVER_MAINTENANCE));
			break;

		case SYNC_COMMUNITY_DOWNLOAD:
			mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_COMMUNITY_DOWNLOAD));
			break;

		case SYNC_HIGHSCORE:
			mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_HIGHSCORE));
			break;

		case SYNC_STATS:
			mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_STATS));
			break;

		case SYNC_USER_RESOURCES:
			mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_USER_RESOURCES));
			break;
		}
	}

	/**
	 * Fires a MOTD event
	 * @param chatMessage server message
	 */
	private void fireMotdEvent(ChatMessage<Motd> chatMessage) {
		ArrayList<Motd> motds = new ArrayList<>();
		motds.add(chatMessage.data);
		MotdEvent motdEvent = new MotdEvent(EventTypes.MOTD_NEW, motds);
		mEventDispatcher.fire(motdEvent);
	}

	/**
	 * Extract MOTD from the chat message
	 * @param chatMessage extract the MOTD from this message
	 */
	private void extractMotd(ChatMessage<?> chatMessage) {

	}

	@Override
	public void onClose() {
		Gdx.app.debug("MessageGateway", "Channel closed");
	}

	@Override
	public void onError(Integer errorCode, String description) {
		Gdx.app.error("MessageGateway", "Error code: " + errorCode + ", desc: " + description);
	}

	/**
	 * Class that listens to game event
	 */
	private class GameEventListener implements IEventListener {
		/**
		 * Initializes the listener
		 */
		GameEventListener() {
			EventDispatcher eventDispatcher = EventDispatcher.getInstance();

			eventDispatcher.connect(EventTypes.USER_CONNECTED, this);
			eventDispatcher.connect(EventTypes.USER_DISCONNECTED, this);
		}

		@Override
		public void handleEvent(GameEvent event) {
			switch (event.type) {
			case USER_CONNECTED:
				connect();
				break;

			case USER_DISCONNECTED: {
				disconnect();
				// // Disconnect in main thread
				// Gdx.app.postRunnable(new Runnable() {
				// @Override
				// public void run() {
				// disconnect();
				// }
				// });

				break;
			}

			default:
				break;
			}
		}
	}

	/** Message listeners */
	private ChannelAPI mChannel = null;
	private Gson mGson = new Gson();
	private UserRepo mUserRepo = UserRepo.getInstance();
	private final EventDispatcher mEventDispatcher = EventDispatcher.getInstance();

	private static MessageGateway mInstance = null;
}
