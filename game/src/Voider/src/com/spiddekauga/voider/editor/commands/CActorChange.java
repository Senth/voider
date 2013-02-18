package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IActorChangeEditor;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Wrapper class for all commands that changes an actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class CActorChange extends Command {
	/**
	 * Sets the actor and the actor editor to send the change command to
	 * @param actor the actor that will be changed
	 * @param actorEditor editor which will receive the #onActorChange(Actor) command.
	 */
	public CActorChange(Actor actor, IActorChangeEditor actorEditor) {
		mActor = actor;
		mActorEditor = actorEditor;
	}

	/**
	 * Sends an onActorChange(Actor) command to the actor editor
	 */
	protected void sendOnChange() {
		mActorEditor.onActorChanged(mActor);
	}

	/** The actor that will be changed */
	protected Actor mActor;
	/** Actor editor to notify of the change */
	private IActorChangeEditor mActorEditor;
}
