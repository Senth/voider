package com.spiddekauga.voider.repo.stat;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.stat.LevelUserStatEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.repo.SqliteGateway;

/**
 * Database gateway for statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class StatSqliteGateway extends SqliteGateway {
	/**
	 * Increases the play count of a level. If no statistics exist for the level it will
	 * be created.
	 * @param id level/campaign id
	 * @param cleared true if the level/campaign was cleared
	 */
	void increasePlayCount(UUID id, boolean cleared) {
		// TODO
	}

	/**
	 * Sets the rating of a level/campaign.
	 * @param id level/campaign id
	 * @param rating in range [0,5]
	 */
	void setRating(UUID id, int rating) {
		// TODO
	}

	/**
	 * Set the level/ćampaign as bookmarked
	 * @param id level/campaign id
	 * @param bookmark true if bookmarked
	 */
	void setBookmark(UUID id, boolean bookmark) {
		// TODO
	}

	/**
	 * Set tags for the level/campaign
	 * @param id level/campaign id
	 * @param tags all tags
	 */
	void setTags(UUID id, ArrayList<Tags> tags) {
		// TODO
	}

	/**
	 * Get level/campaign statistics
	 * @param id level/campaign id
	 * @return statistics for the level, null if no statistics exists
	 */
	LevelUserStatEntity getLevelStats(UUID id) {
		// TODO
		return null;
	}

	/**
	 * Get all statistics that should be synced
	 */

}
