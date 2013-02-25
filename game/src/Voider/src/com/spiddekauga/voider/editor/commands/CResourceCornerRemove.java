package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.IResourceCorner;
import com.spiddekauga.voider.game.IResourceCorner.PolygonComplexException;
import com.spiddekauga.voider.game.IResourceCorner.PolygonCornerTooCloseException;

/**
 * Removes a corner from a resource
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceCornerRemove extends CResourceChange {
	/**
	 * Removes a corner from the specified resource
	 * @param resourceCorner the resourceCorner to remove the corner from
	 * @param index of the corner we want to remove
	 * @param resourceCornerEditor editor to notify about the change via onActorChange(Actor)
	 */
	public CResourceCornerRemove(IResourceCorner resourceCorner, int index, IResourceChangeEditor resourceCornerEditor) {
		super(null, resourceCornerEditor);
		mResourceCorner = resourceCorner;
		mIndex = index;
	}

	@Override
	public boolean execute() {
		mCorner = mResourceCorner.removeCorner(mIndex);

		sendOnChange();

		return mCorner != null;
	}

	@Override
	public boolean undo() {
		try {
			mResourceCorner.addCorner(mCorner, mIndex);
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
	IResourceCorner mResourceCorner;
	/** The position of the corner we removed */
	Vector2 mCorner = null;
	/** Index of the corner */
	int mIndex;
}
