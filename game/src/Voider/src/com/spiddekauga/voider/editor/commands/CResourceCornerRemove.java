package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.Pools;

/**
 * Removes a corner from a resource
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
			mCorner = null;
			sendOnChange();
		} catch (IndexOutOfBoundsException e) {
			Gdx.app.error("ClTerrainActorRemoveCorner", "Index out of bounds");
			return false;
		}

		return true;
	}

	@Override
	public void dispose() {
		super.dispose();

		if (mCorner != null) {
			Pools.vector2.free(mCorner);
		}
	}

	/** Actor to remove/add the corner from/to */
	IResourceCorner mResourceCorner;
	/** The position of the corner we removed */
	Vector2 mCorner = null;
	/** Index of the corner */
	int mIndex;
}
