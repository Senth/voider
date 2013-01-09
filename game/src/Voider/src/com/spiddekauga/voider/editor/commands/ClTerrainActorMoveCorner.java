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
 * Executes a move command on a terrain corner
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClTerrainActorMoveCorner extends LevelCommand {
	/**
	 * Moves a corner in the specified terrain actor
	 * @param actor the terrain actor with the corner to move
	 * @param index corner's index to move
	 * @param newPos the new position of the corner
	 */
	public ClTerrainActorMoveCorner(StaticTerrainActor actor, int index, Vector2 newPos) {
		mActor = actor;
		mIndex = index;
		mDiffMovement = Pools.obtain(Vector2.class);
		mDiffMovement.set(newPos);
		mDiffMovement.sub(mActor.getCorner(index));
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean execute(Level level, LevelEditor levelEditor) {
		Vector2 newPos = Pools.obtain(Vector2.class);
		newPos.set(mActor.getCorner(mIndex));
		newPos.add(mDiffMovement);
		boolean moveSuccess = true;
		try {
			mActor.moveCorner(mIndex, newPos);
		} catch (PolygonComplexException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Complex polygon");
		} catch (PolygonCornerTooCloseException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Corner too close");
		}
		Pools.free(newPos);

		return moveSuccess;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean undo(Level level, LevelEditor levelEditor) {
		Vector2 newPos = Pools.obtain(Vector2.class);
		newPos.set(mActor.getCorner(mIndex));
		newPos.sub(mDiffMovement);
		boolean moveSuccess = true;
		try {
			mActor.moveCorner(mIndex, newPos);
		} catch (PolygonComplexException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Complex polygon");
		} catch (PolygonCornerTooCloseException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Corner too close");
		}
		Pools.free(newPos);
		return moveSuccess;
	}

	@Override
	public void dispose() {
		Pools.free(mDiffMovement);
	}

	/** Difference vector for moving the corner back and forth. */
	private Vector2 mDiffMovement;
	/** The actor which corner we want to move */
	private StaticTerrainActor mActor;
	/** The index of the corner to move */
	private int mIndex;
}
