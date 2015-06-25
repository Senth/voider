package com.spiddekauga.voider.editor.brushes;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.IResourceCorner;

/**
 * A simple resource that draws a line
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class VectorBrush extends Brush implements IResourceCorner, Disposable {
	/**
	 * Creates a vector brush with the specified brush mode
	 * @param brushMode what type of vector brush this is
	 */
	public VectorBrush(VectorBrushModes brushMode) {
		super(brushMode.getColor());
	}

	@Override
	public void addCorners(java.util.List<Vector2> corners) {
		for (Vector2 corner : corners) {
			addCorner(corner);
		}
	}

	@Override
	public void addCorners(Vector2[] corners) {
		for (Vector2 corner : corners) {
			addCorner(corner);
		}
	}

	@Override
	public void addCorner(Vector2 corner) {
		addCorner(corner, mCorners.size());
	}

	@Override
	public void addCorner(Vector2 corner, int index) {
		mCorners.add(index, new Vector2(corner));
	}

	@Override
	public Vector2 removeCorner(int index) {
		return mCorners.remove(index);
	}

	@Override
	public void clearCorners() {
		mCorners.clear();
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
	protected boolean preRender() {
		return mCorners.size() >= 2;
	}

	@Override
	protected void render(ShapeRendererEx shapeRenderer) {
		shapeRenderer.polyline(mCorners, false);
	}

	@Override
	public void dispose() {
		clearCorners();
	}

	@Override
	public void createBodyCorners() {
		// Does nothing
	}

	@Override
	public void destroyBodyCorners() {
		// Does nothing
	}

	/**
	 * Different modes
	 */
	public enum VectorBrushModes {
		/** Add brush color */
		ADD(Config.Editor.BRUSH_ADD_COLOR),
		/** Remove brush color */
		ERASE(Config.Editor.BRUSH_ERASE_COLOR),

		;

		/**
		 * @param color brush color
		 */
		private VectorBrushModes(Color color) {
			mColor = color;
		}

		/**
		 * @return brush color
		 */
		private Color getColor() {
			return mColor;
		}

		private Color mColor;
	}

	private ArrayList<Vector2> mCorners = new ArrayList<>();
}
