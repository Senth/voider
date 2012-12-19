package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Level;

/**
 * Executes a move command on the actor.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClMoveActor extends LevelCommand {
	/**
	 * Moves the actor to the specified position
	 * @param actor the actor to move
	 * @param newPosition the new position of the actor
	 */
	public ClMoveActor(Actor actor, Vector2 newPosition) {
		mDiffMovement = Pools.obtain(Vector2.class);
		mDiffMovement.set(newPosition);
		mDiffMovement.sub(actor.getBody().getPosition());
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public void execute(Level level) {

	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public void undo(Level level) {
		// TODO Auto-generated method stub

	}

	/** The difference vector for moving the actor back and forth */
	private Vector2 mDiffMovement = null;
}
