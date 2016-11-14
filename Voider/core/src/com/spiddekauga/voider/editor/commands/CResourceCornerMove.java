package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResourceCorner;

/**
 * Executes a move command on a terrain corner
 */
public class CResourceCornerMove extends CResourceChange {
/** Difference vector for moving the corner back and forth. */
private Vector2 mDiffMovement = new Vector2();
/** The actor which corner we want to move */
private IResourceCorner mResourceCorner;
/** The index of the corner to move */
private int mIndex;


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
	mDiffMovement.set(newPos);
	mDiffMovement.sub(mResourceCorner.getCornerPosition(index));
}

@Override
public boolean execute() {
	Vector2 newPos = new Vector2(mResourceCorner.getCornerPosition(mIndex));
	newPos.add(mDiffMovement);
	mResourceCorner.moveCorner(mIndex, newPos);
	sendOnChange();
	return true;
}

@Override
public boolean undo() {
	Vector2 newPos = new Vector2(mResourceCorner.getCornerPosition(mIndex));
	newPos.sub(mDiffMovement);
	mResourceCorner.moveCorner(mIndex, newPos);
	sendOnChange();
	return true;
}
}
