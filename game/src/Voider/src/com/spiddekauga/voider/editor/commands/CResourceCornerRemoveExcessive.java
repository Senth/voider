package com.spiddekauga.voider.editor.commands;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.Pools;

/**
 * Removes excessive corners from the resource. I.e. corners that have almost
 * no angle difference between them. E.g. three corners > 0,0 -> 0,5 -> 0,10.
 * In this case 0,5 does not add anything to the shape, it will only make
 * calculations slower... Also removes corners that are too close.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceCornerRemoveExcessive extends Command {
	/**
	 * Constructor which takes the resource to remove excessive corners from
	 * @param resource the resource to remove excessive corners from
	 */
	public CResourceCornerRemoveExcessive(IResourceCorner resource) {
		mResource = resource;
		mRemovedCorners.clear();
	}

	@Override
	public boolean execute() {
		if (mResource.getCornerCount() < 2) {
			return false;
		}

		Vector2 afterVector = Pools.vector2.obtain();
		float beforeAngle = 0;
		float afterAngle = 0;
		ArrayList<Vector2> corners = mResource.getCorners();
		afterVector.set(corners.get(1)).sub(corners.get(0));
		afterAngle = afterVector.angle();
		for (int i = 1; i < corners.size() - 1; ++i) {
			beforeAngle = afterAngle;

			boolean removeCorner = false;

			// Test distance
			if (afterVector.len2() < Config.Editor.Actor.Visual.DRAW_NEW_CORNER_MIN_DIST_SQ) {
				removeCorner = true;
			}
			// Test angle
			else {
				// Calculate after vector
				afterVector.set(corners.get(Collections.nextIndex(corners, i))).sub(corners.get(i));
				afterAngle = afterVector.angle();

				if (Maths.approxCompare(beforeAngle, afterAngle, Config.Editor.Actor.Visual.DRAW_CORNER_ANGLE_MIN)) {
					removeCorner = true;
				} else if (beforeAngle < afterAngle) {
					if (Maths.approxCompare(beforeAngle + 360, afterAngle, Config.Editor.Actor.Visual.DRAW_CORNER_ANGLE_MIN)) {
						removeCorner = true;
					}
				} else {
					if (Maths.approxCompare(beforeAngle - 360, afterAngle, Config.Editor.Actor.Visual.DRAW_CORNER_ANGLE_MIN)) {
						removeCorner = true;
					}
				}
			}

			// Too low difference in degrees between angles...
			if (removeCorner) {
				removeCorner(i);

				// Before vector will have changed again
				if (i < corners.size() - 1) {
					afterVector.set(corners.get(i)).sub(corners.get(i-1));
					afterAngle = afterVector.angle();

					--i;
				}
			}
		}

		// Test length between last corners, i.e. end - 1 -> end & end -> begin
		// end - 1 -> end.
		if (corners.size() > 1) {
			afterVector.set(corners.get(corners.size() - 1)).sub(corners.get(corners.size() - 2));
			if (afterVector.len2() < Config.Editor.Actor.Visual.DRAW_NEW_CORNER_MIN_DIST_SQ) {
				removeCorner(corners.size()-1);
			}
		}

		// end -> begin
		if (corners.size() > 1) {
			afterVector.set(corners.get(corners.size() - 1)).sub(corners.get(0));
			if (afterVector.len2() < Config.Editor.Actor.Visual.DRAW_NEW_CORNER_MIN_DIST_SQ) {
				removeCorner(corners.size()-1);
			}
		}

		Pools.vector2.free(afterVector);


		return true;
	}

	/**
	 * Saves and removes the specified corner
	 * @param index index of the corner to remove
	 */
	private void removeCorner(int index) {
		Vector2 removedCorner = mResource.removeCorner(index);

		if (removedCorner != null) {
			CornerIndex cornerIndex = mCornerIndexPool.obtain();
			cornerIndex.corner = removedCorner;
			cornerIndex.index = index;
			mRemovedCorners.add(cornerIndex);
		} else {
			Gdx.app.error("CResourceCornerRemoveExcessive", "Could not remove the corner");
		}
	}

	/**
	 * Re-adds removed excessive corners
	 * @return always true
	 */
	@Override
	public boolean undo() {
		// Re-add corners, but from back
		for (int i = mRemovedCorners.size() - 1; i >= 0; --i) {
			mResource.addCorner(mRemovedCorners.get(i).corner, mRemovedCorners.get(i).index);

			mCornerIndexPool.free(mRemovedCorners.get(i));
		}

		mRemovedCorners.clear();

		return true;
	}

	@Override
	public void dispose() {
		if (mRemovedCorners != null) {
			for (CornerIndex cornerIndex : mRemovedCorners) {
				Pools.vector2.free(cornerIndex.corner);
				mCornerIndexPool.free(cornerIndex);
			}
			Pools.arrayList.free(mRemovedCorners);
			mRemovedCorners = null;
		}
	}

	/**
	 * Wrapper class for a removed corner with its index
	 */
	private static class CornerIndex {
		/** corner position */
		Vector2 corner = null;
		/** corner index */
		int index = -1;
	}

	/** Resource to remove excessive corners from */
	private IResourceCorner mResource;
	/** Removed corners */
	@SuppressWarnings("unchecked")
	private ArrayList<CornerIndex> mRemovedCorners = Pools.arrayList.obtain();
	/** Pool for corner index */
	private Pool<CornerIndex> mCornerIndexPool = com.badlogic.gdx.utils.Pools.get(CornerIndex.class);
}
