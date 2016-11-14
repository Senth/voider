package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Groups together enemies. These enemies have the same properties: Same position, same, definition,
 * same trigger, same everything. Except they have all have different trigger delays, so they don't
 * start at the same time
 */
public class EnemyGroup extends Resource {
/** All the enemies */
@Tag(6)
private ArrayList<EnemyActor> mEnemies = new ArrayList<EnemyActor>();
/** Trigger delay between enemies, in seconds */
@Tag(7)
private float mTriggerDelay = Config.Editor.Level.Enemy.DELAY_BETWEEN_DEFAULT;

/**
 * Sets the enemy group id
 */
public EnemyGroup() {
	mUniqueId = UUID.randomUUID();
}

/**
 * Sets an enemy to the enemy group. This automatically sets the enemy's group to this one. An enemy
 * can only belong to one enemy group. This will be the original enemy of all duplicates
 * @param enemyActor original enemy used for duplicating enemies in this group
 * @pre this enemy group has to be empty
 */
public void setLeaderEnemy(EnemyActor enemyActor) {
	if (mEnemies.size() == 0) {
		mEnemies.add(enemyActor);

		enemyActor.setEnemyGroup(this);
		enemyActor.setGroupLeader(true);
	} else {
		Gdx.app.error(EnemyGroup.class.getSimpleName(), "Group is not empty when setLeaderEnemy() was called");
	}
}

/**
 * Sets the number of enemies in this group. This will automatically create/delete other enemies if
 * necessary.
 * @param cEnemies number of enemies to have, must be set to 1 or higher.
 * @param addedEnemies all enemies that were added due to increment of enemies, set to null if you
 * don't want to use this.
 * @param removedEnemies all enemies that were removed due to decrement of enemies, set to null if
 * you don't want to use this.
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

	// Add
	while (cEnemies > mEnemies.size()) {
		EnemyActor copyEnemy = mEnemies.get(0).copyForGroup();
		copyEnemy.destroyBody();

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
 * Clears all enemies. This will remove them from the group. Although the leader isn't in the
 * returned array it is too removed from the group.
 * @return all enemies that were removed from the group, except the group leader.
 */
public ArrayList<EnemyActor> clear() {
	ArrayList<EnemyActor> removedEnemies = new ArrayList<>();

	for (EnemyActor enemyActor : mEnemies) {
		enemyActor.setEnemyGroup(null);
	}

	removedEnemies.addAll(mEnemies);
	removedEnemies.remove(0);

	mEnemies.clear();

	return removedEnemies;
}

/**
 * @return enemy spawn delay between the enemies
 */
public float getSpawnTriggerDelay() {
	return mTriggerDelay;
}

/**
 * Sets the duplicate trigger delay of the selected actor.
 * @param delay seconds delay between each actor.
 */
public void setSpawnTriggerDelay(float delay) {
	mTriggerDelay = delay;
}

/**
 * Get the spawning position (index) of the specified enemy
 * @param enemyActor
 * @return spawning position [1, getEnemyCount()], -1 if the enemy wasn't found in this group
 */
public int getEnemySpawnIndex(EnemyActor enemyActor) {
	for (int i = 0; i < mEnemies.size(); ++i) {
		if (mEnemies.get(i) == enemyActor) {
			return i + 1;
		}
	}
	return -1;
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
 * @return the leaders activate trigger if it's a custom set trigger.
 */
public TriggerInfo getSpawnTrigger() {
	EnemyActor leader = getLeader();
	if (leader != null) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(leader, Actions.ACTOR_ACTIVATE);
		if (triggerInfo != null && !(triggerInfo.trigger instanceof TActorActivated)) {
			return triggerInfo;
		}
	}
	return null;
}

/**
 * @return get the enemy leader, null if none has been set
 */
public EnemyActor getLeader() {
	if (!mEnemies.isEmpty()) {
		return mEnemies.get(0);
	} else {
		return null;
	}
}

/**
 * @return the leaders deactivate trigger if it's a custom set trigger
 */
public TriggerInfo getDeactivateTrigger() {
	EnemyActor leader = getLeader();
	if (leader != null) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(leader, Actions.ACTOR_DEACTIVATE);
		if (triggerInfo != null && !(triggerInfo.trigger instanceof TActorActivated)) {
			return triggerInfo;
		}
	}
	return null;
}

@Override
public void removeBoundResource(IResource boundResource, List<Command> commands) {
	super.removeBoundResource(boundResource, commands);

	if (boundResource instanceof EnemyActor) {
		final EnemyActor enemyActor = (EnemyActor) boundResource;

		final int index = mEnemies.indexOf(enemyActor);
		if (index != -1) {
			Command command = new Command() {
				@Override
				public boolean execute() {
					mEnemies.remove(index);
					return true;
				}

				@Override
				public boolean undo() {
					mEnemies.add(index, enemyActor);
					return true;
				}
			};
			commands.add(command);
		}
	}
}

/**
 * @return all enemies in this group
 */
public List<EnemyActor> getEnemies() {
	return mEnemies;
}
}
