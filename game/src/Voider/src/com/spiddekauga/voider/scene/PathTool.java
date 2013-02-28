package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CEnemySetPath;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerMove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveAll;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CResourceSelect;
import com.spiddekauga.voider.game.IResourceCorner.PolygonComplexException;
import com.spiddekauga.voider.game.IResourceCorner.PolygonCornerTooCloseException;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Vector2Pool;

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

		Path oldSelected = mSelectedPath;
		mSelectedPath = (Path) selectedResource;

		for (ISelectListener selectListener : mSelectListeners) {
			selectListener.onResourceSelected(oldSelected, mSelectedPath);
		}

		mChangedSelectedSinceUp = true;

		activate();
	}

	@Override
	public IResource getSelectedResource() {
		return mSelectedPath;
	}

	@Override
	public void activate() {
		if (mSelectedPath != null) {
			mSelectedPath.setSelected(true);

			switch (mState) {
			case ADD_CORNER:
			case REMOVE:
				mSelectedPath.createBodyCorners();
				break;

			case MOVE:
			case SELECT:
				// Does nothing;
				break;
			}
		}
	}

	@Override
	public void deactivate() {
		if (mSelectedPath != null) {
			mSelectedPath.setSelected(false);

			switch (mState) {
			case ADD_CORNER:
			case REMOVE:
				mSelectedPath.destroyBodyCorners();
				break;

			case MOVE:
			case SELECT:
				// Does nothing
			}
		}
	}

	@Override
	public void clear() {
		deactivate();

		mSelectedPath = null;
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
		REMOVE,
		/** Selects a path */
		SELECT
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
				mDragOrigin.set(((Path)mHitBody.getUserData()).getPosition());
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
				boolean removePath = false;

				if (mHitBody.getUserData() == mSelectedPath) {
					if (!mChangedSelectedSinceUp) {
						removePath = true;
					}
				}
				// Hit a corner -> Delete it
				else if (mHitBody.getUserData() instanceof HitWrapper) {
					mCornerIndexCurrent = mSelectedPath.getCornerIndex(mHitBody.getPosition());
					mInvoker.execute(new CResourceCornerRemove(mSelectedPath, mCornerIndexCurrent, mLevelEditor));

					// Was it the last corner? Remove path too then
					if (mSelectedPath.getCornerCount() == 0) {
						removePath = true;
					}
				}
				// Hit another path
				else if (mHitBody.getUserData() instanceof Path) {
					mInvoker.execute(new CResourceSelect((IResource) mHitBody.getUserData(), this));
				}

				if (removePath) {
					mInvoker.execute(new CResourceRemove(mSelectedPath, mLevelEditor));

					// Remove path from all enemies bound to this path
					ArrayList<EnemyActor> enemyCopy = new ArrayList<EnemyActor>(mSelectedPath.getEnemies());
					for (EnemyActor enemy : enemyCopy) {
						mInvoker.execute(new CEnemySetPath(enemy, null, mLevelEditor), true);
					}

					mInvoker.execute(new CResourceCornerRemoveAll(mSelectedPath, mLevelEditor), true);
					mInvoker.execute(new CResourceSelect(null, this), true);
				}
			}
			break;


		case SELECT:
			testPickPath();

			if (mHitBody != null && mHitBody.getUserData() instanceof Path) {
				if (mHitBody.getUserData() != mSelectedPath) {
					mInvoker.execute(new CResourceSelect((Path)mHitBody.getUserData(), this));
				}
			} else {
				if (mSelectedPath != null) {
					mInvoker.execute(new CResourceSelect(null, this));
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
					Vector2 newCornerPos = Vector2Pool.obtain();
					newCornerPos.set(mTouchCurrent);
					mSelectedPath.moveCorner(mCornerIndexCurrent, newCornerPos);
					Vector2Pool.free(newCornerPos);
				} catch (Exception e) {
					// Does nothing
				}
			}
			break;


		case MOVE:
			if (mSelectedPath != null) {
				Vector2 newPosition = getNewMovePosition();
				mSelectedPath.setPosition(newPosition);
				Vector2Pool.free(newPosition);
			}
			break;


		case REMOVE:
		case SELECT:
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
					Vector2 newPos = Vector2Pool.obtain();

					newPos.set(mSelectedPath.getCornerPosition(mCornerIndexCurrent));
					try {
						mSelectedPath.moveCorner(mCornerIndexCurrent, mDragOrigin);
						mInvoker.execute(new CResourceCornerMove(mSelectedPath, mCornerIndexCurrent, newPos, mLevelEditor));
					} catch (Exception e) {
						// Does nothing
					}

					Vector2Pool.free(newPos);
				}
			}

			mCornerIndexLast = mCornerIndexCurrent;
			mCornerIndexCurrent = -1;
			mCornerAddedNow = false;
			break;


		case MOVE:
			// Set the new position of the actor
			if (mSelectedPath != null) {
				// Reset path to original position
				mSelectedPath.setPosition(mDragOrigin);

				Vector2 newPos = getNewMovePosition();
				mInvoker.execute(new CResourceMove(mSelectedPath, newPos, mLevelEditor), mChangedSelectedSinceUp);
				Vector2Pool.free(newPos);
			}
			break;


		case REMOVE:
		case SELECT:
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
		Vector2 localPos = Vector2Pool.obtain();

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

		Vector2Pool.free(localPos);
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
	 * @return new position to move the path to. Don't forget to free this position
	 * using Pools.free(newPos).
	 */
	private Vector2 getNewMovePosition() {
		Vector2 newPosition = Vector2Pool.obtain();
		newPosition.set(mTouchCurrent).sub(mTouchOrigin);
		newPosition.add(mDragOrigin);

		return newPosition;
	}

	/**
	 * Test pick for the level
	 */
	private void testPickPath() {
		testPick();

		if (mHitBody == null) {
			mOnlyFindPath = true;
			testPick(Editor.PICK_PATH_SIZE);
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
