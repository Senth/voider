package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.math.Vector2;

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
}
