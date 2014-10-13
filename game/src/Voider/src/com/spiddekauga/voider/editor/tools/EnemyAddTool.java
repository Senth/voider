package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CEnemySetPath;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EnemyAddTool extends ActorAddTool {
	/**
	 * @param editor editor this tool is bound to
	 * @param selection all selected resources
	 * @param actorType the type of actor this tool uses
	 */
	public EnemyAddTool(IResourceChangeEditor editor, ISelection selection, Class<? extends Actor> actorType) {
		super(editor, selection, actorType);

		mSelectableResourceTypes.add(EnemyActor.class);
	}

	@Override
	protected boolean dragged() {
		if (mMovingActor != null) {
			Vector2 newPosition = getNewPosition();
			setSnapPosition((EnemyActor) mMovingActor, newPosition, (LevelEditor) mEditor, null);
			Pools.vector2.free(newPosition);
		}
		return false;
	}

	@Override
	protected boolean up(int button) {
		if (mMovingActor != null) {
			// Just set the new position
			if (mCreatedThisEvent) {
				Vector2 newPosition = getNewPosition();
				setSnapPosition((EnemyActor) mMovingActor, newPosition, (LevelEditor) mEditor, mInvoker);
				Pools.vector2.free(newPosition);
			}
			// If not new actor, reset to old position and move using command
			else {
				// Reset to old position first
				mMovingActor.setPosition(mDragOrigin);
				Vector2 newPosition = getNewPosition();
				setSnapPosition((EnemyActor) mMovingActor, newPosition, (LevelEditor) mEditor, mInvoker);
				Pools.vector2.free(newPosition);
			}

			mMovingActor.setIsBeingMoved(false);
			mMovingActor = null;
			mCreatedThisEvent = false;
		}
		return false;
	}

	/**
	 * Snaps the selected enemy position to a close path if one exist.
	 * @param enemyActor the enemy actor to move
	 * @param newPosition the new position of the actor
	 * @param levelEditor the level editor to get paths from
	 * @param invoker set this if you want to move the enemy using a command, set to null
	 *        to just set the position
	 */
	protected static void setSnapPosition(EnemyActor enemyActor, Vector2 newPosition, LevelEditor levelEditor, Invoker invoker) {
		// Non path enemy, just set position
		if (enemyActor.getDef(EnemyActorDef.class).getMovementType() != MovementTypes.PATH) {
			if (invoker != null) {
				invoker.execute(new CResourceMove(enemyActor, newPosition, levelEditor));
			} else {
				enemyActor.setPosition(newPosition);
			}
		}
		// Path enemy, try to snap
		else {
			Vector2 snappedPosition = Pools.vector2.obtain().set(newPosition);
			boolean usesPath = false;

			// Is the position close to a path?
			PathDistanceWrapper closestPath = getClosestPath(snappedPosition, levelEditor);
			if (closestPath != null) {
				if (closestPath.distance <= Editor.Level.ENEMY_SNAP_PATH_DISTANCE_SQ) {
					snappedPosition.set(closestPath.path.getCornerPosition(0));
					usesPath = true;
				}
			}

			if (!usesPath) {
				closestPath.path = null;
			}

			if (invoker != null) {
				// Only change if not same path as before
				if (closestPath.path != enemyActor.getPath() || closestPath.path == null || enemyActor.getPath() == null) {
					invoker.execute(new CResourceMove(enemyActor, snappedPosition, levelEditor));

					// Changed paths
					if (closestPath.path != null || enemyActor.getPath() != null) {
						invoker.execute(new CEnemySetPath(enemyActor, closestPath.path, levelEditor), true);
					}
				}
			} else {
				enemyActor.setPosition(snappedPosition);
			}

			Pools.vector2.free(snappedPosition);
		}
	}

	/**
	 * Calculates the closest path to the specified position
	 * @param position the position to search from
	 * @param levelEditor the level editor to search for paths
	 * @return closest path and the distance to the specified position
	 */
	private static PathDistanceWrapper getClosestPath(Vector2 position, LevelEditor levelEditor) {
		Path closestPath = null;
		float closestDistance = Float.POSITIVE_INFINITY;

		for (Path path : levelEditor.getPaths()) {
			if (path.getCornerCount() >= 2) {
				ArrayList<Vector2> pathCorners = path.getCorners();
				for (int i = 0; i < path.getCornerCount() - 1; ++i) {
					int nextIndex = i + 1;

					float diffDistance = Geometry.distBetweenPointLineSegmentSq(pathCorners.get(i), pathCorners.get(nextIndex), position);
					if (diffDistance < closestDistance) {
						closestDistance = diffDistance;
						closestPath = path;
					}
				}
			}
		}

		mTmpPathDistanceWrapper.distance = closestDistance;
		mTmpPathDistanceWrapper.path = closestPath;
		return mTmpPathDistanceWrapper;
	}

	/**
	 * Wrapper class for a pointer and a distance
	 */
	private static class PathDistanceWrapper {
		Path path;
		float distance;
	}

	/** Temporary path distance variable */
	private static PathDistanceWrapper mTmpPathDistanceWrapper = new PathDistanceWrapper();
}
