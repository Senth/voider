package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Fixes the custom fixes on undo. Because the actors doesn't update the fixtures
 * themselves another action needs to do that after it has been changed. During execution
 * DrawActorTool will handle the call to the method, on an undo() however, there only
 * commands will be called, thus this command is needed <strong>before</strong>
 * a call to add, move, or remove a corner is made.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorDefFixCustomFixturesOnUndo extends Command {
	/**
	 * Takes the actor definition to be updated on undo.
	 * @param actorDef the actor definition which {@link com.spiddekauga.voider.game.actors.ActorDef.#fixCustomShapeFixtures()}
	 * will be called.
	 */
	@SuppressWarnings("javadoc")
	public CActorDefFixCustomFixturesOnUndo(ActorDef actorDef) {
		mActorDef = actorDef;
	}

	@Override
	public boolean execute() {
		// Does nothing
		return true;
	}

	@Override
	public boolean undo() {
		mActorDef.fixCustomShapeFixtures();
		return true;
	}

	/** The actor def to fix the custom shape fixtures on undo */
	private ActorDef mActorDef;
}
