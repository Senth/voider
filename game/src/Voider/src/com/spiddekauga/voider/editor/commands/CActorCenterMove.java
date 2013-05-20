package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.utils.Pools;

/**
 * Moves the center offset to the specified position. This command
 * also takes an optional actor which will be moved in the opposite
 * direction; this will cause it to look as it wasn't moved at all.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorCenterMove extends CResourceChange {
	/**
	 * Creates a command that will move the center of the actor definition.
	 * It will also update the actors position. Note that the actors position
	 * must be its position where it was at oldCenter.
	 * @param actorDef the actor definition which center to move
	 * @param newCenter the new center position
	 * @param oldCenter old center position, used for restoring using undo
	 * @param actorEditor the actor editor to send a onActorChange() event to
	 * @param actor optional actor to update the position, set to null to skip
	 */
	public CActorCenterMove(ActorDef actorDef, Vector2 newCenter, Vector2 oldCenter, IResourceChangeEditor actorEditor, Actor actor) {
		super(actor, actorEditor);
		mActor = actor;
		mActorDef = actorDef;
		mCenterNew.set(newCenter);
		mCenterOld.set(oldCenter);
	}

	/**
	 * Creates a command that will move the center of the actor definition

	 * @param actorDef the actor definition which center to move
	 * @param newCenter the new center position
	 * @param oldCenter old center position, used for restoring using undo
	 * @param actorEditor the actor editor to send a onActorChange() event to
	 */
	public CActorCenterMove(ActorDef actorDef, Vector2 newCenter, Vector2 oldCenter, IResourceChangeEditor actorEditor) {
		this(actorDef, newCenter, oldCenter, actorEditor, null);
	}


	@Override
	public boolean execute() {
		mActorDef.getVisualVars().setCenterOffset(mCenterNew);

		if (mActor != null) {
			Vector2 newActorPos = Pools.vector2.obtain();
			newActorPos.set(mCenterOld).sub(mCenterNew);
			newActorPos.add(mActor.getPosition());
			mActor.destroyBody();
			mActor.setPosition(newActorPos);
			mActor.createBody();
			Pools.vector2.free(newActorPos);
		}

		sendOnChange();

		return true;
	}

	@Override
	public boolean undo() {
		mActorDef.getVisualVars().setCenterOffset(mCenterOld);

		if (mActor != null) {
			Vector2 newActorPos = Pools.vector2.obtain();
			newActorPos.set(mCenterNew).sub(mCenterOld);
			newActorPos.add(mActor.getPosition());
			mActor.destroyBody();
			mActor.setPosition(newActorPos);
			mActor.createBody();
			Pools.vector2.free(newActorPos);
		}

		sendOnChange();

		return true;
	}

	@Override
	public void dispose() {
		Pools.vector2.free(mCenterNew);
		Pools.vector2.free(mCenterOld);
		mCenterNew = null;
		mCenterOld = null;
	}

	/** Actor which uses the actor def */
	private Actor mActor;
	/** Actor definition which center to move */
	private ActorDef mActorDef;
	/** New center position offset to use at execute */
	private Vector2 mCenterNew = Pools.vector2.obtain();
	/** Old center position to restore to at undo */
	private Vector2 mCenterOld = Pools.vector2.obtain();
}
