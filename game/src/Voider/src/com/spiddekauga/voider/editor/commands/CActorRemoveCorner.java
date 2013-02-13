package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonComplexException;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonCornerTooCloseException;

/**
 * Removes a corner from a terrain actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorRemoveCorner extends Command {
	/**
	 * Removes a corner from the specified terrain actor
	 * @param actor the actor to remove the corner from
	 * @param index of the corner we want to remove
	 */
	public CActorRemoveCorner(ActorDef actor, int index) {
		mActor = actor;
		mIndex = index;
	}

	@Override
	public boolean execute() {
		mCorner = mActor.removeCorner(mIndex);

		return mCorner != null;
	}

	@Override
	public boolean undo() {
		try {
			mActor.addCorner(mCorner, mIndex);
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
