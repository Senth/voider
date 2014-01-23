package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
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
		testPickAabb(mCallback, Editor.PICK_TRIGGER_SIZE);

		// Hit a trigger, move it
		if (mMoveTrigger != null) {
			mDragOrigin.set(mMoveTrigger.getPosition());
		}
		// Create TriggerScreenAt if we just hit the screen
		else {
			mMoveTrigger = new TScreenAt(mLevelEditor.getLevel(), mTouchCurrent.x);
			mInvoker.execute(new CResourceAdd(mMoveTrigger, mEditor));
			mInvoker.execute(new CSelectionSet(mSelection, mMoveTrigger), true);
			mCreatedTriggerThisEvent = true;
		}

		if (mMoveTrigger != null) {
			mMoveTrigger.setIsBeingMoved(true);
		}

		return false;
	}

	@Override
	protected boolean dragged() {
		if (mMoveTrigger != null) {
			mMoveTrigger.setPosition(mTouchCurrent);
		}
		return false;
	}

	@Override
	protected boolean up() {
		if (mMoveTrigger != null) {
			if (mCreatedTriggerThisEvent) {
				mMoveTrigger.setPosition(mTouchCurrent);
			} else {
				mMoveTrigger.setPosition(mDragOrigin);
				mInvoker.execute(new CResourceMove(mMoveTrigger, mTouchCurrent, mEditor));
			}

			mMoveTrigger.setIsBeingMoved(false);

			mMoveTrigger = null;
			mCreatedTriggerThisEvent = false;
		}

		return false;
	}

	/** Callback for picking triggers */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof TScreenAt) {
				mMoveTrigger = (TScreenAt) userData;
			}
			return false;
		}
	};

	/** Drag origin */
	private Vector2 mDragOrigin = new Vector2();
	/** True if we created the trigger this event */
	private boolean mCreatedTriggerThisEvent = false;
	/** The new trigger that was added */
	private TScreenAt mMoveTrigger = null;
	/** Level editor */
	private LevelEditor mLevelEditor = null;
}
