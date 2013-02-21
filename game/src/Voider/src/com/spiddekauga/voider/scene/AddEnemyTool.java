package com.spiddekauga.voider.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CActorMove;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.EnemyGroup;

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
		super(camera, world, EnemyActor.class, invoker, true, editor);
		mLevelEditor = editor;
	}


	@Override
	public void setSelectedActor(Actor selectedActor) {
		super.setSelectedActor(selectedActor);

		if (selectedActor instanceof EnemyActor) {
			mEnemyGroup = ((EnemyActor) selectedActor).getEnemyGroup();
		}
	}

	@Override
	protected void dragged() {
		switch (mState) {
		case ADD:
		case MOVE:
			if (mMovingActor != null) {
				if (mMovingActor.getDef(EnemyActorDef.class).getMovementType() == MovementTypes.PATH) {
					/** @todo snap to nearby path if one exist */
				} else {
					Vector2 newPosition = getNewMovePosition();
					mMovingActor.setPosition(newPosition);
					Pools.free(newPosition);
				}
			}
			break;

		case SELECT:
		case REMOVE:
			// Does nothing
			break;
		}
	}

	@Override
	protected void up() {
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
					/** @todo snap to nearby path if one exist */
				} else {
					Vector2 newPosition = getNewMovePosition();
					mInvoker.execute(new CActorMove(mMovingActor, newPosition, mEditor), chained);
					Pools.free(newPosition);
				}
				mMovingActor = null;
			}

		default:
			break;
		}
	}

	/** Current enemy group */
	protected EnemyGroup mEnemyGroup = null;
	/** Level editor */
	protected LevelEditor mLevelEditor;
}
