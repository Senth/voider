package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;
import java.util.Iterator;

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
import com.spiddekauga.voider.editor.commands.CTriggerSet;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.resources.IResourcePosition;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerSetTool extends TouchTool {

	/**
	 * @param camera used for picking on the screen
	 * @param world used for converting screen to world coordinates
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param levelEditor editor bound to this tool
	 * @param action the action to be set together with the trigger
	 */
	public TriggerSetTool(
			Camera camera, World world, Invoker invoker, ISelection selection, LevelEditor levelEditor, Actions action) {
		super(camera, world, invoker, selection, levelEditor);

		mTriggerAction = action;
		mLevelEditor = levelEditor;

		mSelectableResourceTypes.add(EnemyActor.class);
		mSelectableResourceTypes.add(Trigger.class);
	}

	@Override
	protected boolean down() {
		// Enemy selected
		if (mSelection.isSelected(EnemyActor.class)) {
			// Skip if we just selected another enemy
			if (mSelection.isSelectionChangedDuringDown()) {
				return false;
			}


			// Did we press a trigger?
			testPickAabb(mCallbackTrigger, Editor.PICK_TRIGGER_SIZE);

			ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

			// Set trigger
			if (mHitTrigger != null) {
				boolean chained = false;
				for (EnemyActor enemyActor : selectedEnemies) {
					mInvoker.execute(new CTriggerSet(enemyActor, mTriggerAction, mHitTrigger, mEditor), chained);
					chained = true;
				}

				mHitTrigger = null;
			}
			// Didn't hit a trigger -> Either remove trigger or create a new one
			else {
				// Does any enemy have a trigger set, then remove all triggers for those
				Iterator<EnemyActor> enemyIt = selectedEnemies.iterator();
				boolean hasSetTrigger = false;
				boolean chained = false;
				while (enemyIt.hasNext()) {
					EnemyActor enemyActor = enemyIt.next();
					if (TriggerInfo.getTriggerInfoByAction(enemyActor, mTriggerAction) != null) {
						mInvoker.execute(new CTriggerSet(enemyActor, mTriggerAction, null, mEditor), chained);
						chained = true;
						hasSetTrigger = true;
					}
				}


				// No triggers were set previously create a new trigger and set the enemies' trigger
				if (!hasSetTrigger) {
					Trigger createdTrigger = createTrigger();

					chained = false;
					for (EnemyActor enemyActor : selectedEnemies) {
						mInvoker.execute(new CTriggerSet(enemyActor, mTriggerAction, createdTrigger, mEditor), chained);
						chained = true;
					}
				}
			}
		}

		// Trigger selected
		else if (mSelection.isSelected(Trigger.class)) {
			testPickPoint(mCallbackEnemy);

			// Hit enemy -> set the trigger for the enemy
			if (mHitEnemy != null) {
				Trigger selectedTrigger = mSelection.getFirstSelectedResourceOfType(Trigger.class);

				TriggerInfo currentEnemyTriggerInfo = TriggerInfo.getTriggerInfoByAction(mHitEnemy, mTriggerAction);
				if (currentEnemyTriggerInfo == null || currentEnemyTriggerInfo.trigger != selectedTrigger) {
					mInvoker.execute(new CTriggerSet(mHitEnemy, mTriggerAction, selectedTrigger, mEditor));
				}

				mHitEnemy = null;
			} else {
				// Did we hit a trigger?
				testPickAabb(mCallbackTrigger, Editor.PICK_TRIGGER_SIZE);

				// Hit a trigger -> Move it
				if (mHitTrigger != null) {
					if (mHitTrigger instanceof TScreenAt) {
						mMoveTrigger = (IResourcePosition) mHitTrigger;
						mMoveTrigger.setIsBeingMoved(true);
						mDragOrigin.set(mMoveTrigger.getPosition());
					}

					mHitTrigger = null;
				}
				// Create a new trigger
				else {
					createTrigger();
				}
			}
		}

		// Nothing selected
		else {
			// Skip if we just changed selection
			if (mSelection.isSelectionChangedDuringDown()) {
				return false;
			}

			// Since nothing is selected we didn't hit a trigger or enemy, just create a trigger
			createTrigger();
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

	/**
	 * Creates a new trigger at the current location
	 * @return trigger that was created
	 */
	private Trigger createTrigger() {
		TScreenAt screenAt = new TScreenAt(mLevelEditor.getLevel(), mTouchCurrent.x);
		screenAt.setIsBeingMoved(true);
		mMoveTrigger = screenAt;
		mInvoker.execute(new CResourceAdd(mMoveTrigger, mEditor));
		mCreatedTriggerThisEvent = true;
		return screenAt;
	}

	@Override
	public boolean isSelectionToolAllowedToChangeResourceType() {
		return false;
	}

	/** Callback for picking triggers */
	private QueryCallback mCallbackTrigger = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof Trigger) {
				mHitTrigger = (Trigger) userData;
				return false;
			}
			return true;
		}
	};

	/** Callback for picking enemies */
	private QueryCallback mCallbackEnemy = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof EnemyActor) {
				mHitEnemy = (EnemyActor) userData;
				return false;
			}
			return true;
		}
	};

	/** Level editor */
	protected LevelEditor mLevelEditor;
	/** Drag origin */
	private Vector2 mDragOrigin = new Vector2();
	/** Moving trigger */
	private IResourcePosition mMoveTrigger = null;
	/** A trigger we hit during picking */
	private Trigger mHitTrigger = null;
	/** Enemy that was hit */
	private EnemyActor mHitEnemy = null;
	/** What trigger action will be bound between enemy and trigger */
	private Actions mTriggerAction = null;
	/** A new trigger was created this event */
	private boolean mCreatedTriggerThisEvent = false;
}
