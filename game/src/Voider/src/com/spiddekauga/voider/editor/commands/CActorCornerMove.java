package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonComplexException;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonCornerTooCloseException;

/**
 * Executes a move command on a terrain corner
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorCornerMove extends Command {
	/**
	 * Moves a corner in the specified actor definition
	 * @param actorDef the actor definition which corner to move
	 * @param index corner's index to move
	 * @param newPos the new position of the corner
	 */
	public CActorCornerMove(ActorDef actorDef, int index, Vector2 newPos) {
		mActorDef = actorDef;
		mIndex = index;
		mDiffMovement = Pools.obtain(Vector2.class);
		mDiffMovement.set(newPos);
		mDiffMovement.sub(mActorDef.getCornerPosition(index));
	}

	@Override
	public boolean execute() {
		Vector2 newPos = Pools.obtain(Vector2.class);
		newPos.set(mActorDef.getCornerPosition(mIndex));
		newPos.add(mDiffMovement);
		boolean moveSuccess = true;
		try {
			mActorDef.moveCorner(mIndex, newPos);
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
	public boolean undo() {
		Vector2 newPos = Pools.obtain(Vector2.class);
		newPos.set(mActorDef.getCornerPosition(mIndex));
		newPos.sub(mDiffMovement);
		boolean moveSuccess = true;
		try {
			mActorDef.moveCorner(mIndex, newPos);
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
	private ActorDef mActorDef;
	/** The index of the corner to move */
	private int mIndex;
}
