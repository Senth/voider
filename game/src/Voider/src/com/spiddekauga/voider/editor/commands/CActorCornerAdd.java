package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.editor.IActorChangeEditor;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Creates a new corner for the specified terrain actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorCornerAdd extends CActorChange {
	/**
	 * Constructs the command where the corner should be added
	 * @param actorDef the actor definition to add the corner to
	 * @param cornerPos the corner position
	 * @param actorEditor editor to send onActorChange(Actor) event to
	 */
	public CActorCornerAdd(ActorDef actorDef, Vector2 cornerPos, IActorChangeEditor actorEditor) {
		super(null, actorEditor);
		mActorDef = actorDef;
		mCornerPos = Pools.obtain(Vector2.class);
		mCornerPos.set(cornerPos);
	}

	@Override
	public boolean execute() {
		try {
			mActorDef.addCorner(mCornerPos);
			mAddedCornerIndex = mActorDef.getCornerCount() - 1;
		} catch (Exception e) {
			return false;
		}

		sendOnChange();

		return true;
	}

	@Override
	public boolean undo() {
		mActorDef.removeCorner(mAddedCornerIndex);

		sendOnChange();

		return true;
	}

	@Override
	public void dispose() {
		Pools.free(mCornerPos);
	}

	/** Terrain actor which we want to add a new corner to*/
	private ActorDef mActorDef;
	/** Initial corner position */
	private Vector2 mCornerPos;
	/** Index of added actor */
	private int mAddedCornerIndex = -1;
}
