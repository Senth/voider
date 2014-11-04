package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.utils.Pool;

/**
 * Tool for moving resources that has a position
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MoveTool extends TouchTool {
	/**
	 * @param editor editor this tool is bound to
	 * @param selection all selected resources
	 */
	public MoveTool(IResourceChangeEditor editor, ISelection selection) {
		super(editor, selection);

		mSelectableResourceTypes.add(IResource.class);
	}

	@Override
	protected boolean down(int button) {
		// Test all
		testPickPoint(mCallbackResource);

		// If we didn't hit anything, test triggers
		if (!mHitResource) {
			testPickAabb(mCallbackTrigger, Config.Editor.PICK_TRIGGER_SIZE);
		}

		if (mHitResource) {
			ArrayList<IResourcePosition> selectedResources = mSelection.getSelectedResourcesOfType(IResourcePosition.class);

			for (IResourcePosition resource : selectedResources) {
				ResourcePositionWrapper resourcePosition = mResourcePositionPool.obtain();
				resourcePosition.resource = resource;
				resourcePosition.originalPos.set(resource.getPosition());
				mMovingResources.add(resourcePosition);
				resource.setIsBeingMoved(true);
			}
			mHitResource = false;

			return true;
		}

		return false;
	}

	@Override
	protected boolean dragged() {
		if (!mMovingResources.isEmpty()) {
			// Special case for enemies - snap to path
			if (mMovingResources.size() == 1 && mSelection.isSelected(EnemyActor.class) && mEditor instanceof LevelEditor) {
				mtNewPosition.set(mTouchCurrent).sub(mTouchOrigin);
				mtNewPosition.add(mMovingResources.get(0).originalPos);
				EnemyAddTool.setSnapPosition((EnemyActor) mMovingResources.get(0).resource, mtNewPosition, (LevelEditor) mEditor, null);
			}
			// Regular move
			else {
				mtDiffPosition.set(mTouchCurrent).sub(mTouchOrigin);

				for (ResourcePositionWrapper movingResource : mMovingResources) {
					mtNewPosition.set(movingResource.originalPos).add(mtDiffPosition);
					movingResource.resource.setPosition(mtNewPosition);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean up(int button) {
		if (!mMovingResources.isEmpty()) {
			// Special case for one enemy - snap to path
			if (mMovingResources.size() == 1 && mSelection.isSelected(EnemyActor.class) && mEditor instanceof LevelEditor) {
				mtNewPosition.set(mTouchCurrent).sub(mTouchOrigin);
				mtNewPosition.add(mMovingResources.get(0).originalPos);
				EnemyAddTool.setSnapPosition((EnemyActor) mMovingResources.get(0).resource, mtNewPosition, (LevelEditor) mEditor, mInvoker);

				mMovingResources.get(0).resource.setIsBeingMoved(false);
			}
			// Regular move
			else {
				mtDiffPosition.set(mTouchCurrent).sub(mTouchOrigin);

				// Reset to original position then move to new position using command
				boolean chained = false;
				for (ResourcePositionWrapper movingResource : mMovingResources) {
					mtNewPosition.set(movingResource.originalPos).add(mtDiffPosition);
					movingResource.resource.setPosition(movingResource.originalPos);
					movingResource.resource.setIsBeingMoved(false);

					mInvoker.execute(new CResourceMove(movingResource.resource, mtNewPosition, mEditor), chained);
					chained = true;
				}
			}

			mResourcePositionPool.freeAll(mMovingResources);
			mMovingResources.clear();

			return true;
		}
		return false;
	}

	/**
	 * Wrapper for moving resources
	 */
	private static class ResourcePositionWrapper {
		/** The resources */
		IResourcePosition resource = null;
		/** The original position */
		Vector2 originalPos = new Vector2();
	}

	/** Query callback for moving actors */
	private QueryCallback mCallbackResource = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();

			if (userData instanceof IResourcePosition) {
				if (mSelection.isSelected((IResource) userData)) {
					mHitResource = true;
					return false;
				}
			}

			return true;
		}
	};

	/** Query callback for picking triggers */
	private QueryCallback mCallbackTrigger = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof Trigger) {
				if (mSelection.isSelected((IResource) userData)) {
					mHitResource = true;
					return false;
				}
			}
			return true;
		}
	};

	/** Temporary new position */
	private Vector2 mtNewPosition = new Vector2();
	/** Temporary diff position */
	private Vector2 mtDiffPosition = new Vector2();
	/** Array of the current resources we're moving */
	private ArrayList<ResourcePositionWrapper> mMovingResources = new ArrayList<MoveTool.ResourcePositionWrapper>();
	/** If we hit a resource with a position */
	private boolean mHitResource = false;
	/** Pool for resource position */
	private Pool<ResourcePositionWrapper> mResourcePositionPool = new Pool<MoveTool.ResourcePositionWrapper>(ResourcePositionWrapper.class, 16, 100);
}
