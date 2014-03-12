package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Groups together enemies. These enemies have the same properties: Same position,
 * same, definition, same trigger, same everything. Except they have all have
 * different trigger delays, so they don't start at the same time
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
	public void setLeaderEnemy(EnemyActor enemyActor) {
		if (mEnemies.size() == 0) {
			mEnemies.add(enemyActor);

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

			if (removedEnemies != null) {
				removedEnemies.add(removedEnemy);
			}
		}

		TriggerInfo activateTrigger = TriggerInfo.getTriggerInfoByAction(mEnemies.get(0), Actions.ACTOR_ACTIVATE);

		// Add
		while (cEnemies > mEnemies.size()) {
			EnemyActor copyEnemy = mEnemies.get(0).copyNewResource();
			copyEnemy.destroyBody();

			// Set activate trigger
			if (activateTrigger != null) {
				TriggerInfo copyTriggerInfo = TriggerInfo.getTriggerInfoByAction(copyEnemy, Actions.ACTOR_ACTIVATE);
				copyTriggerInfo.delay = activateTrigger.delay + mTriggerDelay * mEnemies.size();
			}

			mEnemies.add(copyEnemy);

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
	 * @return all enemies that were removed from the group, except the group leader.
	 * Don't forget to free the array.
	 */
	public ArrayList<EnemyActor> clear() {
		@SuppressWarnings("unchecked")
		ArrayList<EnemyActor> removedEnemies = Pools.arrayList.obtain();

		for (EnemyActor enemyActor : mEnemies) {
			enemyActor.setEnemyGroup(null);
		}

		removedEnemies.addAll(mEnemies);
		removedEnemies.remove(0);

		mEnemies.clear();

		return removedEnemies;
	}

	/**
	 * Sets the duplicate trigger delay of the selected actor.
	 * @param delay seconds delay between each actor.
	 */
	public void setSpawnTriggerDelay(float delay) {
		mTriggerDelay = delay;

		// Update delay in triggers
		TriggerInfo leaderTriggerInfo = TriggerInfo.getTriggerInfoByAction(mEnemies.get(0), Actions.ACTOR_ACTIVATE);
		if (leaderTriggerInfo != null) {
			updateTrigger(leaderTriggerInfo);
		}
	}

	/**
	 * @return enemy spawn delay between the enemies
	 */
	public float getSpawnTriggerDelay() {
		return mTriggerDelay;
	}

	/**
	 * Updates the specified TriggerInfo in the enemies, but not the leader
	 * @param leaderTrigger trigger of the leader which values to copy from
	 */
	private void updateTrigger(TriggerInfo leaderTrigger) {
		for (int i = 1; i < mEnemies.size(); ++i) {
			TriggerInfo enemyTrigger = TriggerInfo.getTriggerInfoByDuplicate(mEnemies.get(i), leaderTrigger);

			// If action is to activate, this is a special case where the delay is
			// multiplied by the group delay
			if (leaderTrigger.action == Actions.ACTOR_ACTIVATE) {
				enemyTrigger.delay = leaderTrigger.delay + mTriggerDelay * i;
			} else {
				enemyTrigger.action = leaderTrigger.action;
			}
		}

		if (Debug.DEBUG_TESTS) {
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
	public void addTrigger(TriggerInfo leaderTrigger) {
		for (int i = 1; i < mEnemies.size(); ++i) {
			TriggerInfo copyTriggerInfo = leaderTrigger.copy();

			if (leaderTrigger.action == Actions.ACTOR_ACTIVATE) {
				copyTriggerInfo.delay = leaderTrigger.delay + mTriggerDelay * i;
			} else {
				copyTriggerInfo.delay = leaderTrigger.delay;
			}

			mEnemies.get(i).addTrigger(copyTriggerInfo);
		}

		if (Debug.DEBUG_TESTS) {
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
	public void removeTrigger(TriggerInfo leaderTrigger) {
		for (int i = 1; i < mEnemies.size(); ++i) {
			TriggerInfo enemyTrigger = TriggerInfo.getTriggerInfoByDuplicate(mEnemies.get(i), leaderTrigger);
			mEnemies.get(i).removeTrigger(enemyTrigger);
		}


		if (Debug.DEBUG_TESTS) {
			int leaderSize = mEnemies.get(0).getTriggerInfos().size();

			for (int i = 1; i < mEnemies.size(); ++i) {
				if (mEnemies.get(i).getTriggerInfos().size() != leaderSize) {
					Gdx.app.error("EnemyGroup", "Not the same amount of triggers in the group!");
				}
			}
		}
	}

	/** All the enemies */
	@Tag(6) private ArrayList<EnemyActor> mEnemies = new ArrayList<EnemyActor>();
	/** Trigger delay between enemies, in seconds */
	@Tag(7) private float mTriggerDelay = Config.Editor.Level.Enemy.DELAY_BETWEEN_DEFAULT;
}
