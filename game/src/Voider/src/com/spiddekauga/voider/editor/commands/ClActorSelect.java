package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Level;

/**
 * Selects an actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClActorSelect extends LevelCommand {
	/**
	 * Selects an actor
	 * @param actor the new actor to select
	 * @param chained true if chained
	 */
	public ClActorSelect(Actor actor, boolean chained) {
		super(chained);
		mSelectActor = actor;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level, com.spiddekauga.voider.editor.LevelEditor)
	 */
	@Override
	public boolean execute(Level level, LevelEditor levelEditor) {
		mOldSelectedActor = levelEditor.getSelectedActor();
		levelEditor.setSelectedActor(mSelectActor);
		return false;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level, com.spiddekauga.voider.editor.LevelEditor)
	 */
	@Override
	public boolean undo(Level level, LevelEditor levelEditor) {
		levelEditor.setSelectedActor(mOldSelectedActor);
		return false;
	}

	/** The actor to select on execute */
	Actor mSelectActor;
	/** The previous actor that was selected */
	Actor mOldSelectedActor = null;
}
