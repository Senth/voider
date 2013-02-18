package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.editor.IActorChangeEditor;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Executes a move command on the actor.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorMove extends CActorChange {
	/**
	 * Moves the actor to the specified position
	 * @param actor the actor to move
	 * @param newPosition the new position of the actor
	 * @param actorEditor the actor editor to send a onActorChange() event to
	 */
	public CActorMove(Actor actor, Vector2 newPosition, IActorChangeEditor actorEditor) {
		super(actor, actorEditor);
		mActor = actor;
		mDiffMovement = Pools.obtain(Vector2.class);
		mDiffMovement.set(newPosition);
		mDiffMovement.sub(actor.getBody().getPosition());

	}

	@Override
	public boolean execute() {
		Vector2 newPos = Pools.obtain(Vector2.class);
		newPos.set(mActor.getPosition()).add(mDiffMovement);
		mActor.setPosition(newPos);
		Pools.free(newPos);
		sendOnChange();

		return true;
	}

	@Override
	public boolean undo() {
		Vector2 newPos = Pools.obtain(Vector2.class);
		newPos.set(mActor.getPosition()).sub(mDiffMovement);
		mActor.setPosition(newPos);
		Pools.free(newPos);
		sendOnChange();

		return true;
	}

	@Override
	public void dispose() {
		if (mDiffMovement != null) {
			Pools.free(mDiffMovement);
		}
	}

	/** The difference vector for moving the actor back and forth */
	private Vector2 mDiffMovement;
}
