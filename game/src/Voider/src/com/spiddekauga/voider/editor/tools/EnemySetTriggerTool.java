package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CTriggerSet;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for setting enemy activate or deactivate trigger
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemySetTriggerTool extends TouchTool {
	/**
	 * @param camera used for picking on the screen
	 * @param world used for converting screen to world coordinates
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor editor bound to this tool
	 * @param action the action to be set together with the trigger
	 */
	public EnemySetTriggerTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor, Actions action) {
		super(camera, world, invoker, selection, editor);

		mTriggerAction = action;
	}

	@Override
	protected boolean down() {
		if (mSelection.isSelectionChangedDuringDown()) {
			return false;
		}

		testPickAabb(mCallback, Editor.PICK_TRIGGER_SIZE);

		// Set the trigger (both if we hit or missed a trigger)
		ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

		for (EnemyActor enemy : selectedEnemies) {
			mInvoker.execute(new CTriggerSet(enemy, mTriggerAction, mHitTrigger, mEditor));
		}

		mHitTrigger = null;

		Pools.arrayList.free(selectedEnemies);

		return false;
	}

	@Override
	protected boolean dragged() {
		// Does nothing
		return false;
	}

	@Override
	protected boolean up() {
		// Does nothing
		return false;
	}

	/** Callback for picking triggers */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof Trigger) {
				mHitTrigger = (Trigger) userData;
			}
			return false;
		}
	};

	/** Hit trigger */
	Trigger mHitTrigger = null;
	/** Trigger action to be used together with the trigger */
	Actions mTriggerAction = null;
}
