package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Creates a new actor and adds it to the level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClActorAdd extends LevelCommand {
	/**
	 * Creates a new actor for the level
	 * @param actor the new actor
	 */
	public ClActorAdd(Actor actor) {
		mActor = actor;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean execute(Level level, LevelEditor levelEditor) {
		level.addActor(mActor);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean undo(Level level, LevelEditor levelEditor) {
		level.removeActor(mActor.getId());
		return true;
	}

	/** The actor to be added/removed from the level */
	Actor mActor;
}
