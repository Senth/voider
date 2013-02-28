package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.IResourceCorner;
import com.spiddekauga.voider.game.IResourceCorner.PolygonComplexException;
import com.spiddekauga.voider.game.IResourceCorner.PolygonCornerTooCloseException;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * Executes a move command on a terrain corner
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceCornerMove extends CResourceChange {
	/**
	 * Moves a corner in the specified actor definition
	 * @param resourceCorner the actor definition which corner to move
	 * @param index corner's index to move
	 * @param newPos the new position of the corner
	 * @param actorEditor editor to send onActorChange(Actor) to
	 */
	public CResourceCornerMove(IResourceCorner resourceCorner, int index, Vector2 newPos, IResourceChangeEditor actorEditor) {
		super(null, actorEditor);
		mResourceCorner = resourceCorner;
		mIndex = index;
		mDiffMovement = Vector2Pool.obtain();
		mDiffMovement.set(newPos);
		mDiffMovement.sub(mResourceCorner.getCornerPosition(index));
	}

	@Override
	public boolean execute() {
		Vector2 newPos = Vector2Pool.obtain();
		newPos.set(mResourceCorner.getCornerPosition(mIndex));
		newPos.add(mDiffMovement);
		boolean moveSuccess = true;
		try {
			mResourceCorner.moveCorner(mIndex, newPos);
			sendOnChange();
		} catch (PolygonComplexException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Complex polygon");
		} catch (PolygonCornerTooCloseException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Corner too close");
		}
		Vector2Pool.free(newPos);

		return moveSuccess;
	}

	@Override
	public boolean undo() {
		Vector2 newPos = Vector2Pool.obtain();
		newPos.set(mResourceCorner.getCornerPosition(mIndex));
		newPos.sub(mDiffMovement);
		boolean moveSuccess = true;
		try {
			mResourceCorner.moveCorner(mIndex, newPos);
			sendOnChange();
		} catch (PolygonComplexException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Complex polygon");
		} catch (PolygonCornerTooCloseException e) {
			moveSuccess = false;
			Gdx.app.error("ClTerrainActorMoveCorner", "Corner too close");
		}
		Vector2Pool.free(newPos);
		return moveSuccess;
	}

	@Override
	public void dispose() {
		Vector2Pool.free(mDiffMovement);
	}

	/** Difference vector for moving the corner back and forth. */
	private Vector2 mDiffMovement;
	/** The actor which corner we want to move */
	private IResourceCorner mResourceCorner;
	/** The index of the corner to move */
	private int mIndex;
}
