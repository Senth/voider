package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CCameraMove;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Pan tool
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PanTool extends TouchTool {

	/**
	 * @param camera used for picking
	 * @param world where the objects are
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor editor this tool is bound to
	 */
	public PanTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor) {
		super(camera, world, invoker, selection, editor);

		// Add all resources so that they don't get deselected
		// This tool hijacks all event messages anyway
		mSelectableResourceTypes.add(IResource.class);
	}

	@Override
	protected boolean down(int button) {
		boolean willScroll = false;

		// When active always scroll
		if (mActive) {
			willScroll = true;
		}
		// Only scroll with specific button
		else {
			willScroll = KeyHelper.isScrolling(button);
		}


		if (willScroll) {
			// Already scrolling, create scroll command to stop the scrolling
			if (mScroller.isScrolling()) {
				Vector2 scrollCameraCurrent = Pools.vector2.obtain();
				scrollCameraCurrent.set(mCamera.position.x, mCamera.position.y);
				mInvoker.execute(new CCameraMove(mCamera, scrollCameraCurrent, mScrollCameraOrigin));
				Pools.vector2.free(scrollCameraCurrent);
			}

			mScroller.touchDown((int)mScreenCurrent.x, (int)mScreenCurrent.y);
			mScrollCameraOrigin.set(mCamera.position.x, mCamera.position.y);
			mCreatedScrollCommand = false;

			return true;
		}

		if (mScroller.isScrolling()) {
			return true;
		}

		return false;
	}

	@Override
	protected boolean dragged() {
		if (mScroller.isScrollingByHand()) {
			mScroller.touchDragged((int)mScreenCurrent.x, (int)mScreenCurrent.y);
			return true;
		}

		return false;
	}

	@Override
	protected boolean up(int button) {
		if (mScroller.isScrollingByHand()) {
			if (mActive || KeyHelper.isScrolling(button)) {
				mScroller.touchUp((int)mScreenCurrent.x, (int)mScreenCurrent.y);
				return true;
			}
		}

		return false;
	}

	@Override
	public void activate() {
		super.activate();

		mActive = true;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		mActive = false;
	}

	/**
	 * Updates the pan tool
	 * @param deltaTime time elapsed since last frame
	 */
	public void update(float deltaTime) {
		// Scrolling
		if (mScroller.isScrolling()) {
			mScroller.update(deltaTime);

			Vector2 diffScroll = Pools.vector2.obtain();
			diffScroll.set(mScroller.getOriginScroll()).sub(mScroller.getCurrentScroll());
			float scale = diffScroll.x / Gdx.graphics.getWidth() * getWorldWidth();

			mCamera.position.x = scale + mScrollCameraOrigin.x;
			mCamera.update();

			Pools.vector2.free(diffScroll);
		}
		else if (!mCreatedScrollCommand) {
			Vector2 scrollCameraCurrent = Pools.vector2.obtain();
			scrollCameraCurrent.set(mCamera.position.x, mCamera.position.y);

			mInvoker.execute(new CCameraMove(mCamera, scrollCameraCurrent, mScrollCameraOrigin));

			mCreatedScrollCommand = true;

			Pools.vector2.free(scrollCameraCurrent);
		}
	}

	/**
	 * @return true if currently panning
	 */
	public boolean isPanning() {
		return mScroller.isScrolling();
	}

	/**
	 * Force pan to stop
	 */
	public void stop() {
		mScroller.stop();
	}

	/**
	 * @return world width
	 */
	private float getWorldWidth() {
		return mCamera.viewportWidth;
	}

	/** Created a scroll command */
	private boolean mCreatedScrollCommand = true;
	/** Origin of camera scroll */
	private Vector2 mScrollCameraOrigin = new Vector2();
	/** Logic for scrolling */
	private Scroller mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.X);
	/** If the scroller is the activet tool */
	private boolean mActive = false;
}
