package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerMove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveAll;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CResourceSelect;
import com.spiddekauga.voider.game.IResourceCorner.PolygonComplexException;
import com.spiddekauga.voider.game.IResourceCorner.PolygonCornerTooCloseException;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.resources.IResource;

/**
 * Creates, destroys paths
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PathTool extends TouchTool implements ISelectTool {
	/**
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param invoker used ofr undo/redo
	 * @param levelEditor will be called when paths are added/removed
	 */
	public PathTool(Camera camera, World world, Invoker invoker, LevelEditor levelEditor) {
		super(camera, world);

		mLevelEditor = levelEditor;
		mInvoker = invoker;
	}

	@Override
	public void addListener(ISelectListener listener) {
		mSelectListeners.add(listener);
	}

	@Override
	public void addListeners(List<ISelectListener> listeners) {
		mSelectListeners.addAll(listeners);
	}

	@Override
	public void removeListener(ISelectListener listener) {
		mSelectListeners.remove(listener);
	}

	@Override
	public void removeListeners(List<ISelectListener> listeners) {
		mSelectListeners.removeAll(listeners);
	}

	@Override
	public void setSelectedResource(IResource selectedResource) {
		deactivate();

		for (ISelectListener selectListener : mSelectListeners) {
			selectListener.onResourceSelect(mSelectedPath, selectedResource);
		}

		mChangedSelectedSinceUp = true;
		mSelectedPath = (Path) selectedResource;

		activate();
	}

	@Override
	public IResource getSelectedResource() {
		return mSelectedPath;
	}

	@Override
	public void activate() {
		if (mSelectedPath != null) {
			switch (mState) {
			case ADD_CORNER:
			case REMOVE:
				mSelectedPath.setSelected(true);
				break;

			case MOVE:
				// Does nothing;
				break;
			}
		}
	}

	@Override
	public void deactivate() {
		if (mSelectedPath != null) {
			mSelectedPath.setSelected(false);
		}
	}

	/**
	 * Sets the state of the path tool
	 * @param state new state of the path tool
	 */
	public void setState(States state) {
		deactivate();

		mState = state;

		activate();
	}

	/**
	 * @return current state of the tool
	 */
	public States getState() {
		return mState;
	}

	/**
	 * All states of the path tool
	 */
	public enum States {
		/** Adds a new corner, or move existing one */
		ADD_CORNER,
		/** Moves the entire path */
		MOVE,
		/** Removes a corner or the entire path */
		REMOVE
	}

	@Override
	protected void down() {
		switch (mState) {
		case ADD_CORNER:
			// Double click inside current path finishes/closes it
			if (mDoubleClick && hitSelectedPath()) {
				// Remove the last corner if we accidentally added one when double clicking
				if (mCornerIndexLast != -1) {
					mInvoker.undo(false);
				}

				mInvoker.execute(new CResourceSelect(null, this));
				return;
			}


			// Test if we hit a path or corner
			testPickPath();


			if (mHitBody != null) {
				// Hit the path -> create corner
				if (hitSelectedPath()) {
					if (!mChangedSelectedSinceUp) {
						createTempCorner();
					}
				}
				// Hit another path -> Select it
				else if (mHitBody.getUserData() instanceof Path) {
					mInvoker.execute(new CResourceSelect((IResource) mHitBody.getUserData(), this));
				}
				// Hit a corner -> Move it
				else {
					mCornerIndexCurrent = mSelectedPath.getCornerIndex(mHitBody.getPosition());
					mDragOrigin.set(mHitBody.getPosition());
					mCornerAddedNow = false;
				}
			}
			// Else just create a new corner
			else {
				// No path selected -> Create new path
				if (mSelectedPath == null) {
					Path path = new Path();
					path.setWorld(mWorld);
					mInvoker.execute(new CResourceAdd(path, mLevelEditor));
					mInvoker.execute(new CResourceSelect(path, this), true);
				}

				createTempCorner();
			}
			break;


		case MOVE:
			testPickPath();

			// If hit path
			if (mHitBody != null && mHitBody.getUserData() instanceof Path) {
				// Select the actor
				if (mSelectedPath != mHitBody.getUserData()) {
					mInvoker.execute(new CResourceSelect((IResource)mHitBody.getUserData(), this));
				}
				mDragOrigin.set(mHitBody.getPosition());
			} else {
				if (mSelectedPath != null) {
					mInvoker.execute(new CResourceSelect(null, this));
				}
			}
			break;


		case REMOVE:
			testPickPath();

			// If we hit the path (no corners) when it was selected -> delete it
			if (mHitBody != null) {
				if (mHitBody.getUserData() == mSelectedPath) {
					if (!mChangedSelectedSinceUp) {
						mInvoker.execute(new CResourceRemove(mSelectedPath, mLevelEditor));
						mInvoker.execute(new CResourceCornerRemoveAll(mSelectedPath, mLevelEditor), true);
						mInvoker.execute(new CResourceSelect(null, this), true);
					}
				}
				// Hit a corner -> Delete it
				else {
					mCornerIndexCurrent = mSelectedPath.getCornerIndex(mHitBody.getPosition());
					mInvoker.execute(new CResourceCornerRemove(mSelectedPath, mCornerIndexCurrent, mLevelEditor));

					// Was it the last corner? Remove path too then
					if (mSelectedPath.getCornerCount() == 0) {
						mInvoker.execute(new CResourceRemove(mSelectedPath, mLevelEditor), true);
						mInvoker.execute(new CResourceSelect(null, this));
					}
				}
			}
			break;
		}
	}

	@Override
	protected void dragged() {
		switch (mState) {
		case ADD_CORNER:
			if (mCornerIndexCurrent != -1) {
				try {
					Vector2 newCornerPos = Pools.obtain(Vector2.class);
					newCornerPos.set(mTouchCurrent);
					mSelectedPath.moveCorner(mCornerIndexCurrent, newCornerPos);
					Pools.free(newCornerPos);
				} catch (Exception e) {
					// Does nothing
				}
			}
			break;


		case MOVE:
			if (mSelectedPath != null) {
				// TODO
				//				Vector2 newPosition = getNewMovePosition();
				//				mSelectedPath.setPosition(newPosition);
				//				Pools.free(newPosition);
			}
			break;


		case REMOVE:
			// Does nothing
			break;
		}
	}

	@Override
	protected void up() {
		switch (mState) {
		case ADD_CORNER:
			if (mSelectedPath != null && mCornerIndexCurrent != -1) {
				// New corner
				if (mCornerAddedNow) {
					createCornerFromTemp();
				}
				// Move corner
				else {
					// Reset to original position
					Vector2 newPos = Pools.obtain(Vector2.class);

					newPos.set(mSelectedPath.getCornerPosition(mCornerIndexCurrent));
					try {
						mSelectedPath.moveCorner(mCornerIndexCurrent, mDragOrigin);
						mInvoker.execute(new CResourceCornerMove(mSelectedPath, mCornerIndexCurrent, newPos, mLevelEditor));
					} catch (Exception e) {
						// Does nothing
					}

					Pools.free(newPos);
				}
			}

			mCornerIndexLast = mCornerIndexCurrent;
			mCornerIndexCurrent = -1;
			mCornerAddedNow = false;
			break;


		case MOVE:
			// TODO
			//			// Set the new position of the actor
			//			if (mSelectedPath != null) {
			//				// Reset actor to original position
			//				mSelectedPath.setPosition(mDragOrigin);
			//
			//				Vector2 newPos = getNewMovePosition();
			//				mInvoker.execute(new CResourceMove(mSelectedPath, newPos, mLevelEditor), mChangedSelectedSinceUp);
			//				Pools.free(newPos);
			//			}
			break;


		case REMOVE:
			// Does nothing
			break;
		}

		mChangedSelectedSinceUp = false;
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		if (!hitBodies.isEmpty()) {
			return hitBodies.get(0);
		} else {
			return null;
		}
	}

	/**
	 * @return true if we hit the selected path
	 */
	private boolean hitSelectedPath() {
		return mHitBody != null && mHitBody.getUserData() == mSelectedPath;
	}

	/**
	 * Creates a temporary corner for the path
	 */
	private void createTempCorner() {
		Vector2 localPos = Pools.obtain(Vector2.class);

		localPos.set(mTouchOrigin);

		try {
			mSelectedPath.addCorner(localPos);
			mCornerIndexCurrent = mSelectedPath.getCornerCount() - 1;
			mDragOrigin.set(mTouchOrigin);
			mCornerAddedNow = true;
		} catch (PolygonComplexException e) {
			/** @TODO print some error message on screen, cannot add corner here */
		} catch (PolygonCornerTooCloseException e) {
			/** @TODO print error message on screen */
		}

		Pools.free(localPos);
	}

	/**
	 * Creates a corner command from the temporary corner
	 */
	private void createCornerFromTemp() {
		// Get the position of the corner then remove it
		Vector2 cornerPos = mSelectedPath.getCornerPosition(mCornerIndexCurrent);
		mSelectedPath.removeCorner(mCornerIndexCurrent);

		// Set the command as chained if no corner exist in the actor
		boolean chained = mSelectedPath.getCornerCount() == 0;

		mInvoker.execute(new CResourceCornerAdd(mSelectedPath, cornerPos, mLevelEditor), chained);
	}

	/**
	 * Test pick for the level
	 */
	private void testPickPath() {
		testPick();

		if (mHitBody == null) {
			mOnlyFindPath = true;
			testPick(Config.PICK_PATH_SIZE);
			mOnlyFindPath = false;
		}
	}


	/** Picking for paths */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();
			// Hit a corner
			if (body.getUserData() instanceof HitWrapper) {
				if (!mOnlyFindPath) {
					HitWrapper hitWrapper = (HitWrapper) body.getUserData();
					if (hitWrapper.resource != null && hitWrapper.resource instanceof Path) {
						mHitBodies.clear();
						mHitBodies.add(fixture.getBody());
						return false;
					}
				}
			}
			// Hit an actor
			else if (body.getUserData() != null && body.getUserData() instanceof Path) {
				mHitBodies.add(body);
			}

			return true;
		}
	};

	/** Current state of the tool */
	private States mState = States.ADD_CORNER;
	/** Currently selected path */
	private Path mSelectedPath = null;
	/** Invoker for undo/redo */
	private Invoker mInvoker;
	/** Level editor */
	private LevelEditor mLevelEditor;
	/** Last added corner */
	private int mCornerIndexLast = -1;
	/** Current corner index */
	private int mCornerIndexCurrent = -1;
	/** Changed Path since up */
	private boolean mChangedSelectedSinceUp = false;
	/** Drag origin of the body */
	private Vector2 mDragOrigin = new Vector2();
	/** True if the corner was added on last down */
	private boolean mCornerAddedNow = false;
	/** If we're testing to pick for the path (and skip corners) */
	private boolean mOnlyFindPath = false;

	/** Listeners for selected resource */
	private ArrayList<ISelectListener> mSelectListeners = new ArrayList<ISelectListener>();
}
