package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Moves the center offset to the specified position. This command
 * also takes an optional actor which will be moved in the opposite
 * direction; this will cause it to look as it wasn't moved at all.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorCenterMove extends Command {
	/**
	 * Creates a command that will move the center of the actor definition.
	 * It will also update the actors position. Note that the actors position
	 * must be its position where it was at oldCenter.
	 * @param actorDef the actor definition which center to move
	 * @param newCenter the new center position
	 * @param oldCenter old center position, used for restoring using undo
	 * @param actor optional actor to update the position, set to null to skip
	 */
	public CActorCenterMove(ActorDef actorDef, Vector2 newCenter, Vector2 oldCenter, Actor actor) {
		mActorDef = actorDef;
		mCenterNew.set(newCenter);
		mCenterOld.set(oldCenter);
		mActor = actor;
	}

	/**
	 * Creates a command that will move the center of the actor definition
	 * @param actorDef the actor definition which center to move
	 * @param newCenter the new center position
	 * @param oldCenter old center position, used for restoring using undo
	 */
	public CActorCenterMove(ActorDef actorDef, Vector2 newCenter, Vector2 oldCenter) {
		this(actorDef, newCenter, oldCenter, null);
	}


	@Override
	public boolean execute() {
		mActorDef.setCenterOffset(mCenterNew);

		if (mActor != null) {
			Vector2 newActorPos = Pools.obtain(Vector2.class);
			newActorPos.set(mCenterOld).sub(mCenterNew);
			newActorPos.add(mActor.getPosition());
			mActor.destroyBody();
			mActor.setPosition(newActorPos);
			mActor.createBody();
			Pools.free(newActorPos);
		}

		return true;
	}

	@Override
	public boolean undo() {
		mActorDef.setCenterOffset(mCenterOld);

		if (mActor != null) {
			Vector2 newActorPos = Pools.obtain(Vector2.class);
			newActorPos.set(mCenterNew).sub(mCenterOld);
			newActorPos.add(mActor.getPosition());
			mActor.destroyBody();
			mActor.setPosition(newActorPos);
			mActor.createBody();
			Pools.free(newActorPos);
		}

		return true;
	}

	@Override
	public void dispose() {
		Pools.free(mCenterNew);
		Pools.free(mCenterOld);
		mCenterNew = null;
		mCenterOld = null;
	}

	/** Actor definition which center to move */
	private ActorDef mActorDef;
	/** Actor to move in the oppositie direction if not null */
	private Actor mActor;
	/** New center position offset to use at execute */
	private Vector2 mCenterNew = Pools.obtain(Vector2.class);
	/** Old center position to restore to at undo */
	private Vector2 mCenterOld = Pools.obtain(Vector2.class);
}
