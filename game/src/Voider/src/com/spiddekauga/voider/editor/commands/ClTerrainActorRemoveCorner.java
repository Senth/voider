package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.actors.StaticTerrainActor.PolygonComplexException;
import com.spiddekauga.voider.game.actors.StaticTerrainActor.PolygonCornerTooCloseException;

/**
 * Removes a corner from a terrain actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClTerrainActorRemoveCorner extends LevelCommand {
	/**
	 * Removes a corner from the specified terrain actor
	 * @param actor the actor to remove the corner from
	 * @param corner the position of the corner to remove
	 */
	public ClTerrainActorRemoveCorner(StaticTerrainActor actor, Vector2 corner) {
		mActor = actor;
		mCorner = Pools.obtain(Vector2.class);
		mCorner.set(corner);
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level, com.spiddekauga.voider.editor.LevelEditor)
	 */
	@Override
	public boolean execute(Level level, LevelEditor levelEditor) {
		mIndex = mActor.getCornerIndex(mCorner);
		if (mIndex == -1) {
			return false;
		}

		mActor.removeCorner(mIndex);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level, com.spiddekauga.voider.editor.LevelEditor)
	 */
	@Override
	public boolean undo(Level level, LevelEditor levelEditor) {
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

	@Override
	public void dispose() {
		Pools.free(mCorner);
	}

	/** Actor to remove/add the corner from/to */
	StaticTerrainActor mActor;
	/** The position of the corner to remove */
	Vector2 mCorner;
	/** Index of the corner */
	int mIndex = -1;
}
