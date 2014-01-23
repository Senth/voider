package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.utils.Pool;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for moving resources that has a position
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MoveTool extends TouchTool {
	/**
	 * @param camera used for picking
	 * @param world where the objects are
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor editor this tool is bound to
	 */
	public MoveTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor) {
		super(camera, world, invoker, selection, editor);

		mSelectableResourceTypes.add(IResource.class);
	}

	@Override
	protected boolean down(int button) {
		testPickPoint(mCallback);

		if (mHitResource) {
			ArrayList<IResourcePosition> selectedResources = mSelection.getSelectedResourcesOfType(IResourcePosition.class);

			for (IResourcePosition resource : selectedResources) {
				ResourcePositionWrapper resourcePosition = mResourcePositionPool.obtain();
				resourcePosition.resource = resource;
				resourcePosition.originalPos.set(resource.getPosition());
				mMovingResources.add(resourcePosition);
				resource.setIsBeingMoved(true);
			}

			Pools.arrayList.free(selectedResources);
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
				Vector2 newPosition = Pools.vector2.obtain();
				newPosition.set(mTouchCurrent).sub(mTouchOrigin);
				newPosition.add(mMovingResources.get(0).originalPos);
				EnemyAddTool.setSnapPosition((EnemyActor)mMovingResources.get(0).resource, newPosition, (LevelEditor)mEditor, null);

				Pools.vector2.free(newPosition);
			}
			// Regular move
			else {
				Vector2 diffMovement = Pools.vector2.obtain();
				diffMovement.set(mTouchCurrent).sub(mTouchOrigin);
				Vector2 newPosition = Pools.vector2.obtain();

				for (ResourcePositionWrapper movingResource : mMovingResources) {
					newPosition.set(movingResource.originalPos).add(diffMovement);
					movingResource.resource.setPosition(newPosition);
				}

				Pools.vector2.freeAll(diffMovement, newPosition);
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
				Vector2 newPosition = Pools.vector2.obtain();
				newPosition.set(mTouchCurrent).sub(mTouchOrigin);
				newPosition.add(mMovingResources.get(0).originalPos);
				EnemyAddTool.setSnapPosition((EnemyActor)mMovingResources.get(0).resource, newPosition, (LevelEditor)mEditor, mInvoker);

				mMovingResources.get(0).resource.setIsBeingMoved(false);

				Pools.vector2.free(newPosition);
			}
			// Regular move
			else {
				Vector2 diffMovement = Pools.vector2.obtain();
				diffMovement.set(mTouchCurrent).sub(mTouchOrigin);
				Vector2 newPosition = Pools.vector2.obtain();

				// Reset to original position then move to new position using command
				boolean chained = false;
				for (ResourcePositionWrapper movingResource : mMovingResources) {
					newPosition.set(movingResource.originalPos).add(diffMovement);
					movingResource.resource.setPosition(movingResource.originalPos);
					movingResource.resource.setIsBeingMoved(false);

					mInvoker.execute(new CResourceMove(movingResource.resource, newPosition, mEditor), chained);
					chained = true;
				}

				Pools.vector2.freeAll(diffMovement, newPosition);
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
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();

			if (userData instanceof IResourcePosition) {
				mHitResource = true;
			}

			return true;
		}
	};

	/** Array of the current resources we're moving */
	private ArrayList<ResourcePositionWrapper> mMovingResources = new ArrayList<MoveTool.ResourcePositionWrapper>();
	/** If we hit a resource with a position */
	private boolean mHitResource = false;
	/** Pool for resource position */
	private Pool<ResourcePositionWrapper> mResourcePositionPool = new Pool<MoveTool.ResourcePositionWrapper>(ResourcePositionWrapper.class, 16, 100);
}
