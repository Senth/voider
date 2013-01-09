package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.actors.StaticTerrainActor.PolygonComplexException;

/**
 * Creates a new corner for the specified terrain actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClTerrainActorAddCorner extends LevelCommand {
	/**
	 * Constructs the command where the corner should be added
	 * @param actor terrain actor which we want to add a new corner to
	 * @param cornerPos the corner position
	 */
	public ClTerrainActorAddCorner(StaticTerrainActor actor, Vector2 cornerPos) {
		mActor = actor;
		mCornerPos = Pools.obtain(Vector2.class);
		mCornerPos.set(cornerPos);
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean execute(Level level, LevelEditor levelEditor) {
		try {
			mActor.addCorner(mCornerPos);
			mAddedCornerIndex = mActor.getLastAddedCornerIndex();
		} catch (PolygonComplexException e) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean undo(Level level, LevelEditor levelEditor) {
		mActor.removeCorner(mAddedCornerIndex);
		return true;
	}

	@Override
	public void dispose() {
		Pools.free(mCornerPos);
	}

	/** Terrain actor which we want to add a new corner to*/
	private StaticTerrainActor mActor;
	/** Initial corner position */
	private Vector2 mCornerPos;
	/** Index of added actor */
	private int mAddedCornerIndex = -1;
}
