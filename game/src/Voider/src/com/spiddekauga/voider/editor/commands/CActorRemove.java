package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Removes an actor from the specified actor editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorRemove extends Command {
	/**
	 * Creates a command which will remove the actor and notify the
	 * actor editor about it.
	 * @param actor the actor to remove
	 * @param editor the editor to remove the actor from
	 */
	public CActorRemove(Actor actor, IActorEditor editor) {
		mActor = actor;
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.onActorRemoved(mActor);
		mActor.destroyBody();
		return true;
	}

	@Override
	public boolean undo() {
		mActor.createBody();
		mEditor.onActorAdded(mActor);
		return true;
	}

	/** The actor to remove */
	private Actor mActor;
	/** The editor to remove the actor from */
	private IActorEditor mEditor;
}
