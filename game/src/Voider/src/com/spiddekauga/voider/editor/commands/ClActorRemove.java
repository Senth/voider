package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Level;

/**
 * Removes an actor from the level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClActorRemove extends LevelCommand {
	/**
	 * Removes an actor from the level, undo readds the actor
	 * @param actor the actor to remove
	 */
	public ClActorRemove(Actor actor) {
		this(actor, false);
	}

	/**
	 * Removes an actor from the level, this has the ability to chain the
	 * command
	 * @param actor the actor to remove
	 * @param chained if the command shall be chained
	 */
	public ClActorRemove(Actor actor, boolean chained) {
		super(chained);
		mActor = actor;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level, com.spiddekauga.voider.editor.LevelEditor)
	 */
	@Override
	public boolean execute(Level level, LevelEditor levelEditor) {
		level.removeActor(mActor.getId());
		return true;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level, com.spiddekauga.voider.editor.LevelEditor)
	 */
	@Override
	public boolean undo(Level level, LevelEditor levelEditor) {
		level.addActor(mActor);
		return true;
	}

	/** Actor to remove/add */
	Actor mActor;
}
