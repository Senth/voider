package com.spiddekauga.voider.editor.brushes;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.utils.Pools;

/**
 * A simple resource that draws a line
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class VectorBrush extends Resource implements IResourceCorner, IResourceEditorRender, Disposable {
	/**
	 * Creates a vector brush with the specified brush mode
	 * @param addMode set to true if the brush shall be in add mode,
	 * false if it shall be in erase mode. Add mode = green line, otherwise
	 * the line is dark purple.
	 */
	public VectorBrush(boolean addMode) {
		mUniqueId = UUID.randomUUID();
		mAddMode = addMode;
	}

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.BRUSH;
	}

	@Override
	public void addCorner(Vector2 corner) {
		addCorner(corner, mCorners.size());
	}

	@Override
	public void addCorner(Vector2 corner, int index) {
		mCorners.add(index, Pools.vector2.obtain().set(corner));
	}

	@Override
	public Vector2 removeCorner(int index) {
		return mCorners.remove(index);
	}

	@Override
	public void moveCorner(int index, Vector2 newPos) {
		mCorners.get(index).set(newPos);
	}

	@Override
	public int getCornerCount() {
		return mCorners.size();
	}

	@Override
	public Vector2 getCornerPosition(int index) {
		return mCorners.get(index);
	}

	@Override
	public ArrayList<Vector2> getCorners() {
		return mCorners;
	}

	@Override
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mCorners.size(); ++i) {
			if (mCorners.get(i).equals(position)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		if (mCorners.size() >= 2) {
			shapeRenderer.push(ShapeType.Line);

			if (mAddMode) {
				shapeRenderer.setColor(Config.Editor.BRUSH_ADD_COLOR);
			} else {
				shapeRenderer.setColor(Config.Editor.BRUSH_ERASE_COLOR);
			}

			shapeRenderer.polyline(mCorners, false);

			shapeRenderer.pop();
		}
	}

	@Override
	public void dispose() {
		Pools.vector2.freeAll(mCorners);
		Pools.arrayList.free(mCorners);
		mCorners = null;
	}

	/**
	 * Sets the brush mode, will affect the brush color
	 * @param addMode set to true if the brush is in add mode, false
	 * if the brush is in erase mode.
	 */
	public void setBrushMode(boolean addMode) {
		mAddMode = addMode;
	}

	@Override
	public void createBodyCorners() {
		// Does nothing
	}

	@Override
	public void destroyBodyCorners() {
		// Does nothing
	}

	/** All corners */
	@SuppressWarnings("unchecked")
	private ArrayList<Vector2> mCorners = Pools.arrayList.obtain();
	/** If the brush shall add or erase */
	private boolean mAddMode = true;
}
