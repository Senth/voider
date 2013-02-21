package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.TriggerAction.Actions;
import com.spiddekauga.voider.game.TriggerInfo;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.Resource;

/**
 * Groups together enemies. These enemies have the same properties: Same position,
 * same, definition, same trigger, same everything. Except they have all have
 * different trigger delays, so they don't start at the same time
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyGroup extends Resource {
	/**
	 * Sets the enemy group id
	 */
	public EnemyGroup() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Sets an enemy to the enemy group. This automatically sets the enemy's
	 * group to this one. An enemy can only belong to one enemy group.
	 * This will be the original enemy of all duplicates
	 * @param enemyActor original enemy used for duplicating enemies in this group
	 * @pre this enemy group has to be empty
	 */
	public void setOriginalEnemy(EnemyActor enemyActor) {
		if (mEnemies.size() == 0) {
			mEnemies.add(enemyActor);
			mEnemyIds.add(enemyActor.getId());

			enemyActor.setEnemyGroup(this);
			enemyActor.setGroupLeader(true);
		} else {
			Gdx.app.error("EnemyGroup",	"Group is not empty when setOriginalEnemy() was called");
		}
	}

	/**
	 * Sets the number of enemies in this group. This will automatically create/delete
	 * other enemies if necessary.
	 * @param cEnemies number of enemies to have, must be set to 1 or higher.
	 * @param addedEnemies all enemies that were added due to increment of enemies,
	 * set to null if you don't want to use this.
	 * @param removedEnemies all enemies that were removed due to decrement of enemies,
	 * set to null if you don't want to use this.
	 */
	public void setEnemyCount(int cEnemies, ArrayList<EnemyActor> addedEnemies, ArrayList<EnemyActor> removedEnemies) {
		if (cEnemies < 1 || mEnemies.size() < 1) {
			return;
		}

		// Remove
		while (cEnemies < mEnemies.size()) {
			EnemyActor removedEnemy = mEnemies.remove(cEnemies);
			removedEnemy.setEnemyGroup(null);
			mEnemyIds.remove(cEnemies);

			if (removedEnemies != null) {
				removedEnemies.add(removedEnemy);
			}
		}

		TriggerInfo triggerInfo = getTriggerActivate(mEnemies.get(0));
		// Add
		while (cEnemies > mEnemies.size()) {
			EnemyActor copyEnemy = mEnemies.get(0).copy();

			if (triggerInfo != null) {
				TriggerInfo copyTriggerInfo = new TriggerInfo();
				copyTriggerInfo.action = Actions.ACTOR_ACTIVATE;
				copyTriggerInfo.triggerId = triggerInfo.triggerId;
				copyTriggerInfo.delay = triggerInfo.delay + mTriggerDelay * mEnemies.size();
			}

			mEnemies.add(copyEnemy);
			mEnemyIds.add(copyEnemy.getId());

			if (addedEnemies != null) {
				addedEnemies.add(copyEnemy);
			}
		}
	}

	/**
	 * @return number of enemies in the group (including the leader)
	 */
	public int getEnemyCount() {
		return mEnemies.size();
	}

	/**
	 * Clears all enemies. This will remove them from the group. Although the leader
	 * isn't in the returned array it is too removed from the group.
	 * @return all enemies that were removed from the group, except the group leader
	 */
	public ArrayList<EnemyActor> clear() {
		ArrayList<EnemyActor> removedEnemies = new ArrayList<EnemyActor>();

		for (EnemyActor enemyActor : mEnemies) {
			enemyActor.setEnemyGroup(null);
		}

		removedEnemies.addAll(mEnemies);
		removedEnemies.remove(0);

		mEnemies.clear();
		mEnemyIds.clear();

		return removedEnemies;
	}

	@Override
	public ArrayList<UUID> getReferences() {
		return mEnemyIds;
	}

	@Override
	public void bindReference(IResource resource) {
		int foundIndex = Collections.linearSearch(mEnemyIds, resource.getId());

		if (foundIndex != -1) {
			mEnemies.set(foundIndex, (EnemyActor) resource);
		} else {
			Gdx.app.error("EnemyGroup", "Could not find the enemy to bind to this group");
		}
	}

	/**
	 * Sets the duplicate trigger delay of the selected actor.
	 * @param delay seconds delay between each actor.
	 */
	public void setSpawnTriggerDelay(float delay) {
		mTriggerDelay = delay;
	}

	/**
	 * @return enemy spawn delay between the enemies
	 */
	public float getSpawnTriggerDelay() {
		return mTriggerDelay;
	}

	/**
	 * @return seconds of duplicate trigger delay between each actors. -1
	 * if no enemy is selected, or only one duplicate exist.
	 */
	public float getDuplicateTriggerDelay() {
		return mTriggerDelay;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mTriggerDelay", mTriggerDelay);
		json.writeValue("mEnemyIds", mEnemyIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mTriggerDelay = json.readValue("mTriggerDelay", float.class, jsonData);
		mEnemyIds = json.readValue("mEnemyIds", ArrayList.class, jsonData);

		// Fill enemies with null values
		for (int i = 0; i < mEnemyIds.size(); ++i) {
			mEnemies.add(null);
		}
	}

	/**
	 * Updates the specified TriggerInfo in the enemies, but not the leader
	 * @param leaderTrigger trigger of the leader which values to copy from
	 */
	void updateTrigger(TriggerInfo leaderTrigger) {
		for (int i = 1; i < mEnemies.size(); ++i) {
			TriggerInfo enemyTrigger = getTriggerInfoFromEnemy(mEnemies.get(i), leaderTrigger);

			// If action is to activate, this is a special case where the delay is
			// multiplied by the group delay
			enemyTrigger.action = leaderTrigger.action;
			enemyTrigger.triggerId = leaderTrigger.triggerId;

			if (leaderTrigger.action == Actions.ACTOR_ACTIVATE) {
				enemyTrigger.delay = leaderTrigger.delay + mTriggerDelay * i;
			} else {
				enemyTrigger.action = leaderTrigger.action;
			}
		}

		if (Config.DEBUG_TESTS) {
			int leaderSize = mEnemies.get(0).getTriggerInfos().size();

			for (int i = 1; i < mEnemies.size(); ++i) {
				if (mEnemies.get(i).getTriggerInfos().size() != leaderSize) {
					Gdx.app.error("EnemyGroup", "Not the same amount of triggers in the group!");
				}
			}
		}
	}

	/**
	 * Sets the position of the other enemies, not the leader
	 * @param leaderPosition position of the leader
	 */
	void setPosition(Vector2 leaderPosition) {
		for (int i = 1; i < mEnemies.size(); ++i) {
			mEnemies.get(i).setPosition(leaderPosition);
		}
	}

	/**
	 * Adds another Trigger to the other enemies, not the leader
	 * @param leaderTrigger trigger of the leader to add in the other enemies
	 */
	void addTrigger(TriggerInfo leaderTrigger) {
		for (int i = 1; i < mEnemies.size(); ++i) {
			TriggerInfo copyTriggerInfo = new TriggerInfo();
			copyTriggerInfo.action = leaderTrigger.action;
			copyTriggerInfo.triggerId = leaderTrigger.triggerId;

			if (leaderTrigger.action == Actions.ACTOR_ACTIVATE) {
				copyTriggerInfo.delay = leaderTrigger.delay + mTriggerDelay * i;
			} else {
				copyTriggerInfo.delay = leaderTrigger.delay;
			}

			mEnemies.get(i).addTrigger(copyTriggerInfo);
		}

		if (Config.DEBUG_TESTS) {
			int leaderSize = mEnemies.get(0).getTriggerInfos().size();

			for (int i = 1; i < mEnemies.size(); ++i) {
				if (mEnemies.get(i).getTriggerInfos().size() != leaderSize) {
					Gdx.app.error("EnemyGroup", "Not the same amount of triggers in the group!");
				}
			}
		}
	}

	/**
	 * Removes a Trigger from the other enemies, not the leader
	 * @param leaderTrigger trigger of the leader to remove in the other enemies
	 */
	void removeTrigger(TriggerInfo leaderTrigger) {
		for (int i = 1; i < mEnemies.size(); ++i) {
			TriggerInfo enemyTrigger = getTriggerInfoFromEnemy(mEnemies.get(i), leaderTrigger);
			mEnemies.get(i).removeTrigger(enemyTrigger);
		}


		if (Config.DEBUG_TESTS) {
			int leaderSize = mEnemies.get(0).getTriggerInfos().size();

			for (int i = 1; i < mEnemies.size(); ++i) {
				if (mEnemies.get(i).getTriggerInfos().size() != leaderSize) {
					Gdx.app.error("EnemyGroup", "Not the same amount of triggers in the group!");
				}
			}
		}
	}

	/**
	 * @return the trigger information that is set to activate from the specified enemy
	 * @param enemy the enemy to get the trigger activate from
	 */
	private TriggerInfo getTriggerActivate(EnemyActor enemy) {
		ArrayList<TriggerInfo> triggerInfos = enemy.getTriggerInfos();

		for (TriggerInfo triggerInfo : triggerInfos) {
			if (triggerInfo.action == Actions.ACTOR_ACTIVATE) {
				return triggerInfo;
			}
		}

		return null;
	}

	/**
	 * Gets the specified enemy's trigger for the specified trigger info.
	 * I.e. this will check in all the enmeny triggers until it finds the specified
	 * trigger.
	 * @param enemy the enemy to find the TriggerInfo in.
	 * @param searchTriggerInfo the trigger info to search for in the specified enemy
	 * @return TriggerInfo that have the same triggerId and action as the specified trigger.
	 * Null if the trigger info wasn't found inside the enemy.
	 */
	private TriggerInfo getTriggerInfoFromEnemy(EnemyActor enemy, TriggerInfo searchTriggerInfo) {
		for (TriggerInfo enemyTriggerInfo : enemy.getTriggerInfos()) {
			if (enemyTriggerInfo.sameTriggerAndAction(searchTriggerInfo)) {
				return enemyTriggerInfo;
			}
		}
		return null;
	}


	/** All the enemies */
	private ArrayList<EnemyActor> mEnemies = new ArrayList<EnemyActor>();
	/** All enemy references */
	private ArrayList<UUID> mEnemyIds = new ArrayList<UUID>();
	/** Trigger delay between enemies, in seconds */
	private float mTriggerDelay = Config.Editor.Level.Enemy.DELAY_DEFAULT;
}
