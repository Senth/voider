package com.spiddekauga.prototype;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import com.spiddekauga.network.WebGateway;
import com.spiddekauga.voider.prototype.entities.HighscoreGetMethod;
import com.spiddekauga.voider.prototype.entities.HighscoreGetMethodResponse;
import com.spiddekauga.voider.prototype.entities.IEntity;
import com.spiddekauga.voider.prototype.entities.NetworkEntitySerializer;


/**
 * Prototype testing
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PrototypeMain {
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		getHighscores();
	}

	/**
	 * Get highscores
	 */
	private static void getHighscores() {
		getHighscores(mLevelIds.get(0), false, "preparing");
		getHighscores(mLevelIds.get(0), false, "1,000 highscores");
		getHighscores(mLevelIds.get(0), false, "1,000 highscores [one fetch]");
		getHighscores(mLevelIds.get(1), false, "10,000 highscores");
		getHighscores(mLevelIds.get(1), false, "10,000 highscores [one fetch]");
	}

	/**
	 * Get a highscore from a specific level
	 * @param levelId id of the level
	 * @param debugIdentifier text for identifying this test
	 * @param fetchUsersInOneBatch true if we should fetch users in one batch in the
	 *        server
	 */
	private static void getHighscores(UUID levelId, boolean fetchUsersInOneBatch, String debugIdentifier) {
		HighscoreGetMethod method = new HighscoreGetMethod();
		method.levelId = levelId;
		method.oneBatch = fetchUsersInOneBatch;

		long startTime = System.nanoTime();

		byte[] entitySend = NetworkEntitySerializer.serializeEntity(method);
		byte[] responseGet = WebGateway.sendRequest(method.getMethodName(), entitySend);

		long endTime = System.nanoTime();
		long diffTime = endTime - startTime;

		IEntity response = NetworkEntitySerializer.deserializeEntity(responseGet);

		if (response instanceof HighscoreGetMethodResponse) {
			mLogger.info("[" + debugIdentifier + "] Time ms: " + (diffTime / 1000000));
		}
	}

	/** Level ids */
	private static ArrayList<UUID> mLevelIds = new ArrayList<>();

	/**
	 * Initialize levels
	 */
	static {
		mLevelIds.add(UUID.fromString("d0b97b20-2cef-11e4-8c21-0800200c9a66"));
		mLevelIds.add(UUID.fromString("d0b97b21-2cef-11e4-8c21-0800200c9a66"));
	}

	/** Logger */
	private static Logger mLogger = Logger.getLogger(PrototypeMain.class.getName());
}
