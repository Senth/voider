package com.spiddekauga.utils;

import java.util.ArrayList;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * 
/** Renders points, lines, rectangles, filled rectangles and boxes.</p>
 * 
 * This class works with OpenGL ES 1.x and 2.0. In its base configuration a 2D orthographic projection with the origin in the
 * lower left corner is used. Units are given in screen pixels.</p>
 * 
 * To change the projection properties use the {@link #setProjectionMatrix(Matrix4)} method. Usually the {@link Camera#combined}
 * matrix is set via this method. If the screen orientation or resolution changes, the projection matrix might have to be adapted
 * as well.</p>
 * 
 * Shapes are rendered in batches to increase performance. The standard use-pattern looks as follows:
 * 
 * <pre>
 * {@code
 * camera.update();
 * shapeRenderer.setProjectionMatrix(camera.combined);
 * 
 * shapeRenderer.push(ShapeType.Line);
 * shapeRenderer.color(1, 1, 0, 1);
 * shapeRenderer.line(x, y, x2, y2);
 * shapeRenderer.rect(x, y, width, height);
 * shapeRenderer.circle(x, y, radius);
 * shapeRenderer.pop();
 * 
 * shapeRenderer.push(ShapeType.Filled);
 * shapeRenderer.color(0, 1, 0, 1);
 * shapeRenderer.rect(x, y, width, height);
 * shapeRenderer.circle(x, y, radius);
 * shapeRenderer.pop();
 * }
 * </pre>
 * 
 * The class has a second matrix called the transformation matrix which is used to rotate, scale and translate shapes in a more
 * flexible manner. This mechanism works much like matrix operations in OpenGL ES 1.x. The following example shows how to rotate a
 * rectangle around its center using the z-axis as the rotation axis and placing it's center at (20, 12, 2):
 * 
 * <pre>
 * shapeRenderer.push(ShapeType.Line);
 * shapeRenderer.identity();
 * shapeRenderer.translate(20, 12, 2);
 * shapeRenderer.rotate(0, 0, 1, 90);
 * shapeRenderer.rect(-width / 2, -height / 2, width, height);
 * shapeRenderer.pop();
 * </pre>
 * 
 * Matrix operations all use postmultiplication and work just like glTranslate, glScale and glRotate. The last transformation
 * specified will be the first that is applied to a shape (rotate then translate in the above example).
 * 
 * The projection and transformation matrices are a state of the ShapeRenderer, just like the color and will be applied to all
 * shapes until they are changed.
 * 
 * @author mzechner, stbachmann
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 * A more advanced shape renderer which allows custom shaders and lines
 * with multicolors.
 */
public class ShapeRendererEx implements Disposable {
	/**
	 * Sets the shader for the shape renderer
	 * @param shaderProgram shader to be used for rendering shapes
	 */
	public void setShader(ShaderProgram shaderProgram) {
		if (mRenderer instanceof ImmediateModeRenderer20) {
			((ImmediateModeRenderer20)mRenderer).setShader(shaderProgram);
		}
	}

	/**
	 * Easier method for drawing a triangle if one has the vertices in Vector2 format.
	 * @param v1 vertex number one
	 * @param v2 vertex number two
	 * @param v3 vertex number three
	 */
	public void triangle(Vector2 v1, Vector2 v2, Vector2 v3) {
		triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
	}

	/**
	 * Easier method for drawing a triangle if one has the vertices in Vector2 format.
	 * @param triangle an array with at least 3 vertices, will only draw the three first
	 * vertices.
	 */
	public void triangle(ArrayList<Vector2> triangle) {
		triangle(triangle, 0);
	}

	/**
	 * Easier method for drawing a triangle if one has the vertices in Vector2 format.
	 * @param triangle an array with at least 3 vertices, will only draw the three first
	 * vertices.
	 * @param indexOffset the index offset, the triangle will be drawn from this offset
	 */
	public void triangle(ArrayList<Vector2> triangle, int indexOffset) {
		int index0 = indexOffset;
		int index1 = indexOffset + 1;
		int index2 = indexOffset + 2;

		if (triangle.size() >= indexOffset + 3) {
			triangle(triangle.get(index0).x, triangle.get(index0).y,
					triangle.get(index1).x, triangle.get(index1).y,
					triangle.get(index2).x, triangle.get(index2).y);
		}
	}

	/**
	 * Easier method for drawing a triangle if one has the vertices in Vector2 format.
	 * @param triangle an array with at least 3 vertices, will only draw the three first
	 * vertices.
	 */
	public void triangle(Vector2[] triangle) {
		triangle(triangle, 0);
	}

	/**
	 * Easier method for drawing a triangle if one has the vertices in Vector2 format.
	 * @param triangle an array with at least 3 vertices, will only draw the three first
	 * vertices.
	 * @param indexOffset the index offset, the triangle will be drawn from this offset
	 */
	public void triangle(Vector2[] triangle, int indexOffset) {
		int index0 = indexOffset;
		int index1 = indexOffset + 1;
		int index2 = indexOffset + 2;

		if (triangle.length >= indexOffset + 3) {
			triangle(triangle[index0].x, triangle[index0].y,
					triangle[index1].x, triangle[index1].y,
					triangle[index2].x, triangle[index2].y);
		}
	}

	/**
	 * Draws all triangles in the array
	 * @param triangles an array with triangles
	 */
	public void triangles(ArrayList<Vector2> triangles) {
		if (triangles.size() % 3 != 0) {
			throw new IllegalArgumentException("triangles must have a pair of 3 vertices.");
		}

		for (int i = 0; i < triangles.size(); i += 3) {
			triangle(triangles, i);
		}
	}

	/**
	 * Draws all triangles in the array with an position offset
	 * @param triangles an array with triangles to draw
	 * @param positionOffset position offset of the triangles
	 */
	public void triangles(final ArrayList<Vector2> triangles, final Vector2 positionOffset) {
		if (triangles == null || triangles.size() % 3 != 0) {
			throw new IllegalArgumentException("triangles must have a pair of 3 vertices.");
		}

		Vector2[] localVertices = new Vector2[3];
		for (int i = 0; i < localVertices.length; ++i) {
			localVertices[i] = Pools.vector2.obtain();
		}

		for (int triangleIndex = 0; triangleIndex < triangles.size() - 2; triangleIndex += 3) {
			for (int localIndex = 0; localIndex < localVertices.length; ++localIndex) {
				localVertices[localIndex].set(triangles.get(triangleIndex + localIndex)).add(positionOffset);
			}
			triangle(localVertices);
		}

		for (Vector2 vertex : localVertices) {
			Pools.vector2.free(vertex);
		}
	}

	/**
	 * Draws a polyline in the x/y plane. The vertices must contain at least 2 points (4 floats x,y). The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param vertices all lines
	 * @param loop set to true if end and beginning shall be connected
	 */
	public void polyline(ArrayList<Vector2> vertices, boolean loop) {
		Vector2 noOffset = Pools.vector2.obtain();
		noOffset.set(0,0);
		polyline(vertices, loop, noOffset);
		Pools.vector2.free(noOffset);
	}

	/**
	 * Draws a polyline in the x/y plane. The vertices must contain at least 2 points (4 floats x,y). The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param vertices all lines
	 * @param loop set to true if end and beginning shall be connected
	 * @param offset offset all lines with this much
	 */
	public void polyline(ArrayList<Vector2> vertices, boolean loop, Vector2 offset) {
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call push(ShapeType.Line)");
		}
		if (vertices.size() < 1) {
			throw new IllegalArgumentException("Polylines must contain at least 2 points.");
		}

		if (loop) {
			for (int i = 0; i < vertices.size(); ++i) {
				int nextIndex = Geometry.computeNextIndex(vertices, i);
				line(vertices.get(i).x + offset.x, vertices.get(i).y + offset.y, vertices.get(nextIndex).x + offset.x, vertices.get(nextIndex).y + offset.y);
			}
		} else {
			for (int i = 0; i < vertices.size() - 1; ++i) {
				line(vertices.get(i).x + offset.x, vertices.get(i).y + offset.y, vertices.get(i+1).x + offset.x, vertices.get(i+1).y + offset.y);
			}
		}
	}

	// -----------------------
	// Old stuff
	// -----------------------
	/** Shape types to be used with {@link #push(ShapeType)}.
	 * @author mzechner, stbachmann */
	public enum ShapeType {
		/** Used for drawing points */
		Point(GL10.GL_POINTS),
		/** Used for drawing lines */
		Line(GL10.GL_LINES),
		/** Used for drawing triangles */
		Filled(GL10.GL_TRIANGLES);

		/** Type sent to OpenGL */
		private final int glType;

		/**
		 * Creates the shape type
		 * @param glType type that will get sent to OpenGL
		 */
		ShapeType (int glType) {
			this.glType = glType;
		}

		/**
		 * @return OpenGL type for rendering
		 */
		public int getGlType () {
			return glType;
		}
	}

	/**
	 * Default constructor. Creates 5000 max vertices
	 */
	public ShapeRendererEx() {
		this(5000);
	}

	/**
	 * @param maxVertices
	 */
	public ShapeRendererEx(int maxVertices) {
		if (Gdx.graphics.isGL20Available()) {
			mRenderer = new ImmediateModeRenderer20(maxVertices, false, true, 0);
		}
		else {
			mRenderer = new ImmediateModeRenderer10(maxVertices);
		}
		mProjView.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mMatrixDirty = true;
	}

	/** Sets the {@link Color} to be used by shapes.
	 * @param color */
	public void setColor(Color color) {
		this.mColor.set(color);
	}

	/** Sets the {@link Color} to be used by shapes.
	 * @param r
	 * @param g
	 * @param b
	 * @param a */
	public void setColor(float r, float g, float b, float a) {
		this.mColor.set(r, g, b, a);
	}

	/** Sets the projection matrix to be used for rendering. Usually this will be set to {@link Camera#combined}.
	 * @param matrix */
	public void setProjectionMatrix(Matrix4 matrix) {
		mProjView.set(matrix);
		mMatrixDirty = true;
	}

	/**
	 * @param matrix
	 */
	public void setTransformMatrix(Matrix4 matrix) {
		mTransform.set(matrix);
		mMatrixDirty = true;
	}

	/** Sets the transformation matrix to identity. */
	public void identity() {
		mTransform.idt();
		mMatrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a translation matrix.
	 * @param x
	 * @param y
	 * @param z */
	public void translate(float x, float y, float z) {
		mTransform.translate(x, y, z);
		mMatrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a rotation matrix.
	 * @param angle angle in degrees
	 * @param axisX
	 * @param axisY
	 * @param axisZ */
	public void rotate(float axisX, float axisY, float axisZ, float angle) {
		mTransform.rotate(axisX, axisY, axisZ, angle);
		mMatrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a scale matrix.
	 * @param scaleX
	 * @param scaleY
	 * @param scaleZ */
	public void scale(float scaleX, float scaleY, float scaleZ) {
		mTransform.scale(scaleX, scaleY, scaleZ);
		mMatrixDirty = true;
	}

	/** Starts a new batch of shapes. All shapes within the batch have to have the type specified. E.g. if {@link ShapeType#Point}
	 * is specified, only call #point().
	 * 
	 * In case OpenGL ES 1.x is used, the projection and modelview matrix will be modified.
	 * */
	private void begin() {
		if (mCurrentType.isEmpty()) {
			throw new GdxRuntimeException("Call push() before beginning a new shape batch");
		}
		if (mMatrixDirty) {
			mCombined.set(mProjView);
			Matrix4.mul(mCombined.val, mTransform.val);
			mMatrixDirty = false;
		}
		mRenderer.begin(mCombined, mCurrentType.peek().getGlType());
	}

	/** Draws a point. The {@link ShapeType} passed to begin has to be {@link ShapeType#Point}.
	 * @param x
	 * @param y
	 * @param z */
	public void point(float x, float y, float z){
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Point) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Point)");
		}
		checkDirty();
		checkFlush(1);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z);
	}

	/** Draws a line. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param x
	 * @param y
	 * @param z
	 * @param x2
	 * @param y2
	 * @param z2 */
	public void line(float x, float y, float z, float x2, float y2, float z2){
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		}
		checkDirty();
		checkFlush(2);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x2, y2, z2);
	}

	/** Draws a line in the x/y plane. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2 */
	public void line(float x, float y, float x2, float y2){
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call push(ShapeType.Line)");
		}
		checkDirty();
		checkFlush(2);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, 0);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x2, y2, 0);
	}

	/** Draws a line in the x/y plane. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param posA draw line from this position
	 * @param posB to this position
	 */
	public void line(Vector2 posA, Vector2 posB) {
		line(posA.x, posA.y, posB.x, posB.y);
	}

	/** Draws a line in the x/y plane. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param posA draw line from this position
	 * @param posB to this position
	 * @param offset offsets posA and posB with offset.
	 */
	public void line(Vector2 posA, Vector2 posB, Vector2 offset) {
		line(posA.x + offset.x, posA.y + offset.y, posB.x + offset.x, posB.y + offset.y);
	}

	/**
	 * @param x1
	 * @param y1
	 * @param cx1
	 * @param cy1
	 * @param cx2
	 * @param cy2
	 * @param x2
	 * @param y2
	 * @param segments
	 */
	public void curve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, int segments){
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call push(ShapeType.Line)");
		}
		checkDirty();
		checkFlush(segments * 2 + 2);

		// Algorithm from: http://www.antigrain.com/research/bezier_interpolation/index.html#PAGE_BEZIER_INTERPOLATION
		float subdiv_step = 1f / segments;
		float subdiv_step2 = subdiv_step * subdiv_step;
		float subdiv_step3 = subdiv_step * subdiv_step * subdiv_step;

		float pre1 = 3 * subdiv_step;
		float pre2 = 3 * subdiv_step2;
		float pre4 = 6 * subdiv_step2;
		float pre5 = 6 * subdiv_step3;

		float tmp1x = x1 - cx1 * 2 + cx2;
		float tmp1y = y1 - cy1 * 2 + cy2;

		float tmp2x = (cx1 - cx2) * 3 - x1 + x2;
		float tmp2y = (cy1 - cy2) * 3 - y1 + y2;

		float fx = x1;
		float fy = y1;

		float dfx = (cx1 - x1) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3;
		float dfy = (cy1 - y1) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3;

		float ddfx = tmp1x * pre4 + tmp2x * pre5;
		float ddfy = tmp1y * pre4 + tmp2y * pre5;

		float dddfx = tmp2x * pre5;
		float dddfy = tmp2y * pre5;

		while (segments-- > 0) {
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(fx, fy, 0);
			fx += dfx;
			fy += dfy;
			dfx += ddfx;
			dfy += ddfy;
			ddfx += dddfx;
			ddfy += dddfy;
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(fx, fy, 0);
		}
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(fx, fy, 0);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x2, y2, 0);
	}

	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 */
	public void triangle(float x1, float y1, float x2, float y2, float x3, float y3){
		if (mCurrentType.isEmpty() || (mCurrentType.peek() != ShapeType.Filled && mCurrentType.peek() != ShapeType.Line)) {
			throw new GdxRuntimeException("Must call push(ShapeType.Filled) or push(ShapeType.Line)");
		}
		checkDirty();
		checkFlush(6);
		if(mCurrentType.peek() == ShapeType.Line){
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x1, y1, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x2, y2, 0);

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x2, y2, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x3, y3, 0);

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x3, y3, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x1, y1, 0);
		}
		else {
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x1, y1, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x2, y2, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x3, y3, 0);
		}
	}

	/** Draws a rectangle in the x/y plane. The x and y coordinate specify the bottom left corner of the rectangle. The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Filled} or  {@link ShapeType#Line}.
	 * @param x
	 * @param y
	 * @param width
	 * @param height */
	public void rect(float x, float y, float width, float height){
		if (mCurrentType.isEmpty() || (mCurrentType.peek() != ShapeType.Filled && mCurrentType.peek() != ShapeType.Line)) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		}

		checkDirty();
		checkFlush(8);

		if(mCurrentType.peek() == ShapeType.Line){
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + width, y, 0);

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + width, y, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + width, y + height, 0);

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + width, y + height, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y + height, 0);

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y + height, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y, 0);
		}
		else {
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + width, y, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + width, y + height, 0);

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + width, y + height, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y + height, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y, 0);
		}
	}


	/** Draws a rectangle in the x/y plane. The x and y coordinate specify the bottom left corner of the rectangle. The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Filled} or  {@link ShapeType#Line}.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param col1 The color at (x, y)
	 * @param col2 The color at (x + width, y)
	 * @param col3 The color at (x + width, y + height)
	 * @param col4 The color at (x, y + height) */
	public void rect(float x, float y, float width, float height, Color col1, Color col2, Color col3, Color col4){
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Filled && mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		}

		checkDirty();
		checkFlush(8);

		if(mCurrentType.peek() == ShapeType.Line){
			mRenderer.color(col1.r, col1.g, col1.b, col1.a);
			mRenderer.vertex(x, y, 0);
			mRenderer.color(col2.r, col2.g, col2.b, col2.a);
			mRenderer.vertex(x + width, y, 0);

			mRenderer.color(col2.r, col2.g, col2.b, col2.a);
			mRenderer.vertex(x + width, y, 0);
			mRenderer.color(col3.r, col3.g, col3.b, col3.a);
			mRenderer.vertex(x + width, y + height, 0);

			mRenderer.color(col3.r, col3.g, col3.b, col3.a);
			mRenderer.vertex(x + width, y + height, 0);
			mRenderer.color(col4.r, col4.g, col4.b, col4.a);
			mRenderer.vertex(x, y + height, 0);

			mRenderer.color(col4.r, col4.g, col4.b, col4.a);
			mRenderer.vertex(x, y + height, 0);
			mRenderer.color(col1.r, col1.g, col1.b, col1.a);
			mRenderer.vertex(x, y, 0);
		}
		else {
			mRenderer.color(col1.r, col1.g, col1.b, col1.a);
			mRenderer.vertex(x, y, 0);
			mRenderer.color(col2.r, col2.g, col2.b, col2.a);
			mRenderer.vertex(x + width, y, 0);
			mRenderer.color(col3.r, col3.g, col3.b, col3.a);
			mRenderer.vertex(x + width, y + height, 0);

			mRenderer.color(col3.r, col3.g, col3.b, col3.a);
			mRenderer.vertex(x + width, y + height, 0);
			mRenderer.color(col4.r, col4.g, col4.b, col4.a);
			mRenderer.vertex(x, y + height, 0);
			mRenderer.color(col1.r, col1.g, col1.b, col1.a);
			mRenderer.vertex(x, y, 0);
		}
	}

	/** Draws a box. The x, y and z coordinate specify the bottom left front corner of the rectangle. The {@link ShapeType} passed
	 * to begin has to be {@link ShapeType#Line}.
	 * @param x
	 * @param y
	 * @param z
	 * @param width
	 * @param height
	 * @param depth
	 */
	public void box(float x, float y, float z, float width, float height, float depth){
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		}

		checkDirty();
		checkFlush(16);

		depth = -depth;

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y, z);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y, z);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y, z + depth);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y, z + depth);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z + depth);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z + depth);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y + height, z);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y + height, z);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y + height, z);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y + height, z);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y + height, z + depth);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y + height, z + depth);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y + height, z + depth);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y + height, z + depth);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y + height, z);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y, z);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y + height, z);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y, z + depth);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + width, y + height, z + depth);

		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y, z + depth);
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x, y + height, z + depth);
	}

	/** Calls {@link #circle(float, float, float, int)} by estimating the number of segments needed for a smooth circle.
	 * @param x
	 * @param y
	 * @param radius */
	public void circle (float x, float y, float radius) {
		circle(x, y, radius, (int)(6 * (float)Math.cbrt(radius)));
	}

	/**
	 * Creates a circle
	 * @param x
	 * @param y
	 * @param radius
	 * @param segments
	 */
	public void circle(float x, float y, float radius, int segments){
		if (segments <= 0) {
			throw new IllegalArgumentException("segments must be >= 0.");
		}
		if (mCurrentType.isEmpty() || (mCurrentType.peek() != ShapeType.Filled && mCurrentType.peek() != ShapeType.Line)) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		}
		checkDirty();
		checkFlush(segments * 2 + 2);

		float angle = 2 * 3.1415926f / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		if(mCurrentType.peek() == ShapeType.Line){
			for (int i = 0; i < segments; i++) {
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, 0);
			}
			// Ensure the last segment is identical to the first.
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + cx, y + cy, 0);
		}
		else {
			segments--;
			for (int i = 0; i < segments; i++) {
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x, y, 0);
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, 0);
			}
			// Ensure the last segment is identical to the first.
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + cx, y + cy, 0);
		}

		cx = radius;
		cy = 0;
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + cx, y + cy, 0);
	}

	/** Calls {@link #cone(float, float, float, float, float, int)} by estimating the number of segments needed for a smooth
	 * circular base.
	 * @param x
	 * @param y
	 * @param z
	 * @param radius
	 * @param height */
	public void cone (float x, float y, float z, float radius, float height) {
		cone(x, y, z, radius, height, (int)(4 * (float)Math.sqrt(radius)));
	}

	/**
	 * Draws a cone
	 * @param x
	 * @param y
	 * @param z
	 * @param radius
	 * @param height
	 * @param segments
	 */
	public void cone(float x, float y, float z, float radius, float height, int segments){
		if (segments <= 0) {
			throw new IllegalArgumentException("segments must be >= 0.");
		}
		if (mCurrentType.isEmpty() || (mCurrentType.peek() != ShapeType.Filled && mCurrentType.peek() != ShapeType.Line)) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		}
		checkDirty();
		checkFlush(segments * 4 + 2);
		float angle = 2 * 3.1415926f / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		if(mCurrentType.peek() == ShapeType.Line){
			for (int i = 0; i < segments; i++) {
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, z);
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x, y, z + height);
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, z);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, z);
			}
			// Ensure the last segment is identical to the first.
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + cx, y + cy, z);
		}
		else {
			segments--;
			for (int i = 0; i < segments; i++) {
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x, y, z);
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, z);
				float temp = cx;
				float temp2 = cy;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, z);
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + temp, y + temp2, z);
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x + cx, y + cy, z);
				mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
				mRenderer.vertex(x, y, z + height);
			}
			// Ensure the last segment is identical to the first.
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x, y, z);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x + cx, y + cy, z);
		}
		cx = radius;
		cy = 0;
		mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
		mRenderer.vertex(x + cx, y + cy, z);
	}

	/** Draws a polygon in the x/y plane. The vertices must contain at least 3 points (6 floats x,y). The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param vertices */
	public void polygon(float[] vertices){
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		}
		if (vertices.length < 6) {
			throw new IllegalArgumentException("Polygons must contain at least 3 points.");
		}
		if (vertices.length % 2 != 0) {
			throw new IllegalArgumentException("Polygons must have a pair number of vertices.");
		}
		final int numFloats = vertices.length;

		checkDirty();
		checkFlush(numFloats);

		float firstX = vertices[0];
		float firstY = vertices[1];

		for (int i = 0; i < numFloats; i += 2) {
			float x1 = vertices[i];
			float y1 = vertices[i + 1];

			float x2;
			float y2;

			if(i + 2 >= numFloats){
				x2 = firstX;
				y2 = firstY;
			}else{
				x2 = vertices[i + 2];
				y2 = vertices[i + 3];
			}

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x1, y1, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x2, y2, 0);
		}
	}

	/** Draws a polyline in the x/y plane. The vertices must contain at least 2 points (4 floats x,y). The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param vertices */
	public void polyline(float[] vertices) {
		if (mCurrentType.isEmpty() || mCurrentType.peek() != ShapeType.Line) {
			throw new GdxRuntimeException("Must call push(ShapeType.Line)");
		}
		if (vertices.length < 4) {
			throw new IllegalArgumentException("Polylines must contain at least 2 points.");
		}
		if (vertices.length % 2 != 0) {
			throw new IllegalArgumentException("Polylines must have a pair number of vertices.");
		}
		final int numFloats = vertices.length;

		checkDirty();
		checkFlush(numFloats);

		for (int i = 0; i < numFloats - 2; i += 2) {
			float x1 = vertices[i];
			float y1 = vertices[i + 1];

			float x2;
			float y2;

			x2 = vertices[i + 2];
			y2 = vertices[i + 3];

			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x1, y1, 0);
			mRenderer.color(mColor.r, mColor.g, mColor.b, mColor.a);
			mRenderer.vertex(x2, y2, 0);
		}
	}

	/**
	 * Checks if the matrix is dirty, it flushes the vertices if it is.
	 */
	protected void checkDirty () {
		if (!mMatrixDirty) {
			return;
		}
		flush();
	}

	/**
	 * Check if we need to flush
	 * @param newVertices
	 */
	protected void checkFlush (int newVertices) {
		if (mRenderer.getMaxVertices() - mRenderer.getNumVertices() >= newVertices) {
			return;
		}
		flush();
	}

	/**
	 * Flushes the shapes to OpenGL. Equivalent to end(); and begin(type);
	 */
	public void flush () {
		mRenderer.end();
		begin();
	}

	/**
	 * @return current shape type
	 */
	public ShapeType getCurrentType () {
		if (mCurrentType.isEmpty()) {
			return null;
		} else {
			return mCurrentType.peek();
		}
	}

	@Override
	public void dispose () {
		mRenderer.dispose();
	}

	/**
	 * Pushes the shape type, i.e. will end current rendering type and start
	 * to draw using this one. {@link #pop()} will return to the previous rendering type
	 * @param shapeType the new shape type to use for future drawing.
	 */
	public void push(ShapeType shapeType) {
		if (mCurrentType.isEmpty() || mCurrentType.peek() != shapeType) {
			if (!mCurrentType.isEmpty()) {
				mRenderer.end();
			}
			mCurrentType.push(shapeType);
			begin();
		} else {
			mCurrentType.push(shapeType);
		}
	}

	/**
	 * Pops the current rendering type and start to draw the previous one. If
	 * no previous rendering type exist.
	 */
	public void pop() {
		if (mCurrentType.isEmpty()) {
			throw new GdxRuntimeException("Called pop() more times than push()!");
		}

		ShapeType poppedType = mCurrentType.pop();
		if (mCurrentType.isEmpty() || mCurrentType.peek() != poppedType) {
			mRenderer.end();
		}
		if (!mCurrentType.isEmpty()) {
			begin();
		}
	}

	/** Renderer */
	ImmediateModeRenderer mRenderer;
	/** If the matrix is dirty */
	boolean mMatrixDirty = false;
	/** projection view matrix */
	Matrix4 mProjView = new Matrix4();
	/** any temporary transformtion to move the object */
	Matrix4 mTransform = new Matrix4();
	/** combined view of the camera */
	Matrix4 mCombined = new Matrix4();
	/** temporary matrix */
	Matrix4 mTmp = new Matrix4();
	/** Color of the shape */
	Color mColor = new Color(1, 1, 1, 1);
	/** Stack of shape type */
	Stack<ShapeType> mCurrentType = new Stack<ShapeRendererEx.ShapeType>();
}
