package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;

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
	 * @param editor editor bound to this tool
	 * @param action the action to be set together with the trigger
	 */
	public TriggerSetTool(
			Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor, Actions action) {
		super(camera, world, invoker, selection, editor);

		mTriggerAction = action;
	}

	@Override
	protected boolean down() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean dragged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean up() {
		// TODO Auto-generated method stub
		return false;
	}

	/** A trigger we hit during picking */
	Trigger mHitTrigger = null;
	/** What trigger action will be bound between enemy and trigger */
	Actions mTriggerAction = null;
}
