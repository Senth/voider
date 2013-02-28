package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.IResourceCorner;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * Creates a new corner for the specified terrain resource
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceCornerAdd extends CResourceChange {
	/**
	 * Constructs the command where the corner should be added
	 * @param resourceCorner the resource definition to add the corner to
	 * @param cornerPos the corner position
	 * @param resourceEditor editor to send onActorChange(Actor) event to
	 */
	public CResourceCornerAdd(IResourceCorner resourceCorner, Vector2 cornerPos, IResourceChangeEditor resourceEditor) {
		super(null, resourceEditor);
		mResourceCorner = resourceCorner;
		mCornerPos = Vector2Pool.obtain();
		mCornerPos.set(cornerPos);
	}

	@Override
	public boolean execute() {
		try {
			mResourceCorner.addCorner(mCornerPos);
			mAddedCornerIndex = mResourceCorner.getCornerCount() - 1;
		} catch (Exception e) {
			return false;
		}

		sendOnChange();

		return true;
	}

	@Override
	public boolean undo() {
		mResourceCorner.removeCorner(mAddedCornerIndex);

		sendOnChange();

		return true;
	}

	@Override
	public void dispose() {
		Vector2Pool.free(mCornerPos);
	}

	/** Terrain resource which we want to add a new corner to*/
	private IResourceCorner mResourceCorner;
	/** Initial corner position */
	private Vector2 mCornerPos;
	/** Index of added resource */
	private int mAddedCornerIndex = -1;
}
