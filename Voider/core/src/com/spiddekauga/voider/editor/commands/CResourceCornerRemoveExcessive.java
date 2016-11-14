package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Geometry.PointIndex;

import java.util.List;

/**
 * Removes excessive corners from the resource. I.e. corners that have almost no angle difference
 * between them. E.g. three corners > 0,0 -> 0,5 -> 0,10. In this case 0,5 does not add anything to
 * the shape, it will only make calculations slower... Also removes corners that are too close.
 */
public class CResourceCornerRemoveExcessive extends Command {
private float mCornerDistMinSq;
private float mCornerAngleMin;
private IResourceCorner mResource;
private List<PointIndex> mRemovedCorners = null;
/**
 * Constructor which takes the resource to remove excessive corners from
 * @param resource the resource to remove excessive corners from
 * @param newCornerMinDistSq squared minimum distance between corners
 * @param cornerMinAngle minimum corner angle
 */
public CResourceCornerRemoveExcessive(IResourceCorner resource, float newCornerMinDistSq, float cornerMinAngle) {
	mResource = resource;
	mCornerDistMinSq = newCornerMinDistSq;
	mCornerAngleMin = cornerMinAngle;
}

@Override
public boolean execute() {
	mRemovedCorners = Geometry.removeExcessivePoints(mCornerDistMinSq, mCornerAngleMin, mResource.getCorners());
	return !mRemovedCorners.isEmpty();
}

/**
 * Re-adds removed excessive corners
 * @return always true
 */
@Override
public boolean undo() {
	// Re-add corners, but from back
	for (int i = mRemovedCorners.size() - 1; i >= 0; --i) {
		PointIndex pointIndex = mRemovedCorners.get(i);
		mResource.addCorner(pointIndex.point, pointIndex.index);
	}
	mRemovedCorners = null;

	return true;
}
}
