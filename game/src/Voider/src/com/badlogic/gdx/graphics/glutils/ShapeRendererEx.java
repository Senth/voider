package com.badlogic.gdx.graphics.glutils;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * A more advanced shape renderer which allows custom shaders and lines
 * with multicolors.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ShapeRendererEx extends ShapeRenderer {
	/**
	 * Sets the shader for the shape renderer
	 * @param shaderProgram shader to be used for rendering shapes
	 */
	public void setShader(ShaderProgram shaderProgram) {
		if (renderer instanceof ImmediateModeRenderer20) {
			((ImmediateModeRenderer20)renderer).setShader(shaderProgram);
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
		for (int i = 0; i < triangles.size(); i += 3) {
			triangle(triangles, i);
		}
	}

	/**
	 * Draws all triangles in the array with an position offset
	 * @param triangles an array with triangles to draw
	 * @param positionOffset position offset of the triangles
	 */
	public void triangles(ArrayList<Vector2> triangles, Vector2 positionOffset) {
		Vector2[] localVertices = new Vector2[3];
		for (int i = 0; i < localVertices.length; ++i) {
			localVertices[i] = Vector2Pool.obtain();
		}

		for (int triangleIndex = 0; triangleIndex < triangles.size() - 2; triangleIndex += 3) {
			for (int localIndex = 0; localIndex < localVertices.length; ++localIndex) {
				localVertices[localIndex].set(triangles.get(triangleIndex + localIndex)).add(positionOffset);
			}
			triangle(localVertices);
		}

		for (Vector2 vertex : localVertices) {
			Vector2Pool.free(vertex);
		}
	}
}
