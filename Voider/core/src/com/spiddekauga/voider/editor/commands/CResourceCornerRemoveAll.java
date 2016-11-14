package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResourceCorner;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes all the corners from the specified resource
 */
public class CResourceCornerRemoveAll extends CResourceChange {
/** The resource definition to remove all the corners from */
private IResourceCorner mResourceCorner;
/** All the corners to restore on undo */
private ArrayList<Vector2> mCorners = new ArrayList<>();

/**
 * Creates a command which removes all the corners from the resource definition
 * @param resourceDef the resource definition to remove all corners from
 * @param resourceEditor editor to notify about the change via onActorChange(Actor)
 */
public CResourceCornerRemoveAll(IResourceCorner resourceDef, IResourceChangeEditor resourceEditor) {
	super(null, resourceEditor);
	mResourceCorner = resourceDef;
	List<Vector2> corners = mResourceCorner.getCorners();
	for (Vector2 corner : corners) {
		mCorners.add(new Vector2(corner));
	}
}

@Override
public boolean execute() {
	mResourceCorner.clearCorners();

	sendOnChange();

	return true;
}

@Override
public boolean undo() {
	try {
		for (Vector2 corner : mCorners) {
			mResourceCorner.addCorner(corner);
		}
	} catch (Exception e) {
		Gdx.app.error("CResourceCornerRemoveAll", "Could not readd all the corners on undo " + e.toString());
		return false;
	}

	sendOnChange();

	return true;
}
}
