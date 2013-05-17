package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;

/**
 * Fixes the custom fixes on either execute or undo. The actors doesn't update the fixtures
 * themselves another action needs to do that after it has been changed.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CActorDefFixCustomFixtures extends Command {
	/**
	 * Takes the actor definition to be updated on undo.
	 * @param actorDef the actor definition which {@link com.spiddekauga.voider.game.actors.ActorDef.#fixCustomShapeFixtures()}
	 * will be called.
	 * @param onExecute set to true to call {@link com.spiddekauga.voider.game.actors.ActorDef.#fixCustomShapeFixtures()}
	 * on #execute(), false to call on #undo()
	 */
	@SuppressWarnings("javadoc")
	public CActorDefFixCustomFixtures(ActorDef actorDef, boolean onExecute) {
		mActorDef = actorDef;
		mOnExecute = onExecute;
	}

	@Override
	public boolean execute() {
		if (mOnExecute) {
			mActorDef.fixCustomShapeFixtures();
		}
		return true;
	}

	@Override
	public boolean undo() {
		if (!mOnExecute) {
			try {
				mActorDef.fixCustomShapeFixtures();
			} catch (PolygonComplexException e) {
				Gdx.app.error("CActorDefFixCustomFixtures", "PolygonComplexException should never happen here!");
			} catch (PolygonCornersTooCloseException e) {
				Gdx.app.error("CActorDefFixCustomFixtures", "PolygonCornersTooCloseException should never happen here!");
			}
		}
		return true;
	}

	/** The actor def to fix the custom shape fixtures */
	private ActorDef mActorDef;
	/** True if fix custom shape fixtures on #execute(), false on #undo() instead */
	private boolean mOnExecute;
}
