package com.spiddekauga.voider.editor.commands;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Removes all the corners from the specified actor definition
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorCornerRemoveAll extends Command {
	/**
	 * Creates a command which removes all the corners from the actor
	 * definition
	 * @param actorDef the actor definition to remove all corners from
	 */
	public CActorCornerRemoveAll(ActorDef actorDef) {
		mActorDef = actorDef;
		mCorners.addAll(mActorDef.getCorners());
	}

	@Override
	public boolean execute() {
		while (mActorDef.getCornerCount() > 0) {
			mActorDef.removeCorner(0);
		}
		return true;
	}

	@Override
	public boolean undo() {
		try {
			for (Vector2 corner : mCorners) {
				mActorDef.addCorner(corner);
			}
		} catch (Exception e) {
			Gdx.app.error("CActorCornerRemoveAll", "Could not readd all the corners on undo " + e.toString());
			return false;
		}

		return true;
	}

	/** The actor definition to remove all the corners from */
	private ActorDef mActorDef;
	/** All the corners to restore on undo */
	private ArrayList<Vector2> mCorners = new ArrayList<Vector2>();
}
