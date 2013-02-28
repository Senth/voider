package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * Calls the reset actor in the actor editor.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorEditorCenterReset extends Command {
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

	@Override
	public void dispose() {
		Vector2Pool.free(mCenterOld);
		mCenterOld = null;
	}

	/** The actor editor */
	private IActorEditor mActorEditor;
	/** Old center position of the actor */
	private Vector2 mCenterOld = Vector2Pool.obtain();
}
