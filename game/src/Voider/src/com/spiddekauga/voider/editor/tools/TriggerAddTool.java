package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TScreenAt;

/**
 * Tool for adding triggers
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerAddTool extends TouchTool {
	/**
	 * @param camera the camera
	 * @param world world where the objects are in
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param levelEditor the editor this tool is bound to
	 */
	public TriggerAddTool(Camera camera, World world, Invoker invoker, ISelection selection, LevelEditor levelEditor) {
		super(camera, world, invoker, selection, levelEditor);
		mLevelEditor = levelEditor;
	}

	@Override
	protected boolean down() {
		testPickPoint();

		// Create TriggerActorActivated if we hit an enemy
		if (mHitEnemy != null) {
			TActorActivated trigger = new TActorActivated(mHitEnemy);
			mInvoker.execute(new CResourceAdd(trigger, mEditor));
			mInvoker.execute(new CSelectionSet(mSelection, trigger), true);
		}
		// Create TriggerScreenAt if we just hit the screen
		else {
			mNewTrigger = new TScreenAt(mLevelEditor.getLevel(), mTouchCurrent.x);
			mInvoker.execute(new CResourceAdd(mNewTrigger, mEditor));
			mInvoker.execute(new CSelectionSet(mSelection, mNewTrigger), true);
		}
		return false;
	}

	@Override
	protected boolean dragged() {
		if (mNewTrigger != null) {
			mNewTrigger.setPosition(mTouchCurrent);
		}
		return false;
	}

	@Override
	protected boolean up() {
		if (mNewTrigger != null) {
			mNewTrigger.setPosition(mTouchCurrent);
			mNewTrigger = null;
		}

		return false;
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	/** Callback for picking triggers and enemies */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof EnemyActor) {
				mHitEnemy = (EnemyActor) userData;
			}
			return false;
		}
	};

	/** This is set if we hit an enemy */
	private EnemyActor mHitEnemy = null;
	/** The new trigger that waas added */
	private TScreenAt mNewTrigger = null;
	/** Level editor */
	private LevelEditor mLevelEditor = null;
}
