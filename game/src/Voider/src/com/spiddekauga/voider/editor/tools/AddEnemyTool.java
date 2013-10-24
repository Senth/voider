package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CEnemySetPath;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.editor.commands.CResourceSelect;
import com.spiddekauga.voider.editor.commands.CTriggerSet;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for adding enemies. This also has the ability to create a stack
 * of enemies that are grouped together. These enemies are essentially the
 * same, same path, same location, etc. The only difference is that
 * they can have a delay when they are activated.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AddEnemyTool extends AddActorTool {
	/**
	 * Creates an add enemy tool. You still need to set the enemy actor definition
	 * via {@link #setNewActorDef(com.spiddekauga.voider.game.actors.ActorDef)} to make
	 * it work.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param invoker used for undo/redo actions
	 * @param editor will be called when actors are added/removed
	 */
	public AddEnemyTool(Camera camera, World world, Invoker invoker, LevelEditor editor) {
		super(camera, world, invoker, EnemyActor.class, true, editor);
		mLevelEditor = editor;
	}

	/**
	 * States of add enemy tool, appends SET_ACTIVATE_TRIGGER and SET_DEACTIVATE_TRIGGER
	 */
	public enum States{
		/** Creates a new actor when pressed */
		ADD,
		/** Removes the actor that was hit */
		REMOVE,
		/** Moves the actor that was hit */
		MOVE,
		/** Selects an actor */
		SELECT,
		/** Selects an activate trigger */
		SET_ACTIVATE_TRIGGER,
		/** Selects an deactivate trigger */
		SET_DEACTIVATE_TRIGGER
	}

	@Override
	public void setSelectedResource(IResource selectedResource) {
		super.setSelectedResource(selectedResource);

		if (selectedResource instanceof EnemyActor) {
			mEnemyGroup = ((EnemyActor) selectedResource).getEnemyGroup();
		}
	}

	/**
	 * Sets the enemy state, not to be confused with {@link #setState(com.spiddekauga.voider.editor.tools.AddActorTool.States)}
	 * Always use this instead of {@link #setState(com.spiddekauga.voider.editor.tools.AddActorTool.States)}.
	 * @param state new state of the tool
	 */
	public void setEnemyState(States state) {
		deactivate();

		mState = state;
		// Set super state
		switch (mState) {
		case ADD:
			setState(AddActorTool.States.ADD);
			break;

		case REMOVE:
			setState(AddActorTool.States.REMOVE);
			break;

		case MOVE:
			setState(AddActorTool.States.MOVE);
			break;

		case SELECT:
			setState(AddActorTool.States.SELECT);
			break;

		default:
			// Does nothing
			break;
		}

		activate();
	}

	/**
	 * Returns current enemy state, not to be confused with #
	 * @return current enemy state.
	 */
	public States getEnemyState() {
		return mState;
	}

	@Override
	public void deactivate() {
		super.deactivate();
	}

	@Override
	public void activate() {
		super.activate();
	}

	@Override
	protected boolean down() {
		switch (mState) {
		case ADD:
		case MOVE:
		case SELECT:
		case REMOVE:
			super.down();
			break;

		case SET_ACTIVATE_TRIGGER:
		case SET_DEACTIVATE_TRIGGER:
			testPickAabb(Editor.PICK_TRIGGER_SIZE);

			if (mHitBody != null) {
				Object hitObject = mHitBody.getUserData();

				// Hit enemy
				if (hitObject instanceof EnemyActor) {
					mInvoker.execute(new CResourceSelect((IResource) hitObject, this));
				}
				// Hit trigger
				else if (hitObject instanceof Trigger) {
					// Only select trigger if we have an enemy selected
					if (mSelectedActor != null) {
						Actions action = null;
						if (mState == States.SET_ACTIVATE_TRIGGER) {
							action = Actions.ACTOR_ACTIVATE;
						} else if (mState == States.SET_DEACTIVATE_TRIGGER) {
							action = Actions.ACTOR_DEACTIVATE;
						}

						mInvoker.execute(new CTriggerSet(mSelectedActor, action, (Trigger) hitObject, mLevelEditor));
					}
				}
			}
			// Deselect trigger otherwise
			else {
				if (mSelectedActor != null) {
					Actions action = null;
					if (mState == States.SET_ACTIVATE_TRIGGER) {
						action = Actions.ACTOR_ACTIVATE;
					} else if (mState == States.SET_DEACTIVATE_TRIGGER) {
						action = Actions.ACTOR_DEACTIVATE;
					}

					mInvoker.execute(new CTriggerSet(mSelectedActor, action, null, mLevelEditor));
				}
			}
			break;
		}

		return true;
	}

	@Override
	protected boolean dragged() {
		switch (mState) {
		case ADD:
		case MOVE:
			if (mMovingActor != null) {
				if (mMovingActor.getDef(EnemyActorDef.class).getMovementType() == MovementTypes.PATH) {
					setSnapPosition(false, false);
				} else {
					Vector2 newPosition = getNewMovePosition();
					mMovingActor.setPosition(newPosition);
					Pools.vector2.free(newPosition);
				}

			}
			break;

		case SELECT:
		case REMOVE:
		case SET_ACTIVATE_TRIGGER:
		case SET_DEACTIVATE_TRIGGER:
			// Does nothing
			break;
		}

		return true;
	}

	@Override
	protected boolean up() {
		boolean chained = false;
		switch (mState) {
		case ADD:
			chained = true;
		case MOVE:
			if (mMovingActor != null) {
				// Reset actor to old position
				mMovingActor.setPosition(mActorOrigin);

				if (mSelectedSinceUp) {
					chained = true;
				}

				if (mMovingActor.getDef(EnemyActorDef.class).getMovementType() == MovementTypes.PATH) {
					setSnapPosition(true, chained);
				} else {
					Vector2 newPosition = getNewMovePosition();
					mInvoker.execute(new CResourceMove(mMovingActor, newPosition, mLevelEditor), chained);
					Pools.vector2.free(newPosition);
				}

				mMovingActor = null;
			}

		case REMOVE:
		case SELECT:
		case SET_ACTIVATE_TRIGGER:
		case SET_DEACTIVATE_TRIGGER:
			// Does nothing
			break;
		}

		return true;
	}

	/**
	 * Snaps the selected enemy position to a close path if one exist.
	 * @param useCommand set to true to use a command for this
	 * @param chained set to true if the command shall be chained
	 */
	protected void setSnapPosition(boolean useCommand, boolean chained) {
		Vector2 snappedPosition = getNewMovePosition();
		boolean usesPath = false;

		// Is the position close to a path?
		Path closestPath = getClosestPath(snappedPosition);
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

		if (useCommand) {
			// Only change if not same path as before
			if (closestPath != ((EnemyActor)mSelectedActor).getPath() || closestPath == null || ((EnemyActor)mSelectedActor).getPath() == null) {
				mInvoker.execute(new CResourceMove(mSelectedActor, snappedPosition, mLevelEditor), chained);

				// Changed paths
				if (closestPath != null || ((EnemyActor)mSelectedActor).getPath() != null) {
					mInvoker.execute(new CEnemySetPath((EnemyActor) mSelectedActor, closestPath, mLevelEditor), true);
				}
			}
		} else {
			mSelectedActor.setPosition(snappedPosition);
		}

		Pools.vector2.free(snappedPosition);
	}

	/**
	 * Calculates the closest path to the specified position
	 * @param position the position to search from
	 * @return closest path, null if none exist
	 */
	private Path getClosestPath(Vector2 position) {
		Path closestPath = null;
		float closestDistance = Float.POSITIVE_INFINITY;
		Vector2 diffVector = Pools.vector2.obtain();

		for (Path path : mLevelEditor.getPaths()) {
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

	@Override
	protected QueryCallback getCallback() {
		switch (mState) {
		case ADD:
		case MOVE:
		case SELECT:
		case REMOVE:
			return super.getCallback();


		case SET_ACTIVATE_TRIGGER:
		case SET_DEACTIVATE_TRIGGER:
			return mTriggerCallback;
		}

		return null;
	}

	/** Picking for triggers and enemy actors */
	private QueryCallback mTriggerCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();
			// Hit an trigger
			if (body.getUserData() instanceof Trigger) {
				mHitBodies.clear();
				mHitBodies.add(body);
				return false;
			}
			// Hit an enemy
			else if (body.getUserData() instanceof EnemyActor) {
				mHitBodies.add(body);
			}

			return true;
		}
	};

	/** Current state of the tool */
	protected States mState = States.ADD;
	/** Current enemy group */
	protected EnemyGroup mEnemyGroup = null;
	/** Level editor */
	protected LevelEditor mLevelEditor;
}
