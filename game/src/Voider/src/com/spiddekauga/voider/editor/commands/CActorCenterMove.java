package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Moves the center offset to the specified position. This command also takes an optional
 * actor which will be moved in the opposite direction; this will cause it to look as it
 * wasn't moved at all.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CActorCenterMove extends CResourceChange {
	/**
	 * Creates a command that will move the center of the actor definition. It will also
	 * update the actors position. Note that the actors position must be its position
	 * where it was at oldCenter.
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
		mActorDef.getVisual().setCenterOffset(mCenterNew);

		if (mActor != null) {
			Vector2 newActorPos = new Vector2(mCenterOld).sub(mCenterNew);
			newActorPos.add(mActor.getPosition());
			mActor.destroyBody();
			mActor.setPosition(newActorPos);
			mActor.createBody();
		}

		sendOnChange();

		return true;
	}

	@Override
	public boolean undo() {
		mActorDef.getVisual().setCenterOffset(mCenterOld);

		if (mActor != null) {
			Vector2 newActorPos = new Vector2(mCenterNew).sub(mCenterOld);
			newActorPos.add(mActor.getPosition());
			mActor.destroyBody();
			mActor.setPosition(newActorPos);
			mActor.createBody();
		}

		sendOnChange();

		return true;
	}

	/** Actor which uses the actor def */
	private Actor mActor;
	/** Actor definition which center to move */
	private ActorDef mActorDef;
	/** New center position offset to use at execute */
	private Vector2 mCenterNew = new Vector2();
	/** Old center position to restore to at undo */
	private Vector2 mCenterOld = new Vector2();
}
