package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Adds a new actor and calls the actor editor about the notification
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorAdd extends Command {
	/**
	 * Creates a command which will add and actor and notify the actor editor
	 * about it.
	 * @param actor the actor to add
	 * @param editor the editor to add the actor to
	 */
	public CActorAdd(Actor actor, IActorEditor editor) {
		mActor = actor;
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mActor.createBody();
		mEditor.onActorAdded(mActor);
		return true;
	}

	@Override
	public boolean undo() {
		mEditor.onActorRemoved(mActor);
		mActor.destroyBody();
		return true;
	}

	/** The actor to add */
	private Actor mActor;
	/** The editor to add the actor to */
	private IActorEditor mEditor;
}
