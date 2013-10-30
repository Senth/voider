package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CEnemySetPath;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.utils.Pools;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyAddTool extends ActorAddTool {
	/**
	 * @param camera used for picking point on screen to world
	 * @param world used for converting screen coordinates to world coordinates
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor editor this tool is bound to
	 * @param actorType the type of actor this tool uses
	 */
	public EnemyAddTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor, Class<? extends Actor> actorType) {
		super(camera, world, invoker, selection, editor, actorType);
	}

	@Override
	protected boolean down() {
		// Move is hit an actor of selected type?
		if (mSelection.isSelectionChangedDuringDown()) {
			testPickPoint();
		}
		// Create a new actor here (if we have selected a definition
		else if (mActorDef != null) {
			mMovingActor = createNewSelectedActor();
			mCreatedThisEvent = true;
		}

		if (mMovingActor != null) {
			mDragOrigin.set(mMovingActor.getPosition());
			return true;
		}

		return false;
	}

	@Override
	protected boolean dragged() {
		if (mMovingActor != null) {
			Vector2 newPosition = getNewPosition();
			setSnapPosition((EnemyActor)mMovingActor, newPosition, (LevelEditor)mEditor, null);
			Pools.vector2.free(newPosition);
		}
		return false;
	}

	@Override
	protected boolean up() {
		if (mMovingActor != null) {
			// Just set the new position
			if (mCreatedThisEvent) {
				Vector2 newPosition = getNewPosition();
				setSnapPosition((EnemyActor)mMovingActor, newPosition, (LevelEditor)mEditor, null);
				Pools.vector2.free(newPosition);
			}
			// If not new actor, reset to old position and move using command
			else {
				// Reset to old position first
				mMovingActor.setPosition(mDragOrigin);
				Vector2 newPosition = getNewPosition();
				setSnapPosition((EnemyActor)mMovingActor, newPosition, (LevelEditor)mEditor, mInvoker);
				Pools.vector2.free(newPosition);
			}

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
	 * @param invoker set this if you want to move the enemy using a command, set to null to just
	 * set the position
	 */
	protected static void setSnapPosition(EnemyActor enemyActor, Vector2 newPosition, LevelEditor levelEditor, Invoker invoker) {
		Vector2 snappedPosition = Pools.vector2.obtain().set(newPosition);
		boolean usesPath = false;

		// Is the position close to a path?
		Path closestPath = getClosestPath(snappedPosition, levelEditor);
		if (closestPath != null) {
			Vector2 diffVector = Pools.vector2.obtain();
			diffVector.set(snappedPosition).sub(closestPath.getCornerPosition(0));

			if (diffVector.len2() <= Editor.Level.ENEMY_SNAP_PATH_DISTANCE_SQ) {
				snappedPosition.set(closestPath.getCornerPosition(0));
				usesPath = true;
			}

			Pools.vector2.free(diffVector);
		}

		if (!usesPath) {
			closestPath = null;
		}

		if (invoker != null) {
			// Only change if not same path as before
			if (closestPath != enemyActor.getPath() || closestPath == null || enemyActor.getPath() == null) {
				invoker.execute(new CResourceMove(enemyActor, snappedPosition, levelEditor));

				// Changed paths
				if (closestPath != null || enemyActor.getPath() != null) {
					invoker.execute(new CEnemySetPath(enemyActor, closestPath, levelEditor), true);
				}
			}
		} else {
			enemyActor.setPosition(snappedPosition);
		}

		Pools.vector2.free(snappedPosition);
	}

	/**
	 * Calculates the closest path to the specified position
	 * @param position the position to search from
	 * @param levelEditor the level editor to search for paths
	 * @return closest path, null if none exist
	 */
	private static Path getClosestPath(Vector2 position, LevelEditor levelEditor) {
		Path closestPath = null;
		float closestDistance = Float.POSITIVE_INFINITY;
		Vector2 diffVector = Pools.vector2.obtain();

		for (Path path : levelEditor.getPaths()) {
			if (path.getCornerCount() >= 2) {
				diffVector.set(path.getCornerPosition(0));
				diffVector.sub(position);

				float diffDistance = diffVector.len2();
				if (diffDistance < closestDistance) {
					closestDistance = diffDistance;
					closestPath = path;
				}
			}
		}

		Pools.vector2.free(diffVector);

		return closestPath;
	}
}
