package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Level;

/**
 * Creates a new actor and adds it to the level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClAddActor extends LevelCommand {
	/**
	 * Creates a new actor for the level
	 * @param actor the new actor
	 */
	public ClAddActor(Actor actor) {
		mActor = actor;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean execute(Level level) {
		level.addActor(mActor);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean undo(Level level) {
		level.removeActor(mActor.getId());
		return true;
	}

	/** The actor to be added/removed from the level */
	Actor mActor;
}
