package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResourceCorner;

/**
 * Creates a new corner for the specified resource
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CResourceCornerAdd extends CResourceChange {
	/**
	 * Constructs the command where the corner should be added. Will insert the corner at
	 * the back of the corners.
	 * @param resourceCorner the resource definition to add the corner to
	 * @param cornerPos the corner position
	 * @param resourceEditor editor to send
	 *        IResourceChangeEditor.#onResourceChanged(IResource) event to
	 */
	public CResourceCornerAdd(IResourceCorner resourceCorner, Vector2 cornerPos, IResourceChangeEditor resourceEditor) {
		this(resourceCorner, cornerPos, resourceCorner.getCornerCount(), resourceEditor);
	}

	/**
	 * Constructs the command where the corner should be added. Will created the corner at
	 * the specified index position
	 * @param resourceCorner the resource definition to add the corner to
	 * @param cornerPos the corner position
	 * @param index the index to insert the corner to
	 * @param resourceEditor editor to send
	 *        IResourceChangeEditor.onResourceChanged(IResource) event to
	 */
	public CResourceCornerAdd(IResourceCorner resourceCorner, Vector2 cornerPos, int index, IResourceChangeEditor resourceEditor) {
		super(null, resourceEditor);
		mResourceCorner = resourceCorner;
		mCornerPos.set(cornerPos);
		mAddedCornerIndex = index;
	}


	@Override
	public boolean execute() {
		try {
			mResourceCorner.addCorner(mCornerPos, mAddedCornerIndex);
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


	/** Terrain resource which we want to add a new corner to */
	private IResourceCorner mResourceCorner;
	/** Initial corner position */
	private Vector2 mCornerPos = new Vector2();
	/** Index of added resource */
	private int mAddedCornerIndex = -1;
}
