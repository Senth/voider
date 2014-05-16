package com.spiddekauga.voider.utils;

import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.repo.ResourceLocalRepo;

/**
 * Handles sync of all the player statistics
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class StatSyncer {
	/**
	 * Tries to upload the stats. If it can't make a connection it will save
	 * the statistics into a file. If it succeeds to upload the player stats it
	 * will try to upload all the previously saved stats too.
	 * @param playerStats player statistics from the last level played
	 */
	public static void uploadStats(PlayerStats playerStats) {
		boolean uploaded = false;
		/** @todo upload stats to the server */

		// TODO sync all the saved statistics
		if (uploaded) {

		}
		// Save the statistics
		else {
			ResourceLocalRepo.save(playerStats);
		}
	}


}
