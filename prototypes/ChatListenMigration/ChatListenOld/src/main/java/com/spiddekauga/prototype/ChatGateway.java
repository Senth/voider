package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI.ChannelException;
import edu.gvsu.cis.masl.channelAPI.ChannelService;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ChatGateway implements ChannelService {
	/**
	 * Initialize the channel
	 * @param instanceName name of the instance (name used for logging)
	 */
	public ChatGateway(String instanceName) {
		mLogger = Logger.getLogger(instanceName);
		mLogger.setLevel(Level.ALL);
		mLogger.info("Initializining");
		mInstanceName = instanceName;

		try {
			mChannel = new ChannelAPI(URL, KEY, this);
			mChannel.open();
		} catch (IOException e) {
			mLogger.severe("Failed to initialize channel");
			e.printStackTrace();
		} catch (ChannelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run this chat, infinite loop
	 */
	public void run() {
		if (mChannel == null) {
			return;
		}

		try {
			while (true) {
				mChannel.send("From " + mInstanceName, METHOD);
				Thread.sleep(SLEEP_MS);
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose() {
		mLogger.info("Closed Channel");

	}

	@Override
	public void onError(Integer arg0, String arg1) {
		mLogger.severe("Error: " + arg0 + ", " + arg1);
	}

	@Override
	public void onMessage(String arg0) {
		mLogger.info("Got Message: " + arg0);
	}

	@Override
	public void onOpen() {
		mLogger.info("Opened Channel");
	}

	private String mInstanceName = null;
	private ChannelAPI mChannel = null;
	private Logger mLogger = null;

	private static final String URL = "http://voider-dev.appspot.com";
	// private static final String URL = "http://localhost:8888";
	private static final String KEY = "prototype-key";
	private static final String METHOD = "/chat-receive";
	private static final int SLEEP_MS = 3000;
}
