package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.utils.Pools;

/**
 * Executes a move command on the resource.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceMove extends CResourceChange {
	/**
	 * Moves the resource to the specified position
	 * @param resource the resource to move
	 * @param newPosition the new position of the resource
	 * @param resourceEditor the resource editor to send a onIResourceChange() event to
	 */
	public CResourceMove(IResourcePosition resource, Vector2 newPosition, IResourceChangeEditor resourceEditor) {
		super(resource, resourceEditor);
		mDiffMovement = Pools.vector2.obtain();
		mDiffMovement.set(newPosition);
		mDiffMovement.sub(resource.getPosition());

	}

	@Override
	public boolean execute() {
		// Skip if we don't move...
		if (mDiffMovement.x == 0 && mDiffMovement.y == 0) {
			return false;
		}

		Vector2 newPos = Pools.vector2.obtain();
		newPos.set(((IResourcePosition) mResource).getPosition()).add(mDiffMovement);
		((IResourcePosition) mResource).setPosition(newPos);
		Pools.vector2.free(newPos);
		sendOnChange();

		return true;
	}

	@Override
	public boolean undo() {
		Vector2 newPos = Pools.vector2.obtain();
		newPos.set(((IResourcePosition) mResource).getPosition()).sub(mDiffMovement);
		((IResourcePosition) mResource).setPosition(newPos);
		Pools.vector2.free(newPos);
		sendOnChange();

		return true;
	}

	@Override
	public void dispose() {
		if (mDiffMovement != null) {
			Pools.vector2.free(mDiffMovement);
		}
	}

	/** The difference vector for moving the resource back and forth */
	private Vector2 mDiffMovement;
}
