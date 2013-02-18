package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Selects an actor in the specified select tool
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorSelect extends Command {
	/**
	 * Creates a command that will select an actor in the specified tool
	 * @param actor the actor to select, if null it deselects any actor
	 * @param tool the tool to select the actor in
	 */
	public CActorSelect(Actor actor, IActorSelect tool) {
		mTool = tool;
		mActor = actor;
	}

	@Override
	public boolean execute() {
		if (mTool != null) {
			mOldActor = mTool.getSelectedActor();
			mTool.setSelectedActor(mActor);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean undo() {
		if (mTool != null) {
			mTool.setSelectedActor(mOldActor);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Interface for selecting an actor
	 */
	public interface IActorSelect {
		/**
		 * Selects the selected actor
		 * @actor the actor to select, if null deselects any actor
		 */
		void setSelectedActor(Actor actor);

		/**
		 * @return current selected actor
		 */
		Actor getSelectedActor();
	}

	/** The actor to select */
	public Actor mActor;
	/** Old selected actor */
	public Actor mOldActor = null;
	/** The tool to selelct the actor in */
	public IActorSelect mTool;
}
