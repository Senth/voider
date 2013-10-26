package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.triggers.Trigger;

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
	 * @param editor the editor this tool is bound to
	 */
	public TriggerAddTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor) {
		super(camera, world, invoker, selection, editor);
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

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	/** Callback for picking triggers and enemies */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof Trigger) {
				mHitTrigger = (Trigger) userData;
			} else if (userData instanceof EnemyActor) {
				mHitEnemy = (EnemyActor) userData;
			}
			return false;
		}
	};

	/** This is set if we hit an enemy */
	private EnemyActor mHitEnemy = null;
	/** This is set if we hit a trigger */
	private Trigger mHitTrigger = null;
}
