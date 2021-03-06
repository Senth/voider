package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.IActorEditor;

/**
 * Calls the reset actor in the actor editor.
 */
public class CActorEditorCenterReset extends Command {
/** The actor editor */
private IActorEditor mActorEditor;
/** Old center position of the actor */
private Vector2 mCenterOld = new Vector2();

/**
 * Resets the actor definitions center offset from an actor editor.
 * @param actorEditor the actor editor to call resetCenterOffset()
 */
public CActorEditorCenterReset(IActorEditor actorEditor) {
	mActorEditor = actorEditor;
	mCenterOld.set(mActorEditor.getCenterOffset());
}

@Override
public boolean execute() {
	mActorEditor.resetCenterOffset();
	return true;
}

@Override
public boolean undo() {
	mActorEditor.setCenterOffset(mCenterOld);
	return true;
}
}
