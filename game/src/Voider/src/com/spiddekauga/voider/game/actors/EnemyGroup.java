package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.Json;
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
	public void setDuplicateTriggerDelay(float delay) {
		mTriggerDelay = delay;
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
	 * Updates the trigger information in the other actors (depending on the first actor)
	 */
	void updateTriggerInfos() {
		TriggerInfo originalTrigger = getTriggerActivate(mEnemies.get(0));

		// Update the other enemy triggers, create if must
		if (originalTrigger != null) {

		}
		// None exist, remove the other ones
		else {

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

	/** All the enemies */
	private ArrayList<EnemyActor> mEnemies = new ArrayList<EnemyActor>();
	/** All enemy references */
	private ArrayList<UUID> mEnemyIds = new ArrayList<UUID>();
	/** Trigger delay between enemies, in seconds */
	private float mTriggerDelay = 0;
}
