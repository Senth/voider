package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IActorChangeEditor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonComplexException;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonCornerTooCloseException;

/**
 * Removes a corner from a terrain actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorCornerRemove extends CActorChange {
	/**
	 * Removes a corner from the specified terrain actor
	 * @param actor the actor to remove the corner from
	 * @param index of the corner we want to remove
	 * @param actorEditor editor to notify about the change via onActorChange(Actor)
	 */
	public CActorCornerRemove(ActorDef actor, int index, IActorChangeEditor actorEditor) {
		super(null, actorEditor);
		mActor = actor;
		mIndex = index;
	}

	@Override
	public boolean execute() {
		mCorner = mActor.removeCorner(mIndex);

		sendOnChange();

		return mCorner != null;
	}

	@Override
	public boolean undo() {
		try {
			mActor.addCorner(mCorner, mIndex);
			sendOnChange();
		} catch (PolygonComplexException e) {
			Gdx.app.error("ClTerrainActorRemoveCorner", "Complax polygon");
			return false;
		} catch (PolygonCornerTooCloseException e) {
			Gdx.app.error("ClTerrainActorRemoveCorner", "Corner too close");
			return false;
		} catch (IndexOutOfBoundsException e) {
			Gdx.app.error("ClTerrainActorRemoveCorner", "Index out of bounds");
			return false;
		}

		return true;
	}

	/** Actor to remove/add the corner from/to */
	ActorDef mActor;
	/** The position of the corner we removed */
	Vector2 mCorner = null;
	/** Index of the corner */
	int mIndex;
}
